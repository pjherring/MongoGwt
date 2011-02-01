/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;


import org.pjherring.mongogwt.shared.domain.operation.Read;
import com.google.inject.Inject;
import com.mongodb.DB;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.pjherring.mongogwt.server.domain.cache.EntityMetaCache;
import org.pjherring.mongogwt.server.domain.cache.EntityMetaCache.ReferenceMeta;
import org.pjherring.mongogwt.server.util.EntityUtil;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.annotations.enums.ReferenceType;
import org.pjherring.mongogwt.shared.domain.operation.Delete;
import org.pjherring.mongogwt.shared.exception.NotPersistedException;
import org.pjherring.mongogwt.shared.query.Query;

/**
 *
 * @author pjherring
 */
public class DeleteImpl implements Delete {

    private final static Logger LOG = Logger.getLogger(DeleteImpl.class.getName());

    protected DB mongoDb;
    protected Read read;
    protected PojoToDBObject translator;
    protected EntityMetaCache entityMetaCache;
    protected Map<String, Map<String, IsEntity>> entityDeleteQueue =
        new HashMap<String, Map<String, IsEntity>>();
    protected EntityUtil util;

    @Inject
    public DeleteImpl(
        DB mongoDb,
        PojoToDBObject translator,
        EntityMetaCache entityMetaCache,
        Read read,
        EntityUtil util) {
        this.mongoDb = mongoDb;
        this.translator = translator;
        this.entityMetaCache = entityMetaCache;
        this.read = read;
        this.util = util;
    }

    /*
     * Deletes the entity.
     *
     * @param The entity to delete.
     */
    @Override
    public void delete(IsEntity entity) {
        entityDeleteQueue.clear();
        markForDeletion(entity);
        doDelete();
        entity.setId(null);
        entity.setCreatedDatetime(null);
    }

    /*
     * Deletes all entities found in the query.
     *
     * @param
     */
    @Override
    public <T extends IsEntity> void delete(Query query, Class<T> clazz) {
        List<T> results = read.find(query, clazz, false);

        //clear our queue
        entityDeleteQueue.clear();

        for (T entity : results) {
            markForDeletion(entity);
        }

        doDelete();
    }

    protected void doOneToManyMarkDeletion(
        IsEntity parent,
        ReferenceMeta referenceMeta) {

        Collection<IsEntity> collection = null;

        try {
            collection =
                (Collection<IsEntity>) referenceMeta.getGetter().invoke(parent);
        } catch (Exception e) {
            LOG.warning("FAILURE IN GETTING ONE TO MANY REFERENCE TO DELETE");
            throw new RuntimeException(e);
        }

        Set<IsEntity> setToDelete = new HashSet<IsEntity>(collection);
        for (IsEntity entity : setToDelete) {
            markForDeletion(entity);
        }
    }

    protected void doManyToOneOneToOneMarkDeletion(
        IsEntity parent,
        ReferenceMeta referenceMeta) {
        IsEntity referencedEntity = null;

        try {
            referencedEntity
                = (IsEntity) referenceMeta.getGetter().invoke(parent);
        } catch (Exception e) {
            LOG.warning("FAILURE IN GETTING MANY TO ONE REFERENCE TO DELETE");
            throw new RuntimeException(e);
        }


        markForDeletion(referencedEntity);
    }

    protected void markForDeletion(IsEntity entity) {

        //we can't delete something that's not persisted
        if (entity.getId() == null) {
            throw new NotPersistedException();
        }

        //have we already marked this entity for deletion
        if (!isMarkedForDeletion(entity)) {

            Set<ReferenceMeta> referenceMetaSet
                = entityMetaCache.getReferenceMetaSet(entity.getClass());

            /*
             * Iterate through all the entity's references and check if
             * cascade delete is true. If so, mark those for deletion.
             */
            for (ReferenceMeta referenceMeta : referenceMetaSet) {

                //if this is true then we have to go delete the references
                if (referenceMeta.getReference().doCascadeDelete()) {
                    if (referenceMeta.getReference().type().equals(ReferenceType.ONE_TO_MANY)) {
                        doOneToManyMarkDeletion(entity, referenceMeta);
                    } else {
                        doManyToOneOneToOneMarkDeletion(entity, referenceMeta);
                    }
                }
            }

            //this will actually "mark" the entity for deletion
            putInDeleteQueue(entity);
        }

    }

    /*
     * This method actually will delete the entities from the data store.
     */
    protected void doDelete() {
        for (String collectionName : entityDeleteQueue.keySet()) {
            for (IsEntity entityToDelete : entityDeleteQueue.get(collectionName).values()) {
                mongoDb.getCollection(collectionName)
                    .remove(translator.translate(entityToDelete));
            }
        }
    }

    protected boolean isMarkedForDeletion(IsEntity entity) {
        String collectionName = util.getCollectionName(entity);
        return entityDeleteQueue.containsKey(collectionName)
             && entityDeleteQueue.get(collectionName)
                .keySet().contains(entity.getId());
    }

    protected void putInDeleteQueue(IsEntity entity) {
        String collectionName = util.getCollectionName(entity);

        if (!entityDeleteQueue.containsKey(collectionName)) {
            entityDeleteQueue.put(collectionName, new HashMap<String, IsEntity>());
        }

        entityDeleteQueue.get(collectionName).put(entity.getId(), entity);
    }

}

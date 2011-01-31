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

    @Override
    public void delete(IsEntity entity) {
        entityDeleteQueue.clear();
        markForDeletion(entity);

        for (String collectionName : entityDeleteQueue.keySet()) {
            for (IsEntity entityToDelete : entityDeleteQueue.get(collectionName).values()) {
                mongoDb.getCollection(collectionName)
                    .remove(translator.translate(entityToDelete));
            }
        }

        entity.setId(null);
        entity.setCreatedDatetime(null);
    }

    protected void doOneToManyMarkDeletion(IsEntity parent, ReferenceMeta referenceMeta) {

        Collection<IsEntity> collection = null;

        try {
            collection =
                (Collection<IsEntity>) referenceMeta.getGetter().invoke(parent);
        } catch (Exception e) {
            LOG.warning("FAILURE IN GETTING ONE TO MANY REFERENCE TO DELETE");
            throw new RuntimeException(e);
        }

        while (collection.iterator().hasNext()) {
            markForDeletion(collection.iterator().next());
        }
    }

    protected void doManyToOneOneToOneMarkDeletion(IsEntity parent, ReferenceMeta referenceMeta) {
        IsEntity referencedEntity = null;

        try {
            referencedEntity
                = (IsEntity) referenceMeta.getGetter().invoke(parent);
        } catch (Exception e) {
            LOG.warning("FAILURE IN GETTING MANY TO ONE REFERENCE TO DELETE");
            throw new RuntimeException(e);
        }

        LOG.info("FOUND ONE TO MANY");

        markForDeletion(referencedEntity);
    }

    protected void markForDeletion(IsEntity entity) {

        if (entity.getId() == null) {
            throw new NotPersistedException();
        }

        if (!isMarkedForDeletion(entity)) {
            Set<ReferenceMeta> referenceMetaSet = entityMetaCache.getReferenceMetaSet(entity.getClass());
            LOG.info("SIZE for " + entity.getClass().getName() + " : " + referenceMetaSet.size());

            for (ReferenceMeta referenceMeta : entityMetaCache.getReferenceMetaSet(entity.getClass())) {

                LOG.info("REFERENCES");
                //if this is true then we have to go delete the references
                if (referenceMeta.getReference().doCascadeDelete()) {
                    if (referenceMeta.getReference().type().equals(ReferenceType.ONE_TO_MANY)) {
                        LOG.info("ONE TO MANY");
                        doOneToManyMarkDeletion(entity, referenceMeta);
                    } else {
                        LOG.info("DOING ONE TO ONE OR MANY TO ONE");
                        doManyToOneOneToOneMarkDeletion(entity, referenceMeta);
                    }
                }
            }

            putInDeleteQueue(entity);
        }

    }

    public void delete(Query query) {
        throw new UnsupportedOperationException("Not supported yet.");
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

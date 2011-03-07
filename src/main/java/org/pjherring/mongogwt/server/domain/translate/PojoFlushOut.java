/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.translate;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.bson.types.ObjectId;
import org.pjherring.mongogwt.server.domain.cache.EntityMetaCache;
import org.pjherring.mongogwt.server.domain.cache.ReferenceMeta;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.annotations.enums.ReferenceType;

/**
 * This class takes in an Entity and fetch's all of its references that the
 * entity does not manage.
 *
 * @author pjherring
 */
@Singleton
public class PojoFlushOut {

    private final static Logger LOG = Logger.getLogger(PojoFlushOut.class.getName());

    protected DB mongoDb;
    protected DBObjectToPojo translate;
    protected EntityMetaCache entityMetaCache;

    @Inject
    public PojoFlushOut(
        DB mongoDb,
        DBObjectToPojo translate,
        EntityMetaCache entityMetaCache) {

        this.mongoDb = mongoDb;
        this.translate = translate;
        this.entityMetaCache = entityMetaCache;
    }

    /*
     * The main method of this class. Takes in an entity and fetches its
     * non managed references.
     *
     * @param entity The entity to get the non-managed references for.
     * @param doFanOut Whether or not to grab all the attributes of the
     *      non managed references. If true it will, if false it will only
     *      grab the id and createdDateTime
     *
     * @return void
     */
    public void flush(IsEntity entity, boolean doFanOut) {

        Class<? extends IsEntity> clazz = entity.getClass();
        Set<ReferenceMeta> referenceMetaSet =
            entityMetaCache.getNonColumnReferenceMetaSet(clazz);

        for (ReferenceMeta referenceMeta : referenceMetaSet) {

            if (!referenceMeta.getReference().managedBy().equals("")) {

                DBCursor cursor = getReferences(entity, doFanOut, referenceMeta);

                Object value = null;

                ReferenceType typeOfReference = referenceMeta.getReference().type();

                if (typeOfReference.equals(ReferenceType.ONE_TO_MANY)) {
                    //fetch references
                    List<IsEntity> entityList = new ArrayList<IsEntity>();

                    while (cursor.hasNext()) {
                        entityList.add(translate.translate(
                            cursor.next(),
                            referenceMeta.getReferencedClass(),
                            doFanOut
                        ));
                    }

                    value = Set.class.isAssignableFrom(
                        (referenceMeta.getGetter().getReturnType())
                    ) ? new HashSet(entityList) : entityList;

                } else {
                    /*
                     * Since this is a many to one or a one to one we know that
                     * the result of this query should only be one object.
                     */
                    DBObject referenceAsObject = cursor.limit(1).next();

                    value = translate.translate(
                        referenceAsObject,
                        referenceMeta.getReferencedClass(),
                        doFanOut
                    );

                }

                try {
                    referenceMeta.getSetter().invoke(entity, value);
                } catch (Exception e) {
                    LOG.warning("Failure in setting: "
                        + referenceMeta.getSetter().getName()
                        + " on " + clazz.getName()
                    );

                    throw new RuntimeException(e);
                }
            }
        }
    }

    /*
     * Queries the database to get the references.
     *
     * @param entity Entity you are setting the references on
     * @param doFanOut Do fetch the complete entities or just the id and createdDate
     * @param referenceData contains information about the reference
     *
     * @return the cursor for the DBObjects
     *
     */
    private DBCursor getReferences(
        IsEntity entity,
        boolean doFanOut,
        ReferenceMeta referenceMeta) {

        /*
         * If not fanning out we just want the id and nothing else about the
         * referenced entity.
         */
        DBObject keys = doFanOut ? null : new BasicDBObject("_id", 1);
        String entityCollectionName =
            entity.getClass().getAnnotation(Entity.class).name();
        DBObject query = new BasicDBObject(
            referenceMeta.getReference().managedBy(),
            new DBRef(mongoDb, entityCollectionName, new ObjectId(entity.getId()))
        );

        String referencedCollectionName = referenceMeta
            .getReferencedClass().getAnnotation(Entity.class).name();

        return mongoDb.getCollection(referencedCollectionName)
            .find(query, keys);
    }
}

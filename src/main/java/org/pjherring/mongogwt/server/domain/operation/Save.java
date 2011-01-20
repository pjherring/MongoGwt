/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.inject.Inject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bson.types.ObjectId;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.IsEmbeddable;
import org.pjherring.mongogwt.shared.domain.operation.DoesCreate;
import org.pjherring.mongogwt.shared.domain.operation.DoesRead;
import org.pjherring.mongogwt.shared.domain.operation.DoesUpdate;
import org.pjherring.mongogwt.shared.annotations.Column;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.annotations.Enumerated;
import org.pjherring.mongogwt.shared.annotations.Reference;
import org.pjherring.mongogwt.shared.annotations.enums.ReferenceType;
import org.pjherring.mongogwt.shared.exception.InvalidCollectionException;
import org.pjherring.mongogwt.shared.exception.ValidationException;

/**
 *
 * @author pjherring
 */
public class Save extends BaseDatabaseOperation
    implements DoesCreate, DoesUpdate {

    private final Logger LOG = Logger.getLogger("Save");
    protected DoesRead reader;

    @Inject
    public Save(DoesRead reader) {
        this.reader = reader;
    }

    /*
     * Saves a pojo to the db.
     */
    public void doCreate(IsEntity pojoToSave) {
        validator.validatePojo(pojoToSave);
        Entity domainCollection
            = pojoToSave.getClass().getAnnotation(Entity.class);


        if (domainCollection.doPersist()) {

            Map<String, Object> valueMap = getValueMap(pojoToSave, domainCollection);
            DBObject dbObjectToBeSaved = BasicDBObjectBuilder.start(valueMap).get();
            getDatabase().getCollection(domainCollection.name()).insert(dbObjectToBeSaved);

            //set Id of pojo
            ObjectId objectId = (ObjectId) dbObjectToBeSaved.get("_id");
            pojoToSave.setId(objectId.toString());
            pojoToSave.setCreatedDatetime(new Date(objectId.getTime()));
        }
    }

    public void doUpdate(IsEntity pojoToUpdate) {
        validator.validateCollection(pojoToUpdate.getClass());

        Entity domainCollection
            = pojoToUpdate.getClass().getAnnotation(Entity.class);
        IsEntity storedPojo = reader.findById(pojoToUpdate.getId(), pojoToUpdate.getClass(), true);

        for (Method method : storedPojo.getClass().getMethods()) {
            if (method.isAnnotationPresent(Column.class)) {
                try {
                    Method pojoToUpdateGetter = pojoToUpdate.getClass().getMethod(method.getName());
                    Object updateValue = pojoToUpdateGetter.invoke(pojoToUpdate);
                    Object storedValue = method.invoke(storedPojo);
                    Column column = method.getAnnotation(Column.class);

                    if (updateValue != null && storedValue != null) {
                        if (updateValue == null && storedValue != null) {
                            validator.validateColumn(column, null, domainCollection);
                        } else if ((storedValue == null && updateValue != null)
                            || (!updateValue.equals(storedValue))) {
                            validator.validateColumn(column, storedValue, domainCollection);
                        }
                    }
                } catch (Exception e) {
                    if (e instanceof ValidationException) {
                        throw (ValidationException) e;
                    } else {
                        throw new Error(e);
                    }
                }
            }
        }

        Map<String, Object> updateValueMap = getValueMap(pojoToUpdate, domainCollection);
        Map<String, Object> storedValueMap = getValueMap(storedPojo, domainCollection);
        DBObject updated = BasicDBObjectBuilder.start(updateValueMap).get();
        DBObject stored = BasicDBObjectBuilder.start(storedValueMap).get();
        database.getCollection(domainCollection.name()).findAndModify(stored, updated);
    }

    /*
     * Converts a pojo to a Map<String, Object>
     * Will save any unsaved references, will only have ObjectId's of reference
     * in db record
     */
    protected Map<String, Object> getValueMap(
        IsSerializable pojoToBuildFrom, Entity collectionDB
    ) {

        Map<String, Object> map = new HashMap<String, Object>();

        //go through each column, grab the value, and put it in the map
        for (Method method : pojoToBuildFrom.getClass().getMethods()) {
            if (method.isAnnotationPresent(Column.class)) {
                Column columnAnnotation = method.getAnnotation(Column.class);

                if (!columnAnnotation.isTransient()) {
                    Object valueToPutInMap;


                    try {
                        if (method.isAnnotationPresent(Reference.class)) {
                            valueToPutInMap = getReferenceValue(
                                method.getAnnotation(Reference.class),
                                pojoToBuildFrom,
                                method
                            );
                        } else {
                            valueToPutInMap = method.invoke(pojoToBuildFrom);

                            if (method.isAnnotationPresent(Enumerated.class)) {
                                Enumerated enumAnno =
                                    method.getAnnotation(Enumerated.class);
                                valueToPutInMap
                                    = ((Enum) valueToPutInMap).name();
                            }
                        }
                    } catch (Exception e) {
                        throw new Error(e);
                    }

                    //do we have an embedded object

                    if (valueToPutInMap instanceof IsEmbeddable) {

                        if (!valueToPutInMap.getClass().isAnnotationPresent(Entity.class)) {
                            throw new InvalidCollectionException(
                                valueToPutInMap.getClass().getSimpleName()
                            );
                        }

                        IsEmbeddable valueAsEmbeddle = (IsEmbeddable) valueToPutInMap;

                        valueToPutInMap =
                            getValueMap(
                                valueAsEmbeddle,
                                valueToPutInMap.getClass().getAnnotation(
                                    Entity.class
                                )
                            );
                    }

                    map.put(columnAnnotation.name(), valueToPutInMap);
                }
            }
        }

        return map;

    }

    /*
     * Gets the value to be saved in the MongoDb DBObject from the reference
     */
    protected Object getReferenceValue(
        Reference reference, IsSerializable savingFromDomainObj, Method getter
    ) throws IllegalAccessException, IllegalArgumentException,
        InvocationTargetException {

        //are we dealing with a one to one relation
        if (reference.type()
            .equals(ReferenceType.ONE_TO_ONE)) {

            /*
             * grab the IsDomainObject from the getter
             * if its not saved, save it and return the ObjectId
             */
            IsEntity referencedDomainObjet
                = (IsEntity) getter.invoke(savingFromDomainObj);
            //if this object has not been persisted save it
            if (referencedDomainObjet.getId() == null) {
                doCreate(referencedDomainObjet);
            }

            return new ObjectId(referencedDomainObjet.getId());

        } else {
            /*
             * Need to get collection of IsDomainObject's
             * and store ids.
             */
            List<ObjectId> idsToBeSaved
                = new ArrayList<ObjectId>();
            Iterable<IsEntity> collection =
                (Iterable<IsEntity>) getter.invoke(savingFromDomainObj);
            //iterate throw the set or list that came back and get a list
            //of object ids
            for (IsEntity collectionMember : collection) {

                //make sure this is saved
                if (collectionMember.getId() == null) {
                    doCreate(collectionMember);
                }

                idsToBeSaved.add(new ObjectId(collectionMember.getId()));
            }
            return idsToBeSaved;
        }
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;


import com.google.inject.Inject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import org.bson.types.ObjectId;
import org.pjherring.mongogwt.server.guice.DatabaseModule.EntityList;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.domain.operation.Create;
import org.pjherring.mongogwt.shared.exception.AlreadyPersistedException;
import org.pjherring.mongogwt.shared.exception.InvalidEntity;
import org.pjherring.mongogwt.shared.exception.ValidationException;

/**
 *
 * @author pjherring
 */
public class CreateImpl implements Create {

    private final static Logger LOG = Logger.getLogger(CreateImpl.class.getName());
    
    protected DB mongoDb;
    protected List<Class<? extends IsEntity>> entityList;
    protected PojoToDBObject translator;
    protected Validate validate;

    @Inject
    public CreateImpl(
        DB mongoDb,
        @EntityList List<Class<? extends IsEntity>> entityList,
        PojoToDBObject translator,
        Validate validate) {
        this.mongoDb = mongoDb;
        this.entityList = entityList;
        this.translator = translator;
        this.validate = validate;
    }

    /*
     * Will either throw an exception for validation reasons or will actually
     * persist the object. If persistance happens the entity will have its
     * ID and Date set.
     *
     * @param entityToPersist The entity to persist.
     */
    public void doCreate(IsEntity entityToPersist) {

        //this will throw exceptions
        canPersist(entityToPersist);

        DBObject toBePersisted = translator.translate(entityToPersist);
        mongoDb
            .getCollection(
                entityToPersist.getClass().getAnnotation(Entity.class).name()
            ).insert(toBePersisted);
        ObjectId id = (ObjectId) toBePersisted.get("_id");

        entityToPersist.setId(id.toString());
        entityToPersist.setCreatedDatetime(new Date(id.getTime()));
    }

    /*
     * This checks the validity of the entity. This will check if the entity
     * has been already persisted, if it is valid to persist (both in the sense
     * that is in a list of entit
     */
    private void canPersist(IsEntity entityToPersist) throws ValidationException {
        if (!entityToPersist.getClass().isAnnotationPresent(Entity.class)) {
            LOG.warning("You must annotate your entities with Entity");
            throw new InvalidEntity(entityToPersist.getClass().getName());
        }

        if (entityToPersist.getId() != null) {
            LOG.warning("Can not persist an entity already persisted");
            throw new AlreadyPersistedException(entityToPersist.getClass().getName());
        }

        if (!entityList.contains(entityToPersist.getClass())) {
            LOG.warning("Can not persit an entity not in the entity list.");
            throw new InvalidEntity(entityToPersist.getClass().getName());
        }

        //call to validate to see if the entity is valid according to rules
        validate.validate(entityToPersist);
    }

}

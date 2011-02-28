/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;

import org.pjherring.mongogwt.server.domain.translate.DBObjectToPojo;
import org.pjherring.mongogwt.server.domain.translate.PojoToDBObject;
import org.pjherring.mongogwt.shared.domain.operation.Validate;
import org.pjherring.mongogwt.shared.domain.operation.Read;
import org.pjherring.mongogwt.shared.domain.operation.Update;
import com.google.inject.Inject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import java.util.List;
import java.util.Map;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.exception.NotPersistedException;
import org.pjherring.mongogwt.shared.exception.UniqueException;
import org.pjherring.mongogwt.shared.exception.ValidationException;

/**
 *
 * @author pjherring
 */
public class UpdateImpl implements Update {

    protected DB mongoDb;
    protected PojoToDBObject pojoToDBObject;
    protected DBObjectToPojo dbObjectToPojo;
    protected Validate validate;
    protected Read read;

    @Inject
    public UpdateImpl(
        DB mongoDb,
        PojoToDBObject pojoToDBObject,
        DBObjectToPojo dbObjectToPojo,
        Validate validate,
        Read read) {

        this.mongoDb = mongoDb;
        this.pojoToDBObject = pojoToDBObject;
        this.dbObjectToPojo = dbObjectToPojo;
        this.validate = validate;
        this.read = read;
    }

    public void doUpdate(IsEntity entity) {

        if (entity.getId() == null) {
            throw new NotPersistedException();
        }

        DBObject toBe =
            pojoToDBObject.translate(entity);

        DBObject whatIs =
            pojoToDBObject.translate(
                read.findById(entity.getId(), entity.getClass(), true)
        );

        validate.validate(entity, false);
        Map<String, List<ValidationException>> validtionExceptionMap
            = validate.getValidationErrorMap();

        for (String key : validtionExceptionMap.keySet()) {
            for (ValidationException exception : validtionExceptionMap.get(key)) {
                if (!(exception instanceof UniqueException)) {
                    throw exception;
                } else if (!whatIs.get(key).equals(toBe.get(key))) {
                    throw exception;
                }
            }
        }

        mongoDb.getCollection(
            entity.getClass().getAnnotation(Entity.class).name()
        ).findAndModify(whatIs, toBe);
    }

}

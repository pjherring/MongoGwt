/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;

import com.google.inject.Inject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBObject;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Logger;
import org.pjherring.mongogwt.shared.domain.operation.DoesValidate;
import org.pjherring.mongogwt.shared.annotations.Column;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.annotations.Reference;
import org.pjherring.mongogwt.shared.annotations.enums.ReferenceType;
import org.pjherring.mongogwt.shared.BaseDomainObject;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.exception.InvalidCollectionException;
import org.pjherring.mongogwt.shared.exception.LengthException;
import org.pjherring.mongogwt.shared.exception.NullableException;
import org.pjherring.mongogwt.shared.exception.RegexpException;
import org.pjherring.mongogwt.shared.exception.UniqueException;
import org.pjherring.mongogwt.shared.query.Query;
import org.pjherring.mongogwt.server.guice.DatabaseModule.EntityList;

/**
 *
 * @author pjherring
 */
public class Validate implements DoesValidate {

    private static final Logger LOG = Logger.getLogger("Validate");

    protected List<Class<? extends IsEntity>> entityList;
    protected DB mongoDb;

    @Inject
    public Validate(@EntityList List<Class<? extends IsEntity>> entityList, DB mongoDb) {
        this.entityList = entityList;
        this.mongoDb = mongoDb;
    }

    public void validatePojo(IsEntity pojoToValidate) {

        validateCollection(pojoToValidate.getClass());

        Entity domainCollection
            = pojoToValidate.getClass().getAnnotation(Entity.class);

        if (!entityList.contains(pojoToValidate.getClass())) {
            throw new InvalidCollectionException(domainCollection.name());
        }

        for (Method method : pojoToValidate.getClass().getMethods()) {
            if (method.isAnnotationPresent(Column.class)) {
                Object value;
                try {
                    value = method.invoke(pojoToValidate);
                } catch (Exception e) {
                    throw new Error(e);
                }

                if (method.isAnnotationPresent(Reference.class)
                    && method.getAnnotation(Reference.class).type().equals(ReferenceType.ONE_TO_ONE)) {

                    LOG.fine("Validation a reference");

                    BaseDomainObject baseDomainObject = (BaseDomainObject) value;

                    if (baseDomainObject.getId() == null) {
                        LOG.fine("Not persisted reference.");
                        validatePojo(baseDomainObject);
                    }

                } else {
                    validateColumn(
                        method.getAnnotation(Column.class),
                        value,
                        domainCollection
                    );
                }
            }
        }
    }

    public void validateColumn(Column columnAnnotation, Object value, Entity domainCollectionAnnotation) {
        if (!columnAnnotation.allowNull() && value == null) {
            LOG.warning("Column can not be null: " + columnAnnotation.name() +
                " for collection " + domainCollectionAnnotation.name());
            throw new NullableException(columnAnnotation.name());
        }

        if (value != null && columnAnnotation.unique()) {
            Query query = new Query()
                .start(columnAnnotation.name()).is(value);
            DBObject uniqueRefObject =
                BasicDBObjectBuilder.start(query.getQueryMap()).get();
            DBObject uniqueFound = mongoDb.getCollection(domainCollectionAnnotation.name())
                .findOne(uniqueRefObject);
            if (uniqueFound != null) {
                LOG.warning("Unique Exception for " + domainCollectionAnnotation.name()
                    + " on column " + columnAnnotation.name() + " with value: " + value);
                throw new UniqueException(columnAnnotation.name());
            }
        }

        if (value != null
            && columnAnnotation.maxLength() != -1
            && value.toString().length()
                > columnAnnotation.maxLength()) {
            throw new LengthException(columnAnnotation.name());
        }

        if (value != null
            && columnAnnotation.minLength() != -1
            && value.toString().length()
                < columnAnnotation.minLength()) {
            throw new LengthException(columnAnnotation.name());
        }

        if (value != null
            && !columnAnnotation.regexp().equals("")
            && !value.toString().matches(columnAnnotation.regexp())) {

            LOG.warning(
                "Column: " + columnAnnotation.name() + " with value: "
                + value.toString()
                + " failed regexp validation for regexp "
                + columnAnnotation.regexp()
            );

            throw new RegexpException(columnAnnotation.name());
        }
    }

    public void validateCollection(Class<? extends IsEntity> clazz) {
        if (clazz.isAnnotationPresent(Entity.class) && entityList.contains(clazz)) {
            validateCollection(clazz.getAnnotation(Entity.class));
        } else {
            throw new InvalidCollectionException(clazz.getName());
        }
    }

    public void validateCollection(Entity domainCollection) {
        if (domainCollection.doPersist()) {
            throw new InvalidCollectionException(domainCollection.name());
        }
    }

}

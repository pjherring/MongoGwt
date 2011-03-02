/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;


import org.pjherring.mongogwt.shared.domain.operation.Validate;
import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.pjherring.mongogwt.server.domain.cache.EntityMetaCache;
import org.pjherring.mongogwt.server.domain.cache.EntityMetaCache.ColumnMeta;
import org.pjherring.mongogwt.shared.IsEmbeddable;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.IsStorable;
import org.pjherring.mongogwt.shared.annotations.Column;
import org.pjherring.mongogwt.shared.annotations.Embedded;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.annotations.Unique;
import org.pjherring.mongogwt.shared.domain.operation.Read;
import org.pjherring.mongogwt.shared.domain.validator.Validator;
import org.pjherring.mongogwt.shared.exception.LengthException;
import org.pjherring.mongogwt.shared.exception.NotFoundException;
import org.pjherring.mongogwt.shared.exception.NullableException;
import org.pjherring.mongogwt.shared.exception.RegexpException;
import org.pjherring.mongogwt.shared.exception.UniqueException;
import org.pjherring.mongogwt.shared.exception.ValidationException;
import org.pjherring.mongogwt.shared.query.Query;

/**
 *
 * @author pjherring
 */
public class ValidateImpl implements Validate {

    private final static Logger LOG = Logger.getLogger(ValidateImpl.class.getName());
    protected Map<Class<? extends IsStorable>, Map<Column, Method>> translationMap ;
    protected Map<String, List<ValidationException>> validationErrorMap;
    protected DB mongoDB;
    protected EntityMetaCache entityMetaCache;
    protected Read read;

    @Inject
    public ValidateImpl(DB mongoDB, EntityMetaCache entityMetaCache, Read read) {
        translationMap = new HashMap<Class<? extends IsStorable>, Map<Column, Method>>();
        validationErrorMap = new HashMap<String, List<ValidationException>>();

        this.mongoDB = mongoDB;
        this.entityMetaCache = entityMetaCache;
        this.read = read;
    }

    @Override
    public void validate(IsStorable isStorable, boolean doThrowExceptions) {

        validationErrorMap.clear();

        List<Validator> validatorHooks
            = entityMetaCache.getValidatorList(isStorable.getClass());

        if (validatorHooks != null) {
            for (Validator validator : validatorHooks) {
                if (!validator.isValid(isStorable)) {
                    throw new ValidationException(validator.getClass());
                }
            }
        }

        List<String> uniqueColumnList = null;
        DBObject uniqueQuery = null;
        boolean doUniqueAnnotationValidation =
            isStorable.getClass().isAnnotationPresent(Unique.class);

        if (doUniqueAnnotationValidation) {
            Unique unique = isStorable.getClass().getAnnotation(Unique.class);
            uniqueQuery = new BasicDBObject();
            uniqueColumnList = Arrays.asList(unique.value());
        }

        Set<EntityMetaCache.ColumnMeta> columnMetaSet
            = entityMetaCache.getColumnMetaSet(isStorable.getClass());

        for (ColumnMeta columnMeta : columnMetaSet) {
            Method getter = columnMeta.getGetter();
            Object value = null;

            try {
                value = getter.invoke(isStorable);
            } catch (Exception e) {
                LOG.warning("Error in using getter: " + getter.getName() + " on " + isStorable.getClass().getName());
                throw new RuntimeException(e);
            }

            doNullableValidation(
                columnMeta.getColumnAnnotation(),
                value,
                doThrowExceptions
            );

            /*
             * If value is null we do not need to do any additional validation
             * checks. If we are at this point a null value is acceptable because
             * the null value passed the @see doNullableValidation.
             */
            if (value != null) {

                if (getter.isAnnotationPresent(Embedded.class) && IsEmbeddable.class.isAssignableFrom(value.getClass())) {
                    validate((IsStorable) value, doThrowExceptions);
                }

                doLengthValidation(
                    columnMeta.getColumnAnnotation(),
                    value,
                    doThrowExceptions
                );
                doRegexValidation(
                    columnMeta.getColumnAnnotation(), value,
                    doThrowExceptions
                );

                doColumnUniqueValidation(
                    columnMeta.getColumnAnnotation(),
                    isStorable.getClass(),
                    value,
                    doThrowExceptions
                );

                /*
                 * If uniqueColumnList is not null and this column is part of the
                 * unique constraint, put the value in the query object.
                 */

                boolean isUniqueColumn = doUniqueAnnotationValidation
                    && uniqueColumnList.contains(
                        columnMeta.getColumnAnnotation().name()
                    );

                if (isUniqueColumn) {
                    uniqueQuery.put(
                        columnMeta.getColumnAnnotation().name(),
                        value
                    );
                }

                /*
                 * Recursion. Checking validity of the embedded object.
                 */
                if (columnMeta.getGetter().isAnnotationPresent(Embedded.class)) {
                    validate((IsStorable) value);
                }
            }
        }

        if (doUniqueAnnotationValidation) {

            //query to test uniqueness
            DBCursor cursor = mongoDB
                .getCollection(isStorable.getClass().getAnnotation(Entity.class).name())
                .find(uniqueQuery);

            //if the cursor is bigger than 0, this violates the constraint
            if (cursor.size() > 0) {
                ValidationException validationException = new UniqueException();

                for (String uniqueColumn : uniqueColumnList) {
                    storeValidationException(uniqueColumn, validationException);
                }

                if (doThrowExceptions) {
                    throw validationException;
                }
            }
        }
    }

    @Override
    public void validate(IsStorable isStorable) throws ValidationException {
        validate(isStorable, true);
    }

    @Override
    public Map<String, List<ValidationException>> getValidationErrorMap() {
        return validationErrorMap;
    }

    /*
     * Checks for null constraint validation
     *
     * @param column The Column Annotation object
     * @param value The value of the column
     * @param whether or not to throw the exception or just store it
     *
     * @throws NullableException
     */
    private void doNullableValidation(Column column, Object value, boolean doThrowException) {

        if (value == null && !column.allowNull()) {
            ValidationException validationException =
                new NullableException(column.name());

            storeValidationException(column.name(), validationException);
            if (doThrowException) {
                throw validationException;
            }
        }
    }

    /*
     * Checks for length constraint validation
     *
     * @param column The column annotation object
     * @param value The value of the column
     * @param doThrowException Whether or not to throw the exception or store it
     *
     * @throws LengthException
     */
    private void doLengthValidation(Column column, Object value, boolean doThrowException) {

        if (value != null && column.minLength() > -1
            && String.class.isAssignableFrom(value.getClass())
            && ((String) value).length() < column.minLength()) {

            ValidationException validationException
                = new LengthException(column.name());
            storeValidationException(column.name(), validationException);
            if (doThrowException) {
                throw validationException;
            }
        }

        if (value != null && column.maxLength() > -1
            && String.class.isAssignableFrom(value.getClass())
            && ((String) value).length() > column.maxLength()) {

            ValidationException validationException
                = new LengthException(column.name());

            storeValidationException(column.name(), validationException);

            if (doThrowException) {
                throw validationException;
            }
        }
    }

    /*
     * Checks for regexp constraint validation
     *
     * @param column The column annotation object
     * @param value The value of the column
     * @param doThrowException Whether or not to throw the exception or store it
     *
     * @throws RegexpException
     */
    private void doRegexValidation(Column column, Object value, boolean doThrowException) {
        if (value != null && !column.regexp().equals("")
            && String.class.isAssignableFrom(value.getClass())) {

            String regex = column.regexp();

            /*
             * We do this to allow regular expressions to match strings
             * that have the regular expression and more. I.e. if the regex
             * is "\d{2,}" than it should match anything with two sequential
             * digits
             */
            if (regex.startsWith("^")) {
                regex = regex.substring(1);
            } else {
                regex = ".*" + regex;
            }

            if (regex.endsWith("$")) {
                regex = regex.substring(0, regex.length() - 1);
            } else {
                regex+= ".*";
            }

            Pattern pattern = Pattern.compile(regex);

            if (!pattern.matcher((String) value).matches()) {
                ValidationException validationException
                    = new RegexpException(column.name());
                storeValidationException(column.name(), validationException);
                if (doThrowException) {
                    throw validationException;
                }
            }
        }
    }

    /*
     * Checks for unique constraint validation
     *
     * @param column The column annotation object
     * @param value The value of the column
     * @param doThrowException Whether or not to throw the exception or store it
     *
     * @throws UniqueException
     */
    private void doColumnUniqueValidation (
        Column column,
        Class<? extends IsStorable> clazz,
        Object value,
        boolean doThrowException) {

        if (value != null && column.unique() && clazz.isAnnotationPresent(Entity.class)) {

            Entity entity = clazz.getAnnotation(Entity.class);
            Query query = new Query().start(column.name()).is(value);

            try {
                IsEntity isEntity = read.findOne(
                    query,
                    (Class<? extends IsEntity>) clazz,
                    false
                );
            } catch (NotFoundException e) {
                return ;
            }

            ValidationException validationException
                = new UniqueException(column.name());
            storeValidationException(column.name(), validationException);

            if (doThrowException) {
                throw validationException;
            }
        }
    }

    private void storeValidationException(String columnName, ValidationException exception) {
        if (!validationErrorMap.containsKey(columnName)) {
            validationErrorMap.put(
                columnName,
                new ArrayList<ValidationException>()
            );
        }

        List<ValidationException> list = validationErrorMap.get(columnName);
        list.add(exception);
    }

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;


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
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.pjherring.mongogwt.shared.IsEmbeddable;
import org.pjherring.mongogwt.shared.IsStorable;
import org.pjherring.mongogwt.shared.annotations.Column;
import org.pjherring.mongogwt.shared.annotations.Embedded;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.annotations.Unique;
import org.pjherring.mongogwt.shared.exception.LengthException;
import org.pjherring.mongogwt.shared.exception.NullableException;
import org.pjherring.mongogwt.shared.exception.RegexpException;
import org.pjherring.mongogwt.shared.exception.UniqueException;
import org.pjherring.mongogwt.shared.exception.ValidationException;

/**
 *
 * @author pjherring
 */
public class ValidateImpl implements Validate {

    private final static Logger LOG = Logger.getLogger(ValidateImpl.class.getName());
    protected Map<Class<? extends IsStorable>, Map<Column, Method>> translationMap ;
    protected Map<String, List<ValidationException>> validationErrorMap;
    protected DB mongoDB;

    @Inject
    public ValidateImpl(DB mongoDB) {
        translationMap = new HashMap<Class<? extends IsStorable>, Map<Column, Method>>();
        validationErrorMap = new HashMap<String, List<ValidationException>>();
        this.mongoDB = mongoDB;
    }

    public void validate(IsStorable isStorable, boolean doThrowExceptions) {

        if (translationMap.containsKey(isStorable.getClass())) {
            //reset the validation error map to pick up exceptions from the
            //latest validation
            validationErrorMap.clear();
            doValidationWithMap(isStorable, translationMap.get(isStorable.getClass()), doThrowExceptions);
        } else {
            translationMap.put(isStorable.getClass(), createTranslationMap(isStorable));
            validate(isStorable, doThrowExceptions);
        }
    }

    public void validate(IsStorable isStorable) throws ValidationException {
        validate(isStorable, true);
    }

    public Map<String, List<ValidationException>> getValidationErrorMap() {
        return validationErrorMap;
    }

    /*
     * TODO: REFACTOR
     *
     * This method was tested in PojoToDBObject. There should be some refactoring
     * to make forming this map a seperate class. So it is only in one place.
     */
    private <T extends IsStorable> Map<Column, Method> createTranslationMap(T isStorable) {
        Map<Column, Method> entityTranslationMap =
            new HashMap<Column, Method>();

        for (Method method : isStorable.getClass().getMethods()) {
            /*
             * We want this method if its a getter meaning no parameters
             * and annotated with column.
             */
            if (method.isAnnotationPresent(Column.class) && method.getParameterTypes().length == 0) {
                Column column = method.getAnnotation(Column.class);
                entityTranslationMap.put(column, method);
            }
        }

        return entityTranslationMap;
    }

    private void doValidationWithMap(
        IsStorable isStorable,
        Map<Column, Method> columnNameGetterMap,
        boolean doThrowExceptions) {
        List<String> uniqueColumnList = null;
        DBObject uniqueQuery = null;

        /*
         * If there is a unique annotation on this Entity we need to construct
         * a DBObject query so we can see if this Entity violates a Unique
         * Constraint.
         *
         */
        if (isStorable.getClass().isAnnotationPresent(Unique.class)) {
            Unique unique = isStorable.getClass().getAnnotation(Unique.class);
            uniqueQuery = new BasicDBObject();
            uniqueColumnList = Arrays.asList(unique.value());
        }

        for (Column column : columnNameGetterMap.keySet()) {
            Method getter = columnNameGetterMap.get(column);
            Object value = null;

            try {
                value = getter.invoke(isStorable);
            } catch (Exception e) {
                LOG.warning("Error in using getter: " + getter.getName() + " on " + isStorable.getClass().getName());
                throw new RuntimeException(e);
            }

            doNullableValidation(column, value, doThrowExceptions);

            if (getter.isAnnotationPresent(Embedded.class) && IsEmbeddable.class.isAssignableFrom(value.getClass())) {
                validate((IsStorable) value, doThrowExceptions);
            }

            doLengthValidation(column, value, doThrowExceptions);
            doRegexValidation(column, value, doThrowExceptions);
            doColumnUniqueValidation(column, isStorable.getClass(), value, doThrowExceptions);

            /*
             * If uniqueColumnList is not null and this column is part of the
             * unique constraint, put the value in the query object.
             */
            if (uniqueColumnList != null && uniqueColumnList.contains(column.name())) {
                uniqueQuery.put(column.name(), value);
            }
        }

        /*
         * If our list is null, we will check for the unique constraint violation
         * here.
         */
        if (uniqueColumnList != null) {
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

    private void doColumnUniqueValidation(
        Column column,
        Class<? extends IsStorable> clazz,
        Object value,
        boolean doThrowException) throws UniqueException {
        if (value != null && column.unique() && clazz.isAnnotationPresent(Entity.class)) {
            Entity entity = clazz.getAnnotation(Entity.class);
            DBObject queryObject = new BasicDBObject();
            queryObject.put(column.name(), value);
            DBCursor cursor = mongoDB.getCollection(entity.name()).find(queryObject);

            if (cursor.size() > 0) {
                ValidationException validationException
                    = new UniqueException(column.name());
                storeValidationException(column.name(), validationException);
                if (doThrowException) {
                    throw validationException;
                }

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

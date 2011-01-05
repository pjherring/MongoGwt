/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;

import com.google.inject.Inject;
import com.mongodb.DB;
import java.lang.reflect.Method;
import java.util.logging.Logger;
import org.pjherring.mongogwt.shared.domain.operation.DoesValidate;

/**
 *
 * @author pjherring
 */
public abstract class BaseDatabaseOperation {

    private static final Logger LOG = Logger.getLogger("BaseDatabaseOperation");
    protected DB database;
    protected DoesValidate validator;

    @Inject
    public void setValidate(DoesValidate validator) {
        this.validator = validator;
    }

    @Inject
    public void setDatabase(
        DB database) {
        this.database = database;
    }

    protected DB getDatabase() {
        return database;
    }

    protected Method getMethodFromString(String methodName, Class clazz) {

        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }

        throw new RuntimeException("Method " + methodName + " for class "
            + clazz.getSimpleName() + " does not exist.");
    }

    protected Method getGetterFromColumnName(String columnName, Class clazz) {
        return getMethodFromString(
            "get" + Character.toUpperCase(columnName.charAt(0))
                + columnName.substring(1),
            clazz
        );
    }
}
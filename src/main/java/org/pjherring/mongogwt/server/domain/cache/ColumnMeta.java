/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.cache;


import java.lang.reflect.Method;
import java.util.logging.Logger;
import org.pjherring.mongogwt.shared.annotations.Column;

/**
 *
 * @author pjherring
 */
public class ColumnMeta {

    private final static Logger LOG = Logger.getLogger(ColumnMeta.class.getName());

    private Method getter;
    private Method setter;
    private Column columnAnnotation;

    public Method getGetter() {
        return getter;
    }

    public void setGetter(Method getter) {
        this.getter = getter;
    }

    public Method getSetter() {
        return setter;
    }

    public void setSetter(Method setter) {
        this.setter = setter;
    }

    public Column getColumnAnnotation() {
        return columnAnnotation;
    }

    public void setColumnAnnotation(Column columnAnnotation) {
            this.columnAnnotation = columnAnnotation;
        }

}

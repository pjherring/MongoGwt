/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.cache;


import java.lang.reflect.Method;
import java.util.logging.Logger;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.annotations.Reference;

/**
 *
 * @author pjherring
 */
public class ReferenceMeta {

    private final static Logger LOG = Logger.getLogger(ReferenceMeta.class.getName());

    private Class<? extends IsEntity> referencedClass;
    private Reference reference;
    private Method getter;
    private Method setter;

    public Class<? extends IsEntity> getReferencedClass() {
        return referencedClass;
    }

    public void setReferencedClass(Class<? extends IsEntity> referencedClass) {
        this.referencedClass = referencedClass;
    }

    public Reference getReference() {
        return reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

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
}

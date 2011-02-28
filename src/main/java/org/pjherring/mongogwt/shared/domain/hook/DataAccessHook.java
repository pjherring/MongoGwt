/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.domain.hook;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author pjherring
 */
public class DataAccessHook {


    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Hooks {
        Class[] preCreate() default {};
        Class[] postCreate() default {};
        Class[] preRead() default {};
        Class[] postRead() default {};
        Class[] preUpdate() default {};
        Class[] postUpdate() default {};
        Class[] preDelete() default {};
        Class[] postDelete() default {};
    }

    public enum When {
        PRE, POST;
    }

    public enum What {
        CREATE, READ, UPDATE, DELETE;
    }
}

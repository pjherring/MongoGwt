/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author pjherring
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Column {
    String name() default "";
    int minLength() default -1;
    int maxLength() default -1;
    boolean allowNull() default true;
    String regexp() default "";
    boolean unique() default false;
    boolean isTransient() default false;
}

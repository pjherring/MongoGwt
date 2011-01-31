/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.annotations;

import org.pjherring.mongogwt.shared.annotations.enums.ReferenceType;
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
public @interface Reference {
    ReferenceType type();
    String joinColumn() default "";
    String managedBy() default "";
    boolean doCascadeDelete() default false;
}


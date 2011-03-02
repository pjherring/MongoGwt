/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.exception;

import org.pjherring.mongogwt.shared.domain.validator.Validator;

/**
 *
 * @author pjherring
 */
public class ValidationException extends RuntimeException {
    public ValidationException() {}

    public ValidationException(Class<? extends Validator> clazz) {
        super("The validator " + clazz.getName() + " marked this entity as not valid.");
    }

    public ValidationException(String msg) {
        super(msg);
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.exceptions;

/**
 *
 * @author pjherring
 */
public class ValidationException extends RuntimeException {
    public ValidationException() {}

    public ValidationException(String msg) {
        super(msg);
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.exceptions;

/**
 *
 * @author pjherring
 */
public class NullableException extends ConstraintException {

    public NullableException(String columnName) {
        super("Null column exception", columnName);
    }
}

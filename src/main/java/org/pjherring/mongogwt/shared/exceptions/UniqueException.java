/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.exceptions;

/**
 *
 * @author pjherring
 */
public class UniqueException extends ConstraintException {
    public UniqueException() {}

    public UniqueException(String columnName) {
        super("Unique column exception", columnName);
    }

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.exception;

/**
 *
 * @author pjherring
 */
public class LengthException extends ConstraintException {

    public LengthException() {}

    public LengthException(String columnName) {
        super("Invalid length.", columnName);
    }
}

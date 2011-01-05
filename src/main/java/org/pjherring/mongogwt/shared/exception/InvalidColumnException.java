/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.exception;

/**
 *
 * @author pjherring
 */
public class InvalidColumnException extends ValidationException {

    protected String columnName;

    public InvalidColumnException() {}

    public InvalidColumnException(String columnName) {
        super("Invalid column name.");
        this.columnName = columnName;
    }
}

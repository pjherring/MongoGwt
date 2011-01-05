/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.exception;

/**
 *
 * @author pjherring
 */
public class ConstraintException extends ValidationException {

    protected String columnName;

    public ConstraintException() {}

    public ConstraintException(String msg, String columnName) {
        super(msg);
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }
}

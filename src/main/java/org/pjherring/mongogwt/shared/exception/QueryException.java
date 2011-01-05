/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.exception;

/**
 *
 * @author pjherring
 */
public class QueryException extends RuntimeException {

    protected String columnName;
    protected String collectionName;

    /**
     * Creates a new instance of <code>QueryException</code> without detail message.
     */
    public QueryException() {
    }


    /**
     * Constructs an instance of <code>QueryException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public QueryException(String msg) {
        super(msg);
    }

    public QueryException(String columnName, String collectionName) {
        super(
            "Column \"" + columnName +
            "\" is not valid for querying on collection " + collectionName
        );
    }

    public String getColumnName() {
        return columnName;
    }

    public String getCollectionName() {
        return collectionName;
    }

}

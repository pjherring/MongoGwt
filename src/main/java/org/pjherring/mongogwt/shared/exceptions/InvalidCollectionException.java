/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.exceptions;

/**
 *
 * @author pjherring
 */
public class InvalidCollectionException extends ValidationException {

    protected String collectionName;

    public InvalidCollectionException(String collectionName) {
        super("You have tried to do a database value on an invalid collection name.");
        this.collectionName = collectionName;
    }

    public String getCollectionName() {
        return collectionName;
    }
}

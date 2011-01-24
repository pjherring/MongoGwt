/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.exception;

/**
 *
 * @author pjherring
 */
public class AlreadyPersistedException extends ValidationException {

    /**
     * Creates a new instance of <code>AlreadyPersistedException</code> without detail message.
     */
    public AlreadyPersistedException() {
    }


    /**
     * Constructs an instance of <code>AlreadyPersistedException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public AlreadyPersistedException(String msg) {
        super(msg);
    }
}

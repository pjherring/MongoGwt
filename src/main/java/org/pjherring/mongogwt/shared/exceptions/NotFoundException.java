/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.exceptions;

/**
 *
 * @author pjherring
 */
public class NotFoundException extends RuntimeException {

    /**
     * Creates a new instance of <code>NotFoundException</code> without detail message.
     */
    public NotFoundException() {
    }


    /**
     * Constructs an instance of <code>NotFoundException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public NotFoundException(String msg) {
        super(msg);
    }
}

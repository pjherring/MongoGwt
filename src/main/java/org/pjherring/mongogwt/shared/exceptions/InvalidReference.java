/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.exceptions;

/**
 *
 * @author pjherring
 */
public class InvalidReference extends RuntimeException {

    /**
     * Creates a new instance of <code>InvalidReference</code> without detail message.
     */
    public InvalidReference() {
    }


    /**
     * Constructs an instance of <code>InvalidReference</code> with the specified detail message.
     * @param msg the detail message.
     */
    public InvalidReference(String msg) {
        super(msg);
    }
}

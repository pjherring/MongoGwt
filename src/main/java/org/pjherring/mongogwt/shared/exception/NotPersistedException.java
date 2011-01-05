/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.exception;


import java.util.logging.Logger;

/**
 *
 * @author pjherring
 */
public class NotPersistedException extends RuntimeException {

    private final static Logger LOG = Logger.getLogger(NotPersistedException.class.getName());

    public NotPersistedException() {
        super("Can not perform operation on an entity that is not persisted.");
    }

}

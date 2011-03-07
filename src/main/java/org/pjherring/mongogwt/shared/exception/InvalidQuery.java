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
public class InvalidQuery extends RuntimeException {

    public InvalidQuery() {
        super("Invalid query.");
    }

    public InvalidQuery(String reason) {
        super("Invalid query: " + reason);
    }

}

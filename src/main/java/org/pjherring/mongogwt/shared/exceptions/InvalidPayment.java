/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.exceptions;

/**
 *
 * @author pjherring
 */
public class InvalidPayment extends RuntimeException {

    public InvalidPayment(String errors) {
        super("ERRORS: " + errors);
    }

}

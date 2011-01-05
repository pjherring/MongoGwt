/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.exceptions;

/**
 *
 * @author pjherring
 */
public class RegexpException extends ConstraintException {

    public RegexpException(String columnName) {
        super("A column falied a regular expression test.", columnName);
    }
}

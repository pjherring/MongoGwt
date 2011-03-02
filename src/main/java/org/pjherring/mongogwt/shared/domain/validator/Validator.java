/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.domain.validator;


import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.IsStorable;

/**
 *
 * @author pjherring
 */
public abstract class Validator<T extends IsStorable> {
    public abstract boolean isValid(T entity);
}

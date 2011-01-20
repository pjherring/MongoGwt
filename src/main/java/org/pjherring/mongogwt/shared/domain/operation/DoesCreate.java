/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.domain.operation;

import org.pjherring.mongogwt.shared.IsEntity;

/**
 *
 * @author pjherring
 */
public interface DoesCreate {
    void doCreate(IsEntity domainObject);
}

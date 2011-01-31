/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.domain.operation;

import com.google.inject.ImplementedBy;
import org.pjherring.mongogwt.server.domain.operation.UpdateImpl;
import org.pjherring.mongogwt.shared.IsEntity;

/**
 *
 * @author pjherring
 */
@ImplementedBy(UpdateImpl.class)
public interface Update {

    void doUpdate(IsEntity entity);

}

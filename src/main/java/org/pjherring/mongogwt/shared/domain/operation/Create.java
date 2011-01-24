/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.domain.operation;

import com.google.inject.ImplementedBy;
import org.pjherring.mongogwt.server.domain.operation.CreateImpl;
import org.pjherring.mongogwt.shared.IsEntity;

/**
 *
 * @author pjherring
 */
@ImplementedBy(CreateImpl.class)
public interface Create {
    void doCreate(IsEntity entityToPersist);
}

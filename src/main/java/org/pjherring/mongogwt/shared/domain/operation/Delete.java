/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.domain.operation;

import com.google.inject.ImplementedBy;
import org.pjherring.mongogwt.server.domain.operation.DeleteImpl;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.query.Query;

/**
 *
 * @author pjherring
 */
@ImplementedBy(DeleteImpl.class)
public interface Delete {

    void delete(IsEntity entity);
    void delete(Query query);
}

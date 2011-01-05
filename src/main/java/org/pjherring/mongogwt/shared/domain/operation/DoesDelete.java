/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.domain.operation;

import org.pjherring.mongogwt.shared.query.Query;
import org.pjherring.mongogwt.shared.IsDomainObject;

/**
 *
 * @author pjherring
 */
public interface DoesDelete {
    <T extends IsDomainObject> void doDelete(Query query, Class<T> type);
}

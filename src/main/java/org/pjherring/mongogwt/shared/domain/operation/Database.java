/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.domain.operation;

import org.pjherring.mongogwt.shared.IsDomainObject;
import java.util.List;
import org.pjherring.mongogwt.shared.exceptions.ValidationException;
import org.pjherring.mongogwt.shared.query.Query;

/**
 *
 * @author pjherring
 */
public interface Database {
    void doCreate(IsDomainObject domainObject) throws ValidationException;
    void doUpdate(IsDomainObject domainObject) throws ValidationException;
    <T extends IsDomainObject> List<T>
        find(Query query, Class<T> type, boolean doFanOut);
    <T extends IsDomainObject> T
        findOne(Query query, Class<T> type, boolean doFanOut);
    void doDelete(Query query, Class<? extends IsDomainObject> type);
    <T extends IsDomainObject> T refresh(IsDomainObject domainObject, Class<T> type);
    <T extends IsDomainObject> Long count(Query query, Class<T> type);

}
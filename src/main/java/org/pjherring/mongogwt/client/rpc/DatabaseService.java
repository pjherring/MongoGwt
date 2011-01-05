/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.client.rpc;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.List;
import org.pjherring.mongogwt.shared.IsDomainObject;
import org.pjherring.mongogwt.shared.exceptions.NotFoundException;
import org.pjherring.mongogwt.shared.exceptions.QueryException;
import org.pjherring.mongogwt.shared.exceptions.ValidationException;
import org.pjherring.mongogwt.shared.query.Query;

/**
 *
 * @author pjherring
 */
@RemoteServiceRelativePath("data")
public interface DatabaseService extends RemoteService {
    IsDomainObject create(IsDomainObject domainObject) throws ValidationException;
    List<IsDomainObject> find(Query query, String type, boolean doFanOut) throws NotFoundException, QueryException;
    IsDomainObject findOne(Query query, String type, boolean doFanOut) throws NotFoundException, QueryException;
    IsDomainObject update(IsDomainObject domainObject) throws ValidationException, QueryException;
    void delete(Query query, String type) throws NotFoundException;
    IsDomainObject refresh(IsDomainObject domainObject, String type) throws NotFoundException, QueryException;
    Long count(Query query, String type);
}

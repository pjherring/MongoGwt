/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.client.rpc;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.List;
import org.pjherring.mongogwt.shared.IsDomainObject;
import org.pjherring.mongogwt.shared.exception.ConstraintException;
import org.pjherring.mongogwt.shared.exception.InvalidCollectionException;
import org.pjherring.mongogwt.shared.exception.InvalidColumnException;
import org.pjherring.mongogwt.shared.exception.InvalidReference;
import org.pjherring.mongogwt.shared.exception.LengthException;
import org.pjherring.mongogwt.shared.exception.NotFoundException;
import org.pjherring.mongogwt.shared.exception.NotPersistedException;
import org.pjherring.mongogwt.shared.exception.NullableException;
import org.pjherring.mongogwt.shared.exception.QueryException;
import org.pjherring.mongogwt.shared.exception.RegexpException;
import org.pjherring.mongogwt.shared.exception.UniqueException;
import org.pjherring.mongogwt.shared.exception.ValidationException;
import org.pjherring.mongogwt.shared.query.Query;

/**
 *
 * @author pjherring
 */
@RemoteServiceRelativePath("data")
public interface DatabaseService extends RemoteService {
    IsDomainObject create(IsDomainObject domainObject)
        throws NullableException, ConstraintException, InvalidCollectionException,
        InvalidColumnException, ValidationException, InvalidReference,
        LengthException, RegexpException, UniqueException;
    List<IsDomainObject> find(Query query, String type, boolean doFanOut) throws NotFoundException, QueryException;
    IsDomainObject findOne(Query query, String type, boolean doFanOut) throws NotFoundException, QueryException;
    IsDomainObject update(IsDomainObject domainObject) throws ValidationException, QueryException, NotPersistedException;
    void delete(Query query, String type) throws NotFoundException, NotPersistedException;
    void delete(IsDomainObject domainObject) throws NotPersistedException;
    IsDomainObject refresh(IsDomainObject domainObject, String type) throws NotFoundException, QueryException, NotPersistedException;
    Long count(Query query, String type);
}

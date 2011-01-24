/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.client.rpc;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.List;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.exception.ConstraintException;
import org.pjherring.mongogwt.shared.exception.InvalidEntity;
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
    IsEntity create(IsEntity domainObject)
        throws NullableException, ConstraintException, InvalidEntity,
        InvalidColumnException, ValidationException, InvalidReference,
        LengthException, RegexpException, UniqueException;
    List<IsEntity> find(Query query, String type, boolean doFanOut) throws NotFoundException, QueryException;
    IsEntity findOne(Query query, String type, boolean doFanOut) throws NotFoundException, QueryException;
    IsEntity update(IsEntity domainObject) throws ValidationException, QueryException, NotPersistedException;
    void delete(Query query, String type) throws NotFoundException, NotPersistedException;
    void delete(IsEntity domainObject) throws NotPersistedException;
    IsEntity refresh(IsEntity domainObject, String type) throws NotFoundException, QueryException, NotPersistedException;
    Long count(Query query, String type);
}

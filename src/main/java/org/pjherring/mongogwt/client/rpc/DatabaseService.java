/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.client.rpc;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.List;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.exception.NotFoundException;
import org.pjherring.mongogwt.shared.exception.NotPersistedException;
import org.pjherring.mongogwt.shared.exception.ValidationException;
import org.pjherring.mongogwt.shared.query.Query;

/**
 *
 * @author pjherring
 */
@RemoteServiceRelativePath("data")
public interface DatabaseService extends RemoteService {

    IsEntity create(IsEntity domainObject) throws ValidationException;
    List<IsEntity> find(Query query, String type, boolean doFanOut) throws NotFoundException;
    IsEntity findOne(Query query, String type, boolean doFanOut) throws NotFoundException;
    IsEntity findById(String id, String type, boolean doFanOut) throws NotFoundException;
    IsEntity update(IsEntity domainObject) throws ValidationException, NotPersistedException;
    void delete(Query query, String type) throws NotFoundException, NotPersistedException;
    void delete(IsEntity entity) throws NotFoundException, NotPersistedException;
    IsEntity refresh(IsEntity domainObject, String type) throws NotFoundException, NotPersistedException;
}

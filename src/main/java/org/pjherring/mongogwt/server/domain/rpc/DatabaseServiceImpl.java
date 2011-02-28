/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.rpc;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.pjherring.mongogwt.client.rpc.DatabaseService;
import org.pjherring.mongogwt.server.domain.operation.Database;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.exception.NotFoundException;
import org.pjherring.mongogwt.shared.exception.NotPersistedException;
import org.pjherring.mongogwt.shared.exception.ValidationException;
import org.pjherring.mongogwt.shared.query.Query;

/**
 * This class handles the RPC calls. It then passes these calls along to to
 * @see org.pjherring.mongogwt.shared.domain.operation.Database
 * @author pjherring
 */
@Singleton
public class DatabaseServiceImpl extends RemoteServiceServlet implements DatabaseService {

    protected Database database;

    @Inject
    public DatabaseServiceImpl(
        Database database
        ) {
        this.database = database;
    }

    @Override
    public IsEntity create(IsEntity domainObject) throws ValidationException {
        return database.create(domainObject);
    }

    @Override
    public List<IsEntity> find(Query query, String type, boolean doFanOut) throws NotFoundException {
        return database.find(query, getClassFromString(type), doFanOut);
    }

    @Override
    public IsEntity findOne(Query query, String type, boolean doFanOut) throws NotFoundException {
        return database.findOne(query, getClassFromString(type), doFanOut);
    }

    @Override
    public IsEntity findById(String id, String type, boolean doFanOut) throws NotFoundException {
        return database.findById(id, getClassFromString(type), doFanOut);
    }

    @Override
    public IsEntity update(IsEntity domainObject) throws ValidationException {
        return database.update(domainObject);
    }

    @Override
    public void delete(Query query, String type) throws NotFoundException, NotPersistedException {
        database.delete(query, getClassFromString(type));
    }

    @Override
    public void delete(IsEntity domainObject) throws NotPersistedException {
        database.delete(domainObject);
    }

    @Override
    public IsEntity refresh(IsEntity domainObject, String type) throws NotFoundException, NotPersistedException {
        return database.findById(domainObject.getId(), getClassFromString(type), true);
    }

    protected Class getClassFromString(String className) {
        try {
            return Class.forName(className);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}
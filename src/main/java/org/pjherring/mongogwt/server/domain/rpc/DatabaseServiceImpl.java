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
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.domain.operation.Database;
import org.pjherring.mongogwt.shared.exception.NotFoundException;
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
    public DatabaseServiceImpl(Database database) {
        this.database = database;
    }

    public IsEntity create(IsEntity domainObject) {
        database.create(domainObject);
        return domainObject;
    }

    public List<IsEntity> find(Query query, String type, boolean doFanOut) {
        Class typeAsClass = getClassFromString(type);

        List<IsEntity> results = database.find(query, typeAsClass, doFanOut);
        return results;
    }

    public IsEntity findOne(Query query, String type, boolean doFanOut) {
        Class typeAsClass = getClassFromString(type);
        return database.findOne(query, typeAsClass, doFanOut);
    }

    public IsEntity update(IsEntity domainObject) {
        database.update(domainObject);
        return domainObject;
    }

    public void delete(Query query, String type) {
        database.delete(query, getClassFromString(type));
    }

    public void delete(IsEntity domainObject) {
        database.delete(domainObject);
    }

    public IsEntity refresh(IsEntity domainObject, String type) throws NotFoundException {
        return database.refresh(domainObject, getClassFromString(type));
    }

    public Long count(Query query, String type) {
        return database.count(query, getClassFromString(type));
    }


    protected Class getClassFromString(String className) {
        try {
            return Class.forName(className);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}
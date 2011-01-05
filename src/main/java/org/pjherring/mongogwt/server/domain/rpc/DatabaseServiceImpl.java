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
import org.pjherring.mongogwt.shared.IsDomainObject;
import org.pjherring.mongogwt.shared.domain.operation.Database;
import org.pjherring.mongogwt.shared.exceptions.NotFoundException;
import org.pjherring.mongogwt.shared.query.Query;

/**
 *
 * @author pjherring
 */
@Singleton
public class DatabaseServiceImpl extends RemoteServiceServlet implements DatabaseService {

    protected Database database;

    @Inject
    public DatabaseServiceImpl(Database database) {
        this.database = database;
    }

    public IsDomainObject create(IsDomainObject domainObject) {
        database.doCreate(domainObject);
        return domainObject;
    }

    public List<IsDomainObject> find(Query query, String type, boolean doFanOut) {
        Class typeAsClass = getClassFromString(type);

        List<IsDomainObject> results = database.find(query, typeAsClass, doFanOut);
        return results;
    }

    public IsDomainObject findOne(Query query, String type, boolean doFanOut) {
        Class typeAsClass = getClassFromString(type);
        return database.findOne(query, typeAsClass, doFanOut);
    }

    public IsDomainObject update(IsDomainObject domainObject) {
        database.doUpdate(domainObject);
        return domainObject;
    }

    public void delete(Query query, String type) {
        database.doDelete(query, getClassFromString(type));
    }

    public IsDomainObject refresh(IsDomainObject domainObject, String type) throws NotFoundException {
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
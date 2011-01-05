/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain;

import com.google.inject.Inject;
import com.mongodb.DB;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.pjherring.mongogwt.server.domain.hook.DataAccessType;
import org.pjherring.mongogwt.server.domain.hook.BaseDataAccessHook;
import org.pjherring.mongogwt.server.domain.hook.WhenDataAccess;
import org.pjherring.mongogwt.shared.IsDomainObject;
import org.pjherring.mongogwt.shared.domain.operation.DoesCreate;
import org.pjherring.mongogwt.shared.domain.operation.DoesDelete;
import org.pjherring.mongogwt.shared.domain.operation.DoesRead;
import org.pjherring.mongogwt.shared.domain.operation.DoesUpdate;
import org.pjherring.mongogwt.shared.domain.operation.DoesValidate;
import org.pjherring.mongogwt.shared.domain.operation.Database;
import org.pjherring.mongogwt.shared.exception.NotPersistedException;
import org.pjherring.mongogwt.shared.query.Query;

/**
 *
 * @author pjherring
 */
public class DatabaseImpl implements Database {

    private static final Logger LOG = Logger.getLogger("IsDatabaseImpl");

    protected List<String> collectionList;
    protected DB database;

    protected DoesCreate creator;
    protected DoesRead reader;
    protected DoesUpdate updater;
    protected DoesDelete deleter;
    protected Map<Class<? extends IsDomainObject>, Map<DataAccessType, Map<WhenDataAccess, List<BaseDataAccessHook>>>> dataAccessMap;

    @Inject
    public DatabaseImpl(
        DoesCreate creator,
        DoesRead reader,
        DoesUpdate updater,
        DoesDelete deleter,
        DoesValidate validator,
        Map<Class<? extends IsDomainObject>, Map<DataAccessType, Map<WhenDataAccess, List<BaseDataAccessHook>>>> dataAccessMap) {

        this.creator = creator;
        this.reader = reader;
        this.updater = updater;
        this.deleter = deleter;
        this.dataAccessMap = dataAccessMap;

    }

    /*
     * Creates the @param domainObject
     */
    public  void create(IsDomainObject domainObject) {
        doAccess(domainObject.getClass(), domainObject, null, DataAccessType.CREATE, WhenDataAccess.BEFORE);
        creator.doCreate(domainObject);
        doAccess(domainObject.getClass(), domainObject, null, DataAccessType.CREATE, WhenDataAccess.AFTER);
    }

    /*
     * Updates the @param domainObject
     */
    public void update(IsDomainObject domainObject) {
        checkPersisted(domainObject);

        doAccess(domainObject.getClass(), domainObject, null, DataAccessType.UPDATE, WhenDataAccess.BEFORE);
        updater.doUpdate(domainObject);
        doAccess(domainObject.getClass(), domainObject, null, DataAccessType.UPDATE, WhenDataAccess.AFTER);
    }

    /*
     * Finds domain objects according to @param query. @param doFanOut indicates
     * whether or not we should include references in the result objects. I.e.
     * if a person has multiple email address and there are two entity's "Person"
     * and "Email", doFanOut = true will grab all the related "Email" entities. If false,
     * the opposite.
     */
    public <T extends IsDomainObject> List<T> find(Query query, Class<T> type, boolean doFanOut) {
        doAccess(type, null, query, DataAccessType.READ, WhenDataAccess.BEFORE);
        List<T> toReturn = reader.find(query, type, doFanOut);

        for (T domainObject : toReturn) {
            doAccess(type, domainObject, query, DataAccessType.READ, WhenDataAccess.AFTER);
        }

        return toReturn;
    }

    /*
     * @param query how to query the db
     * @param type the Class of the domain object on which to query
     * @param doFanOut should references be included in result
     */
    public <T extends IsDomainObject> T findOne(Query query, Class<T> type, boolean doFanOut) {
        T toReturn;
        doAccess(type, null, query, DataAccessType.READ, WhenDataAccess.BEFORE);

        if (query.getQueryMap().containsKey("_id")
            && query.getQueryMap().get("_id") != null) {
            toReturn = reader.findById(
                query.getQueryMap().get("_id").toString(),
                type,
                doFanOut
            );
        }

        toReturn = reader.findOne(query, type, doFanOut);
        doAccess(type, toReturn, query, DataAccessType.READ, WhenDataAccess.AFTER);

        return toReturn;
    }

    /*
     * @param query queries the database to find objects to delete
     * @param type Class of the domain object to delete.
     */
    public void delete(Query query, Class<? extends IsDomainObject> type) {
        doAccess(type, null, query, DataAccessType.DELETE, WhenDataAccess.BEFORE);
        deleter.doDelete(query, type);
        doAccess(type, null, query, DataAccessType.DELETE, WhenDataAccess.AFTER);
    }

    /*
     * domainObject The object to delete.
     */
    public <T extends IsDomainObject> void delete(T domainObject) {
        checkPersisted(domainObject);

        delete(
            new Query().start("_id").is(domainObject.getId()),
            domainObject.getClass()
        );
    }

    /*
     * @param domainObject The object to refresh.
     * @param type The class of @param domainObject
     */
    public <T extends IsDomainObject> T refresh(
        IsDomainObject domainObject,
        Class<T> type) {
        checkPersisted(domainObject);

        return reader.findById(domainObject.getId(), type, true);
    }

    /*
     * Searches for any hooks to perform. (nullable)
     * @param clazz Class of domain object.
     * @param query Query that might be used by hook (nullable)
     * @param type Type of data access (Create, Read, Update, Delete)
     * @param when Before or After operation has been done
     */
    private void doAccess(
        Class clazz,
        IsDomainObject domainObject,
        Query query,
        DataAccessType type,
        WhenDataAccess when) {

        if (dataAccessMap.containsKey(clazz)) {
            LOG.info("Looking for hooks for : " + clazz.getSimpleName());
            Map<DataAccessType, Map<WhenDataAccess, List<BaseDataAccessHook>>> accessTypeToWhenMap
                = dataAccessMap.get(clazz);
            if (accessTypeToWhenMap.containsKey(type)) {
                LOG.info("Looking for : " + type.name());
                Map<WhenDataAccess, List<BaseDataAccessHook>> whenToAccessMap
                    = accessTypeToWhenMap.get(type);

                if (whenToAccessMap.containsKey(when)) {
                    LOG.info("Looking for : " + when.name());
                        
                    for (BaseDataAccessHook hook : whenToAccessMap.get(when)) {
                            hook.setDomainObject(domainObject);
                            hook.setQuery(query);
                            hook.run();
                    }
                }
            }
        }
    }

    /*
     * Finds count of a query.
     */
    public <T extends IsDomainObject> Long count(Query query, Class<T> type) {
        return reader.count(query, type);
    }

    /*
     * Checks to see if an object is persited. If not this will
     * throw @exception org.pjherring.mongogwt.shared.exception.NotPersistedException .
     */
    private void checkPersisted(IsDomainObject domainObject) {
        if (domainObject.getId() == null) {
            throw new NotPersistedException();
        }
    }
}
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

    public  void doCreate(IsDomainObject domainObject) {
        doAccess(domainObject.getClass(), domainObject, null, DataAccessType.CREATE, WhenDataAccess.BEFORE);
        creator.doCreate(domainObject);
        doAccess(domainObject.getClass(), domainObject, null, DataAccessType.CREATE, WhenDataAccess.AFTER);
    }

    public void doUpdate(IsDomainObject domainObject) {
        doAccess(domainObject.getClass(), domainObject, null, DataAccessType.UPDATE, WhenDataAccess.BEFORE);
        updater.doUpdate(domainObject);
        doAccess(domainObject.getClass(), domainObject, null, DataAccessType.UPDATE, WhenDataAccess.AFTER);
    }

    public <T extends IsDomainObject> List<T> find(Query query, Class<T> type, boolean doFanOut) {
        doAccess(type, null, query, DataAccessType.READ, WhenDataAccess.BEFORE);
        List<T> toReturn = reader.find(query, type, doFanOut);

        for (T domainObject : toReturn) {
            doAccess(type, domainObject, query, DataAccessType.READ, WhenDataAccess.AFTER);
        }

        return toReturn;
    }

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

    public void doDelete(Query query, Class<? extends IsDomainObject> type) {
        doAccess(type, null, query, DataAccessType.DELETE, WhenDataAccess.BEFORE);
        deleter.doDelete(query, type);
        doAccess(type, null, query, DataAccessType.DELETE, WhenDataAccess.AFTER);
    }

    public <T extends IsDomainObject> T refresh(
        IsDomainObject domainObject,
        Class<T> type) {

        return reader.findById(domainObject.getId(), type, true);
    }

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

    public <T extends IsDomainObject> Long count(Query query, Class<T> type) {
        return reader.count(query, type);
    }
}
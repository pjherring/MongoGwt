/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.guice.provider.hook;

import com.google.inject.Provider;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.pjherring.mongogwt.server.domain.hook.BaseDataAccessHook;
import org.pjherring.mongogwt.server.domain.hook.DataAccessType;
import org.pjherring.mongogwt.server.domain.hook.WhenDataAccess;

/**
 *
 * @author pjherring
 */
public class BaseAccessMapProvider
    implements Provider<Map<DataAccessType, Map<WhenDataAccess, List<BaseDataAccessHook>>>> {

    private Map<DataAccessType, Map<WhenDataAccess, List<BaseDataAccessHook>>>
        accessMap = new EnumMap<DataAccessType, Map<WhenDataAccess, List<BaseDataAccessHook>>>(DataAccessType.class);
    private Map<WhenDataAccess, List<BaseDataAccessHook>> createMap
        = new EnumMap<WhenDataAccess, List<BaseDataAccessHook>>(WhenDataAccess.class);
    private Map<WhenDataAccess, List<BaseDataAccessHook>> readMap
        = new EnumMap<WhenDataAccess, List<BaseDataAccessHook>>(WhenDataAccess.class);
    private Map<WhenDataAccess, List<BaseDataAccessHook>> updateMap
        = new EnumMap<WhenDataAccess, List<BaseDataAccessHook>>(WhenDataAccess.class);
    private Map<WhenDataAccess, List<BaseDataAccessHook>> deleteMap
        = new EnumMap<WhenDataAccess, List<BaseDataAccessHook>>(WhenDataAccess.class);

    protected void setUp() {
        List beforeCreate = getBeforeCreateList();

        if (beforeCreate != null && beforeCreate.size() > 0) {
            createMap.put(WhenDataAccess.BEFORE, beforeCreate);
        }

        List afterCreate = getAfterCreateList();

        if (afterCreate != null && afterCreate.size() > 0) {
            createMap.put(WhenDataAccess.AFTER, afterCreate);
        }

        if (createMap.size() > 0) {
            accessMap.put(DataAccessType.CREATE, createMap);
        }

        List beforeRead = getBeforeReadList();

        if (beforeRead != null && beforeRead.size() > 0) {
            readMap.put(WhenDataAccess.BEFORE, beforeRead);
        }

        List afterRead = getAfterReadList();

        if (afterRead != null && afterRead.size() > 0) {
            readMap.put(WhenDataAccess.AFTER, afterCreate);
        }

        if (readMap.size() > 0) {
            accessMap.put(DataAccessType.READ, readMap);
        }

        List beforeUpdate = getBeforeUpdateList();

        if (beforeUpdate != null && beforeUpdate.size() > 0) {
            updateMap.put(WhenDataAccess.BEFORE, beforeUpdate);
        }

        List afterUpdate = getAfterUpdateList();

        if (afterUpdate != null && afterUpdate.size() > 0) {
            updateMap.put(WhenDataAccess.AFTER, afterUpdate);
        }

        if (updateMap.size() > 0) {
            accessMap.put(DataAccessType.UPDATE, updateMap);
        }

        List beforeDelete = getBeforeDeleteList();

        if (beforeDelete != null && beforeDelete.size() > 0) {
            deleteMap.put(WhenDataAccess.BEFORE, beforeDelete);
        }

        List afterDelete = getAfterDeleteList();

        if (afterDelete != null && afterDelete.size() > 0) {
            deleteMap.put(WhenDataAccess.AFTER, afterDelete);
        }

        if (deleteMap.size() > 0) {
            accessMap.put(DataAccessType.DELETE, deleteMap);
        }

    }

    public Map<DataAccessType, Map<WhenDataAccess, List<BaseDataAccessHook>>> get() {
        setUp();
        return accessMap;
    }

    /*
     * Override these methods to set up maps
     */
    protected List<BaseDataAccessHook> getBeforeCreateList() {
        return null;
    }

    protected List<BaseDataAccessHook> getAfterCreateList() {
        return null;
    }

    protected List<BaseDataAccessHook> getBeforeReadList() {
        return null;
    }

    protected List<BaseDataAccessHook> getAfterReadList() {
        return null;
    }

    protected List<BaseDataAccessHook> getBeforeUpdateList() {
        return null;
    }

    protected List<BaseDataAccessHook> getAfterUpdateList() {
        return null;
    }

    protected List<BaseDataAccessHook> getBeforeDeleteList() {
        return null;
    }

    protected List<BaseDataAccessHook> getAfterDeleteList() {
        return null;
    }

}

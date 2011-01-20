/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.guice.provider.hook;


import com.google.inject.Provider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.pjherring.mongogwt.server.domain.hook.BaseDataAccessHook;
import org.pjherring.mongogwt.server.domain.hook.DataAccessType;
import org.pjherring.mongogwt.server.domain.hook.WhenDataAccess;
import org.pjherring.mongogwt.shared.IsEntity;

/**
 *
 * @author pjherring
 */
public class EmptyAccessMapProvider 
    implements Provider<
        Map<
            Class<? extends IsEntity>,
            Map<
                DataAccessType,
                Map<
                    WhenDataAccess,
                    List<BaseDataAccessHook>
                >
            >
        >
    > {

    private final static Logger LOG = Logger.getLogger(EmptyAccessMapProvider.class.getName());

    private Map<Class<? extends IsEntity>, Map<DataAccessType, Map<WhenDataAccess, List<BaseDataAccessHook>>>>
        map = new HashMap<Class<? extends IsEntity>, Map<DataAccessType, Map<WhenDataAccess, List<BaseDataAccessHook>>>>();

    public Map<Class<? extends IsEntity>, Map<DataAccessType, Map<WhenDataAccess, List<BaseDataAccessHook>>>> get() {
        return map;
    }

}

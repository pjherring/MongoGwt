/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.guice;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import java.util.List;
import java.util.Map;
import org.pjherring.mongogwt.server.domain.hook.DataAccessType;
import org.pjherring.mongogwt.server.domain.hook.BaseDataAccessHook;
import org.pjherring.mongogwt.server.domain.hook.WhenDataAccess;
import org.pjherring.mongogwt.server.guice.provider.hook.EmptyAccessMapProvider;
import org.pjherring.mongogwt.shared.IsEntity;

/**
 *
 * @author pjherring
 */
public class DataAccessModule extends AbstractModule {
    /*
     * This type literal lets us map DataAccess classes to DomainObjects by
     * going up a chain of Type (CRUD) and When (Before or After). So I can find
     * that when I need to run an operation before creating some DomainClass,
     * lets call it Member I would find that class by searching this Map with the
     * following: Member.class, DataAccessType.CREATE, DataAccessType.BEFORE.
     */
    protected TypeLiteral<
        Map<
            Class<? extends IsEntity>,
            Map<DataAccessType,
                Map<WhenDataAccess, 
                    List<BaseDataAccessHook>>
            >
        >
    > dataAccessMapLiteral = new TypeLiteral<
        Map<Class<? extends IsEntity>, Map<
            DataAccessType, Map<
                WhenDataAccess, 
                    List<BaseDataAccessHook>
            >>>>(){;};
        

    @Override
    protected void configure() {
        bindAccessMap();
    }

    protected void bindAccessMap() {
        bind(dataAccessMapLiteral).toProvider(EmptyAccessMapProvider.class);
    }




}

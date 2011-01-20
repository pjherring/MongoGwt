/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.rpc;


import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.pjherring.mongogwt.server.domain.rpc.DatabaseServiceImpl;
import org.pjherring.mongogwt.server.guice.DataAccessTestModule;
import org.pjherring.mongogwt.server.guice.DatabaseModule;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.domain.SimpleDomain;
import org.pjherring.mongogwt.shared.domain.operation.Database;

/**
 *
 * @author pjherring
 */
@Singleton
public class DatabaseImplMock extends DatabaseServiceImpl {

    private static Injector injector = Guice.createInjector(
        new DataAccessTestModule(),
        new DbTestGuiceModule()
    );
    private static Database database = injector.getInstance(Database.class);

    public DatabaseImplMock() {
        super(database);
    }

    public static class DbTestGuiceModule extends DatabaseModule {

        @Override
        protected String getHostName() {
            return "localhost";
        }

        @Override
        protected String getDatabaseName() {
            return "mongoGwtClientTesting";
        }

        @Override
        protected List<Class<? extends IsEntity>> getEntityList() {
            List<Class<? extends IsEntity>> entities = new ArrayList<Class<? extends IsEntity>>();
            entities.add(SimpleDomain.class);
            return entities;
        }

    }


}

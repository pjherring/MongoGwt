/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.rpc;


import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.pjherring.mongogwt.server.domain.rpc.DatabaseServiceImpl;
import org.pjherring.mongogwt.server.guice.DataAccessTestModule;
import org.pjherring.mongogwt.server.guice.DatabaseClientTestModule;
import org.pjherring.mongogwt.shared.domain.operation.Database;

/**
 *
 * @author pjherring
 */
@Singleton
public class DatabaseImplMock extends DatabaseServiceImpl {

    private static Injector injector = Guice.createInjector(
        new DatabaseClientTestModule(),
        new DataAccessTestModule()
    );
    private static Database database = injector.getInstance(Database.class);

    public DatabaseImplMock() {
        super(database);
    }

}

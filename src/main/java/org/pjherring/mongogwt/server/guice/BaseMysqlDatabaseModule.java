/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.guice;


import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.logging.Logger;
import org.pjherring.mongogwt.shared.IsEntity;

/**
 *
 * @author pjherring
 */
public abstract class BaseMysqlDatabaseModule extends AbstractModule {


    private final static Logger LOG = Logger.getLogger(BaseMysqlDatabaseModule.class.getName());

    @Override
    protected void configure() {
    }

    @Inject
    @Provides
    @Singleton
    public Connection getConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection(
                getDatabaseUrl(),
                getLogin(),
                getPassword()
            );

            return connection;
        } catch (Exception e) {
            LOG.warning("FAILURE IN CONNECTING TO THE DATABASE");
            throw new RuntimeException(e);
        }
    }


    protected abstract String getDatabaseUrl();
    protected abstract String getLogin();
    protected abstract String getPassword();
    protected abstract List<Class<? extends IsEntity>> getEntities();

    @Provides
    public List<Class<? extends IsEntity>> getEntityList() {
        return getEntities();
    }

}

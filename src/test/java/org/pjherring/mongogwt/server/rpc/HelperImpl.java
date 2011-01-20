/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.rpc;


import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mongodb.DB;
import java.util.logging.Logger;
import org.pjherring.mongogwt.client.rpc.Helper;
import org.pjherring.mongogwt.server.guice.DataAccessTestModule;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.domain.SimpleDomain;

/**
 *
 * @author pjherring
 */
public class HelperImpl extends RemoteServiceServlet implements Helper {

    private final static Logger LOG = Logger.getLogger(HelperImpl.class.getName());
    private Injector injector = Guice.createInjector(new DatabaseImplMock.DbTestGuiceModule(), new DataAccessTestModule());

    public void dumpDatabase() {
        LOG.info("DUMPING");
        DB db = injector.getInstance(DB.class);
        db.getCollection(SimpleDomain.class.getAnnotation(Entity.class).name()).drop();
    }

}

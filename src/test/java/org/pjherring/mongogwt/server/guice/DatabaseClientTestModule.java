/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.guice;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.domain.SimpleDomain;

/**
 *
 * @author pjherring
 */
public class DatabaseClientTestModule extends DatabaseModule {

    private final static Logger LOG = Logger.getLogger(DatabaseClientTestModule.class.getName());

    @Override
    protected String getHostName() {
        return "localhost";
    }

    @Override
    protected String getDatabaseName() {
        return "cwClientTest";
    }

    @Override
    protected void bindCollectionNames() {
        List<String> list = new ArrayList<String>();
        list.add(SimpleDomain.class.getAnnotation(Entity.class).name());

        bind(stringList).annotatedWith(CollectionNames.class).toInstance(list);
    }

}

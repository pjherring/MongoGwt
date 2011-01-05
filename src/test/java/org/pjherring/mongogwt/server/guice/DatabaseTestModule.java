/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.guice;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.domain.DomainOne;
import org.pjherring.mongogwt.shared.domain.DomainTwo;

/**
 *
 * @author pjherring
 */
public class DatabaseTestModule extends DatabaseModule {

    private final static Logger LOG = Logger.getLogger(DatabaseTestModule.class.getName());

    @Override
    protected String getHostName() {
        return "localhost";
    }

    @Override
    protected String getDatabaseName() {
        return "testCw";
    }

    @Override
    protected void bindCollectionNames() {
        List<String> list = new ArrayList<String>();
        list.add(DomainOne.class.getAnnotation(Entity.class).name());
        list.add(DomainTwo.class.getAnnotation(Entity.class).name());

        bind(stringList).annotatedWith(CollectionNames.class).toInstance(list);
    }

}

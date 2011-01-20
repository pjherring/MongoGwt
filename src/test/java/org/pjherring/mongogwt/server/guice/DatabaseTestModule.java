/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.guice;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.pjherring.mongogwt.server.domain.operation.PojoMapTranslatorTest;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.domain.DomainOne;
import org.pjherring.mongogwt.shared.domain.DomainTwo;
import org.pjherring.mongogwt.shared.domain.DomainUnique;

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
    protected List<Class<? extends IsEntity>> getEntityList() {
        List<Class<? extends IsEntity>> entities = new ArrayList<Class<? extends IsEntity>>();
        entities.add(DomainOne.class);
        entities.add(DomainTwo.class);
        entities.add(DomainUnique.class);
        entities.add(PojoMapTranslatorTest.PojoMapDomain.class);

        return entities;
    }

}

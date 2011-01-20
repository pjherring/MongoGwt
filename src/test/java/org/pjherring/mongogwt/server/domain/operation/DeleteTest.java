/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;

import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.query.Query;
import org.pjherring.mongogwt.shared.domain.DomainOne;
import org.pjherring.mongogwt.shared.domain.DomainTwo;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mongodb.DB;
import org.pjherring.mongogwt.server.guice.DataAccessTestModule;
import org.pjherring.mongogwt.server.guice.DatabaseTestModule;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pjherring.mongogwt.shared.domain.operation.Database;
import org.pjherring.mongogwt.shared.exception.NotFoundException;
import org.pjherring.mongogwt.shared.exception.NotPersistedException;
import static org.junit.Assert.*;

/**
 *
 * @author pjherring
 */
public class DeleteTest {

    private final static Injector injector =
        Guice.createInjector(new DatabaseTestModule(), new DataAccessTestModule());
    private Database database;
    private DB mongoDb;

    public DeleteTest() {
        database = injector.getInstance(Database.class);
        mongoDb = injector.getInstance(DB.class);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        mongoDb.getCollection(DomainTwo.class.getAnnotation(Entity.class).name()).drop();
        mongoDb.getCollection(DomainOne.class.getAnnotation(Entity.class).name()).drop();
    }

    @Test(expected=NotFoundException.class)
    public void testDelete() {
        DomainOne domainOne = SaveTest.createDomainOne();
        DomainTwo domainTwo = SaveTest.createDomainTwo();
        domainOne.setDomainTwo(domainTwo);

        database.create(domainOne);
        assertNotNull(domainOne.getId());

        database.delete(new Query().start("_id").is(domainOne.getId()), DomainOne.class);
        database.findOne(new Query().start("_id").is(domainOne.getId()), DomainOne.class, true);
    }

    @Test(expected=NotFoundException.class)
    public void testDeleteObject() {
        DomainOne domainOne = SaveTest.createDomainOne();
        DomainTwo domainTwo = SaveTest.createDomainTwo();
        domainOne.setDomainTwo(domainTwo);

        database.create(domainOne);
        assertNotNull(domainOne.getId());

        database.delete(domainOne);
        database.findOne(new Query().start("_id").is(domainOne.getId()), DomainOne.class, true);
    }

    @Test(expected=NotPersistedException.class)
    public void testNotPersisted() {
        DomainOne domainOne = SaveTest.createDomainOne();
        database.delete(domainOne);
    }

}
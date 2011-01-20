/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;

import org.pjherring.mongogwt.shared.query.Query;
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
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.domain.DomainOne;
import org.pjherring.mongogwt.shared.domain.DomainTwo;
import org.pjherring.mongogwt.shared.domain.DomainUnique;
import org.pjherring.mongogwt.shared.domain.operation.Database;
import static org.junit.Assert.*;

/**
 *
 * @author pjherring
 */
public class UpdateTest {

    private final static Injector injector =
        Guice.createInjector(new DatabaseTestModule(), new DataAccessTestModule());
    private Database database;
    private DB mongoDb;

    public UpdateTest() {
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
        mongoDb.getCollection(DomainTwo.class.getAnnotation(Entity.class).name()).drop();
        mongoDb.getCollection(DomainOne.class.getAnnotation(Entity.class).name()).drop();
        mongoDb.getCollection(DomainUnique.class.getAnnotation(Entity.class).name()).drop();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testUpdate() {
        String newString = "newstring12";
        String oldString = "oldstring12";
        //create
        DomainTwo domain = SaveTest.createDomainTwo();
        domain.setStringData(oldString);

        assertNull(domain.getId());
        database.create(domain);
        assertNotNull(domain.getId());

        //find
        DomainTwo found = database.findOne(
            new Query().start("_id").is(domain.getId()),
            DomainTwo.class,
            true
        );
        assertEquals(oldString, found.getStringData());
        found.setStringData(newString);
        //update
        database.update(found);

        //find again
        DomainTwo foundAgain = database.findOne(
            new Query().start("_id").is(domain.getId()),
            DomainTwo.class,
            true
        );
        assertEquals(newString, foundAgain.getStringData());
    }

    @Test
    public void testUpdateWithUnique() {
        String oldString = "some unique string";
        String newString = "some new string";
        DomainUnique unique = new DomainUnique();
        unique.setUniqueString(oldString);

        database.create(unique);

        Query findQuery = new Query().start("_id").is(unique.getId());

        DomainUnique found = database.findOne(findQuery, DomainUnique.class, true);

        assertEquals(oldString, found.getUniqueString());

        found.setUniqueString(newString);
        database.update(found);

        DomainUnique foundAgain = database.findOne(findQuery, DomainUnique.class, true);
        assertEquals(newString, foundAgain.getUniqueString());
    }

}
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
import org.pjherring.mongogwt.shared.domain.DomainTwo;
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
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testUpdate() {
        //create
        DomainTwo domain = SaveTest.createDomainTwo();

        assertNull(domain.getId());
        database.create(domain);
        assertNotNull(domain.getId());

        //find
        DomainTwo found = database.findOne(
            new Query().start("_id").is(domain.getId()),
            DomainTwo.class,
            true
        );
        assertEquals(found.getId(), domain.getId());
        String oldString = found.getStringData();
        found.setStringData("new String");
        //update
        database.update(found);

        //find again
        DomainTwo foundAgain = database.findOne(
            new Query().start("_id").is(domain.getId()),
            DomainTwo.class,
            true
        );
        assertFalse(foundAgain.getStringData().equals(oldString));
    }

}
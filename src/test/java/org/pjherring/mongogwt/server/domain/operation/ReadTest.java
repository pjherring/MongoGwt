/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;


import java.util.List;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mongodb.DB;
import com.mongodb.QueryBuilder;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pjherring.mongogwt.server.guice.DataAccessTestModule;
import org.pjherring.mongogwt.server.guice.DatabaseTestModule;
import org.pjherring.mongogwt.shared.domain.DomainOne;
import org.pjherring.mongogwt.shared.domain.DomainTwo;
import org.pjherring.mongogwt.shared.domain.operation.Database;
import org.pjherring.mongogwt.shared.exception.NotFoundException;
import org.pjherring.mongogwt.shared.query.Query;
import static org.junit.Assert.*;

/**
 *
 * @author pjherring
 */
public class ReadTest {

    private final static Logger LOG = Logger.getLogger(ReadTest.class.getName());

    private final static Injector injector =
        Guice.createInjector(new DatabaseTestModule(), new DataAccessTestModule());
    private Database database;
    private DB mongoDb;

    public ReadTest() {
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
        mongoDb.getCollection("domainOne").drop();
        mongoDb.getCollection("domainTwo").drop();
    }

    @Test
    public void testIs() {
        DomainOne one = SaveTest.createDomainOne();
        one.setStringData("some");
        database.create(one);

        Query query = new Query()
            .start("stringData").is("some");
        DomainOne d1Found = database.findOne(query, DomainOne.class, true);

        assertEquals(one.getId(), d1Found.getId());
    }

    @Test
    public void testNotEquals() {
        DomainOne one = SaveTest.createDomainOne();
        one.setStringData("blop");
        database.create(one);

        Query query = new Query()
            .start("stringData").notEquals("some");
        List<DomainOne> results = database.find(query, DomainOne.class, true);
        assertTrue(results.size() == 1);
        assertEquals(results.get(0).getId(), one.getId());

    }

    @Test
    public void testLessThan() {
        DomainOne one = SaveTest.createDomainOne();
        one.setLongData(50L);
        database.create(one);

        Query query = new Query()
            .start("longData").lessThen(60L);
        List<DomainOne> results = database.find(query, DomainOne.class, true);
        assertTrue(results.size() == 1);
        assertEquals(results.get(0).getId(), one.getId());

        Query notFoundQuery = new Query()
            .start("longData").lessThen(50L);

        try {
            database.find(notFoundQuery, DomainOne.class, true);
        } catch (NotFoundException e) {
            assertTrue("NOT FOUND", true);
        }

        notFoundQuery = new Query()
            .start("longData").lessThen(40L);

        try {
            database.find(notFoundQuery, DomainOne.class, true);
        } catch (NotFoundException e) {
            assertTrue("NOT FOUND", true);
        }
    }

    @Test
    public void testLessThanEqual() {
        DomainOne one = SaveTest.createDomainOne();
        one.setLongData(50L);
        database.create(one);

        Query query = new Query()
            .start("longData").lessThenEquals(50L);
        List<DomainOne> results = database.find(query, DomainOne.class, true);
        assertTrue(results.size() == 1);
        assertEquals(results.get(0).getId(), one.getId());

        Query notFoundQuery = new Query()
            .start("longData").lessThenEquals(49L);

        try {
            database.find(notFoundQuery, DomainOne.class, true);
        } catch (NotFoundException e) {
            assertTrue("NOT FOUND", true);
        }
    }

    @Test
    public void testGreaterThan() {
        DomainOne one = SaveTest.createDomainOne();
        one.setLongData(50L);
        database.create(one);

        Query query = new Query()
            .start("longData").greaterThen(40L);
        List<DomainOne> results = database.find(query, DomainOne.class, true);
        assertTrue(results.size() == 1);
        assertEquals(results.get(0).getId(), one.getId());

        Query notFoundQuery = new Query()
            .start("longData").greaterThen(50L);

        try {
            database.find(notFoundQuery, DomainOne.class, true);
        } catch (NotFoundException e) {
            assertTrue("NOT FOUND", true);
        }

        notFoundQuery = new Query()
            .start("longData").greaterThen(60L);

        try {
            database.find(notFoundQuery, DomainOne.class, true);
        } catch (NotFoundException e) {
            assertTrue("NOT FOUND", true);
        }
    }

    @Test
    public void testGreaterThanEqual() {
        DomainOne one = SaveTest.createDomainOne();
        one.setLongData(50L);
        database.create(one);

        Query query = new Query()
            .start("longData").greaterThenEquals(50L);
        List<DomainOne> results = database.find(query, DomainOne.class, true);
        assertTrue(results.size() == 1);
        assertEquals(results.get(0).getId(), one.getId());

        Query notFoundQuery = new Query()
            .start("longData").greaterThenEquals(51L);

        try {
            database.find(notFoundQuery, DomainOne.class, true);
        } catch (NotFoundException e) {
            assertTrue("NOT FOUND", true);
        }
    }

    @Test
    public void testRange() {
        DomainOne one = SaveTest.createDomainOne();
        one.setLongData(50L);
        database.create(one);

        Query query = new Query()
            .start("longData").range(40L, 60L);

        List<DomainOne> results = database.find(query, DomainOne.class, true);
        assertTrue(results.size() == 1);
        assertEquals(results.get(0).getId(), one.getId());

        Query notIncludeFloorQuery = new Query()
            .start("longData").range(40L, 60L, false, false);

        results = database.find(notIncludeFloorQuery, DomainOne.class, true);
        assertTrue(results.size() == 1);
        assertEquals(results.get(0).getId(), one.getId());


    }

    @Test(expected=NotFoundException.class)
    public void testInvalidRange() {
        Query invalidRangeQuery = new Query()
            .start("longData").range(51L, 60L);
        database.find(invalidRangeQuery, DomainOne.class, true);
    }

    @Test
    public void testAll() {
        DomainOne one = SaveTest.createDomainOne();
        one.addIntgers(1, 3, 4, 5);
        database.create(one);
        Query query = new Query()
            .start("integers").all(1, 3, 4, 5);
        List<DomainOne> results = database.find(query, DomainOne.class, true);
        assertTrue(results.size() == 1);
        assertEquals(results.get(0).getId(), one.getId());
    }

    @Test(expected=NotFoundException.class)
    public void testAllFailure() {
        DomainOne one = SaveTest.createDomainOne();
        one.addIntgers(1, 3, 5);
        database.create(one);
        Query query = new Query()
            .start("integers").all(1, 3, 4, 5);
        database.find(query, DomainOne.class, true);
    }

    @Test
    public void testIn() {
        DomainTwo two = SaveTest.createDomainTwo();
        two.setLongData(5L);
        database.create(two);
        Query query = new Query()
            .start("longData").in(3L, 5L, 10L);

        List<DomainTwo> results = database.find(query, DomainTwo.class, true);
        assertTrue(results.size() == 1);
        assertEquals(results.get(0).getId(), two.getId());
    }

    @Test(expected=NotFoundException.class)
    public void testFailedIn() {
        DomainOne one = SaveTest.createDomainOne();
        one.setLongData(5L);
        database.create(one);
        Query query = new Query()
            .start("longData").in(3L, 6L, 10L);

        database.find(query, DomainOne.class, true);
    }

    @Test
    public void testNotIn() {
        DomainOne one = SaveTest.createDomainOne();
        one.setLongData(5L);
        database.create(one);
        Query query = new Query()
            .start("longData").notIn(3L, 10L);

        List<DomainOne> results = database.find(query, DomainOne.class, true);
        assertTrue(results.size() == 1);
        assertEquals(results.get(0).getId(), one.getId());
    }

    @Test(expected=NotFoundException.class)
    public void testFailedNotIn() {
        DomainOne one = SaveTest.createDomainOne();
        one.setLongData(5L);
        database.create(one);
        Query query = new Query()
            .start("longData").notIn(3L, 5L, 10L);

        database.find(query, DomainOne.class, true);
    }

    @Test
    public void testOr() {
        DomainOne one = SaveTest.createDomainOne();
        String d1String = "someString";
        long domainLong = 5L;
        one.setStringData(d1String);

        DomainOne oneTwo = SaveTest.createDomainOne();
        oneTwo.setStringData("randomString");
        oneTwo.addIntgers(1,3,5);

        database.create(one);
        database.create(oneTwo);

        Query firstOr = new Query()
            .start("stringData").is(d1String);
        Query secondOr = new Query()
            .start("longData").is(domainLong);
        Query thirdOr = new Query()
            .start("integers").all(1,3,5);

        Query orQuery = new Query().or(firstOr, secondOr);
        List<DomainOne> results = database.find(orQuery, DomainOne.class, true);
        assertTrue(results.size() == 2);
    }

    @Test
    public void testRegexp() {
        String regexp = "[a-z]{4,}";
        DomainOne one = SaveTest.createDomainOne();
        one.setStringData("someString");

        database.create(one);

        Query query = new Query()
            .start("stringData").regexp(regexp);
        List<DomainOne> results = database.find(query, DomainOne.class, true);
        assertTrue(results.size() == 1);
        assertEquals(one.getId(), results.get(0).getId());
    }

    @Test
    public void testSort() {
        DomainOne one = SaveTest.createDomainOne();
        one.setStringData("aaaa");

        DomainOne oneTwo = SaveTest.createDomainOne();
        oneTwo.setStringData("bbbb");

        database.create(one);
        database.create(oneTwo);

        Query query = new Query().addSort("name", Query.Sort.DESC);
        List<DomainOne> results = database.find(query, DomainOne.class, true);

        assertTrue(results.size() == 2);
        assertEquals(results.get(1).getId(), oneTwo.getId());
        assertEquals(results.get(0).getId(), one.getId());
    }

    @Test
    public void testLimit() {
        DomainOne one = SaveTest.createDomainOne();
        one.setStringData("aaaa");
        
        DomainOne oneTwo = SaveTest.createDomainOne();
        oneTwo.setStringData("dddd");

        database.create(one);
        database.create(oneTwo);
        Query query = new Query().setLimit(1);
        List<DomainOne> results =  database.find(query, DomainOne.class, true);
        assertEquals(results.size(), 1);
    }

    @Test
    public void testCount() {
        DomainOne one = SaveTest.createDomainOne();
        one.setStringData("aaaa");

        DomainOne oneTwo = SaveTest.createDomainOne();
        oneTwo.setStringData("dddd");

        database.create(one);
        database.create(oneTwo);

        Query query = new Query();
        Long count = database.count(query, DomainOne.class);
        assertEquals(new Long(2), count);
    }

    @Test(expected=Query.KeyNotSetException.class)
    public void testKeyNotStored() {
        Query query = new Query().is("someValue");
    }
}

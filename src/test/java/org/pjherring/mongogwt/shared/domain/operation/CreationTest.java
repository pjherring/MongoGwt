/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.domain.operation;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mongodb.DB;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pjherring.mongogwt.server.guice.DataAccessTestModule;
import org.pjherring.mongogwt.server.guice.DatabaseTestModule;
import org.pjherring.mongogwt.shared.domain.DomainOne;
import org.pjherring.mongogwt.shared.domain.DomainTwo;
import org.pjherring.mongogwt.shared.exceptions.RegexpException;
import org.pjherring.mongogwt.shared.exceptions.UniqueException;
import static org.junit.Assert.*;

/**
 *
 * @author pjherring
 */
public class CreationTest {

    private final static Injector injector =
        Guice.createInjector(new DatabaseTestModule(), new DataAccessTestModule());
    private Database database;
    private DB mongoDb;

    public CreationTest() {
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

    public static DomainOne createDomainOne() {
        DomainOne domainOne = new DomainOne();
        domainOne.setStringData("stringData");
        domainOne.setDateData(new Date());
        domainOne.setLongData(5L);

        return domainOne;
    }

    public static DomainTwo createDomainTwo() {
        DomainTwo domainTwo = new DomainTwo();
        domainTwo.setDateData(new Date());
        domainTwo.setStringData("13afafa");
        domainTwo.setLongData(5L);

        return domainTwo;
    }

    @Test
    public void testCreateTwo() {
        DomainTwo d2 = createDomainTwo();

        assertNull(d2.getId());
        assertNull(d2.getCreatedDatetime());

        database.doCreate(d2);

        assertNotNull(d2.getId());
        assertNotNull(d2.getCreatedDatetime());
    }

    @Test(expected=RegexpException.class)
    public void testRegexpError() {
        DomainTwo d2 = createDomainTwo();
        //d2 expects there to be at least to digits
        d2.setStringData("some string");
        database.doCreate(d2);
    }

    @Test
    public void testCreateOne() {
        DomainOne d1 = createDomainOne();
        DomainTwo d2 = createDomainTwo();
        d1.setDomainTwo(d2);

        database.doCreate(d1);

        assertNotNull(d1.getId());
        assertNotNull(d2.getId());
    }

    @Test(expected=UniqueException.class)
    public void testUniqueException() {
        DomainOne d1 = createDomainOne();
        DomainTwo d2 = createDomainTwo();
        DomainOne d11 = createDomainOne();

        d1.setDomainTwo(d2);
        d11.setDomainTwo(d2);
        database.doCreate(d1);
        database.doCreate(d11);
    }

    @Test
    public void testArrayStore() {
        DomainOne d1 = createDomainOne();
        d1.setStringData("unique");
        DomainTwo d2 = createDomainTwo();
        d1.setDomainTwo(d2);
        d1.addIntgers(5, 3, 2);
        database.doCreate(d1);
    }

}
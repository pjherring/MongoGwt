/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;

import org.pjherring.mongogwt.shared.annotations.Entity;
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
import org.pjherring.mongogwt.shared.domain.operation.Database;
import org.pjherring.mongogwt.shared.exception.RegexpException;
import org.pjherring.mongogwt.shared.exception.UniqueException;
import static org.junit.Assert.*;

/**
 *
 * @author pjherring
 */
public class SaveTest {

    private final static Injector injector =
        Guice.createInjector(new DatabaseTestModule(), new DataAccessTestModule());
    private Database database;
    private DB mongoDb;

    public SaveTest() {
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
    }

    @After
    public void tearDown() {
    }

    public static DomainOne createDomainOne() {
        DomainOne domainOne = new DomainOne();
        domainOne.setStringData("stringData");
        domainOne.setDateData(new Date());
        domainOne.setLongData(5L);

        domainOne.setDomainTwo(createDomainTwo());

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

        database.create(d2);

        assertNotNull(d2.getId());
        assertNotNull(d2.getCreatedDatetime());
    }

    @Test(expected=RegexpException.class)
    public void testRegexpError() {
        DomainTwo d2 = createDomainTwo();
        //d2 expects there to be at least to digits
        d2.setStringData("some string");
        database.create(d2);
    }

    @Test
    public void testCreateOne() {
        DomainOne d1 = createDomainOne();
        DomainTwo d2 = createDomainTwo();
        d1.setDomainTwo(d2);

        database.create(d1);

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
        database.create(d1);
        database.create(d11);
    }

    @Test
    public void testArrayStore() {
        DomainOne d1 = createDomainOne();
        d1.setStringData("unique");
        DomainTwo d2 = createDomainTwo();
        d1.setDomainTwo(d2);
        d1.addIntgers(5, 3, 2);
        database.create(d1);
    }

}
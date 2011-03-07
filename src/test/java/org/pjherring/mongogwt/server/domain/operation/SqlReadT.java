/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;

import java.sql.Connection;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pjherring.mongogwt.shared.query.Query;
import org.pjherring.mongogwt.testing.domain.EntityOne;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 *
 * This has been removed from testing.
 *
 * @author pjherring
 */
public class SqlReadT extends EasyMockSupport {

    SqlRead read;
    Connection connection;


    public SqlReadT() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        connection = createMock(Connection.class);
        read = new SqlRead(connection);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testBuildSelect() {
        Query query = new Query().select("one", "two", "three");

        String select = read.queryToSql(query, EntityOne.class);
        assertEquals("SELECT o.one, o.two, o.three FROM one o", select);
    }

    @Test
    public void testBuilderWithJoin() {
        Query query = new Query()
            .select("one", "two", "three", "two.four");
        String sql = read.queryToSql(query, EntityOne.class);
        assertEquals("SELECT o.one, o.two, o.three, t.four FROM one o JOIN two t", sql);
    }

    @Test
    public void testSimpleWhere() {
        Query query = new Query().start("one").is("someValue");
        String sql = read.queryToSql(query, EntityOne.class);
        assertEquals(
            "SELECT * FROM one o WHERE o.one = ?", sql
        );
    }

    @Test
    public void testAnd() {
        Query query =
            new Query()
                .start("one").is("someValue")
                .and("two").is("otherValue");

        String sql = read.queryToSql(query, EntityOne.class);
        assertEquals(
            "SELECT * FROM one o WHERE o.one = ?", sql
        );
    }

}
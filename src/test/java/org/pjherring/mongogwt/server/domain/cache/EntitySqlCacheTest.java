/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.cache;

import org.easymock.Capture;
import java.util.Map;
import org.pjherring.mongogwt.testing.domain.EntityOne;
import java.sql.Connection;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 *
 * @author pjherring
 */
public class EntitySqlCacheTest extends EasyMockSupport {

    EntitySqlCache cache;
    Connection conn;
    Map cacheMock;

    public EntitySqlCacheTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        conn = createMock(Connection.class);
        cache = new EntitySqlCache(conn,  new EntityMetaCache());
        cacheMock = createMock(Map.class);
        cache.insertCache = cacheMock;
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConstructStatementMeta() throws Exception {
        Capture<String> sqlCapture = new Capture<String>();

        expect(conn.prepareStatement(capture(sqlCapture))).andReturn(null);

        replayAll();

        StatementMeta statement = cache.constructStatementMeta(EntityOne.class);

        assertTrue(statement.getColumns().contains("data"));
        assertTrue(statement.getColumns().contains("date"));
        assertTrue(statement.getColumns().contains("isSomething"));

        String sql = sqlCapture.getValue();
        assertTrue(sql.contains("INSERT"));
        assertTrue(sql.contains("(?, ?, ?)"));
        assertTrue(sql.contains("data"));
        assertTrue(sql.contains("date"));
        assertTrue(sql.contains("isSomething"));
        assertNotNull(sql);
    }
}
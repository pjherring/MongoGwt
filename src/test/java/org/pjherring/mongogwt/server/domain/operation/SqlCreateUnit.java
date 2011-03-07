/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;

import org.pjherring.mongogwt.server.domain.operation.SqlCreate;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.List;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pjherring.mongogwt.server.domain.cache.EntityMetaCache;
import org.pjherring.mongogwt.server.domain.cache.EntitySqlCache;
import org.pjherring.mongogwt.server.domain.cache.StatementMeta;
import org.pjherring.mongogwt.testing.domain.EntityOne;
import static org.easymock.EasyMock.*;

/**
 *
 * NOTE: This has been removed from testing.
 * @author pjherring
 */
public class SqlCreateUnit extends EasyMockSupport {

    SqlCreate create;
    EntitySqlCache cache;

    public SqlCreateUnit() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        cache = createMock(EntitySqlCache.class);
        create = new SqlCreate(new EntityMetaCache(), cache);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSimpleInsert() throws Exception {
        EntityOne entity = new EntityOne();
        entity.setData("data");
        entity.setDate(new Date());
        entity.setIsSomething(true);
        
        List listMock = createMock(List.class);

        StatementMeta statementMeta = createMock(StatementMeta.class);
        PreparedStatement preparedMock = createMock(PreparedStatement.class);

        expect(cache.getInsertStatement(eq(EntityOne.class)))
            .andReturn(statementMeta);
        expect(statementMeta.getColumns()).andReturn(listMock);
        expectLastCall().times(3);

        expect(listMock.indexOf(eq("data"))).andReturn(1);
        expect(listMock.indexOf(eq("date"))).andReturn(2);
        expect(listMock.indexOf(eq("isSomething"))).andReturn(3);

        expect(statementMeta.getPreparedStatement()).andReturn(preparedMock);
        expectLastCall().times(4);

        preparedMock.setObject(eq(1), eq("data"));
        preparedMock.setObject(eq(2), isA(Date.class));
        preparedMock.setObject(3, true);

        expect(preparedMock.execute()).andReturn(true);

        replayAll();

        create.create(entity);

        verifyAll();
    }

}
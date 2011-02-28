/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.hook;

import org.pjherring.mongogwt.shared.domain.operation.Validate;
import org.pjherring.mongogwt.shared.domain.hook.DataAccessHook;
import java.util.Map;
import java.util.Arrays;
import java.util.List;
import org.pjherring.mongogwt.shared.domain.hook.DataAccessHook.Hooks;
import java.util.logging.Logger;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pjherring.mongogwt.shared.BaseDomainObject;
import org.pjherring.mongogwt.shared.IsStorable;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.query.Query;
import static org.easymock.EasyMock.*;

/**
 *
 * @author pjherring
 */
public class DataAccessHookRunnerTest extends EasyMockSupport {

    private final static Logger LOG = Logger.getLogger(DataAccessHookRunnerTest.class.getName());
    private DataAccessHookRunner runner;
    private Validate validate;

    public DataAccessHookRunnerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        validate = createMock(Validate.class);
        runner = new DataAccessHookRunner(validate);
    }

    @After
    public void tearDown() {
    }

    public static class CreateHook extends BaseDataAccessHook<SimpleEntity> {

        public static String adjusted = "adjusted";

        @Override
        public void run() {
            getDomainObject().setData(getDomainObject().getData() + adjusted);
        }

        @Override
        public boolean doRun() {
            return getDomainObject() != null;
        }

    }

    public static class QueryReadHook extends BaseDataAccessHook<SimpleEntity> {

        public static String key = "key";
        public static String oldValue = "oldValue";
        public static String newValue = "new";

        @Override
        public void run() {
            getQuery().getQueryMap().put("key", newValue);
        }

        @Override
        public boolean doRun() {
            return getQuery() != null;
        }

    }

    @Hooks(
        preCreate={CreateHook.class},
        postCreate={CreateHook.class},
        preRead={CreateHook.class, QueryReadHook.class},
        postRead={CreateHook.class,  QueryReadHook.class},
        preUpdate={CreateHook.class},
        postUpdate={CreateHook.class},
        preDelete={CreateHook.class},
        postDelete={CreateHook.class}
    )
    @Entity(name="simple")
    public static class SimpleEntity extends BaseDomainObject {
        private String data;

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    @Test
    public void testEntityHook() {
        validate.validate(isA(IsStorable.class));
        expectLastCall().atLeastOnce();

        String data = "data";
        SimpleEntity entity = createMock(SimpleEntity.class);
        expect(entity.getData()).andReturn(data);
        expectLastCall().times(8);
        entity.setData(eq(data + CreateHook.adjusted));
        expectLastCall().times(8);

        replayAll();
        
        runner.runDataAccessHooks(entity, SimpleEntity.class, DataAccessHook.When.PRE, DataAccessHook.What.CREATE);
        runner.runDataAccessHooks(entity, SimpleEntity.class, DataAccessHook.When.POST, DataAccessHook.What.CREATE);
        runner.runDataAccessHooks(entity, SimpleEntity.class, DataAccessHook.When.PRE, DataAccessHook.What.READ);
        runner.runDataAccessHooks(entity, SimpleEntity.class, DataAccessHook.When.POST, DataAccessHook.What.READ);
        runner.runDataAccessHooks(entity, SimpleEntity.class, DataAccessHook.When.PRE, DataAccessHook.What.UPDATE);
        runner.runDataAccessHooks(entity, SimpleEntity.class, DataAccessHook.When.POST, DataAccessHook.What.UPDATE);
        runner.runDataAccessHooks(entity, SimpleEntity.class, DataAccessHook.When.PRE, DataAccessHook.What.DELETE);
        runner.runDataAccessHooks(entity, SimpleEntity.class, DataAccessHook.When.POST, DataAccessHook.What.DELETE);

        verifyAll();
    }

    @Test
    public void testQueryHook() {

        Query query = createMock(Query.class);
        Map mockMap = createMock(Map.class);

        expect(query.getQueryMap()).andReturn(mockMap);
        expectLastCall().times(2);
        expect(mockMap.put(eq(QueryReadHook.key), eq(QueryReadHook.newValue))).andReturn(null);
        expectLastCall().times(2);

        replayAll();
        runner.runDataAccessHooks(query, SimpleEntity.class, DataAccessHook.When.PRE, DataAccessHook.What.READ);
        runner.runDataAccessHooks(query, SimpleEntity.class, DataAccessHook.When.POST, DataAccessHook.What.READ);
        verifyAll();
    }

    @Test
    public void testCollectionOfEntityHook_PreCreate() {
        validate.validate(isA(IsStorable.class));
        expectLastCall().atLeastOnce();

        String data = "String";
        SimpleEntity simpleMockOne = createMock(SimpleEntity.class);
        SimpleEntity simpleMockTwo = createMock(SimpleEntity.class);

        expect(simpleMockOne.getData()).andReturn(data);
        expectLastCall().times(8);
        simpleMockOne.setData(eq(data + CreateHook.adjusted));
        expectLastCall().times(8);

        expect(simpleMockTwo.getData()).andReturn(data);
        expectLastCall().times(8);
        simpleMockTwo.setData(eq(data + CreateHook.adjusted));
        expectLastCall().times(8);

        List<SimpleEntity> list = Arrays.asList(new SimpleEntity[]{simpleMockOne, simpleMockTwo});

        replayAll();

        runner.runDataAccessHooks(
            list, SimpleEntity.class, DataAccessHook.When.PRE, DataAccessHook.What.CREATE
        );
        runner.runDataAccessHooks(
            list, SimpleEntity.class, DataAccessHook.When.POST, DataAccessHook.What.CREATE
        );
        runner.runDataAccessHooks(
            list, SimpleEntity.class, DataAccessHook.When.PRE, DataAccessHook.What.READ
        );
        runner.runDataAccessHooks(
            list, SimpleEntity.class, DataAccessHook.When.POST, DataAccessHook.What.READ
        );
        runner.runDataAccessHooks(
            list, SimpleEntity.class, DataAccessHook.When.PRE, DataAccessHook.What.UPDATE
        );
        runner.runDataAccessHooks(
            list, SimpleEntity.class, DataAccessHook.When.POST, DataAccessHook.What.UPDATE
        );
        runner.runDataAccessHooks(
            list, SimpleEntity.class, DataAccessHook.When.PRE, DataAccessHook.What.DELETE
        );
        runner.runDataAccessHooks(
            list, SimpleEntity.class, DataAccessHook.When.POST, DataAccessHook.What.DELETE
        );

        verifyAll();
    }
}
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.client.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pjherring.mongogwt.shared.BaseDomainObject;
import org.pjherring.mongogwt.shared.exceptions.UniqueException;
import org.pjherring.mongogwt.testing.guice.MongoClientModule;
import static org.junit.Assert.*;

/**
 *
 * @author pjherring
 */
public class TestingRpcDatabaseTest {

    private RpcDatabase rpcDatabase;
    private TestingRpcDatabase testingRpcDatabase;
    private static final Injector injector
        = Guice.createInjector(new MongoClientModule());

    public static class DummyDomain extends BaseDomainObject {

        private String string;

        public DummyDomain(String string) {
            this.string = string;
        }

        public void setString(String string) {
            this.string = string;
        }

        public String getString() {
            return string;
        }
    }

    public TestingRpcDatabaseTest() {
        rpcDatabase = injector.getInstance(RpcDatabase.class);
        testingRpcDatabase = (TestingRpcDatabase) rpcDatabase;
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

    /*
     * Only wrote two tests because all the logic is the same for every step.
     * Probably means the TestingRpcDatabase should be refactored to simplify
     * but that is a nice not a need.
     */
    @Test
    public void testCreateSuccess() {
        final DummyDomain domain = new DummyDomain("blah");

        testingRpcDatabase.addToCreate(domain, true);

        rpcDatabase.create(domain, DummyDomain.class, new AsyncCallback<DummyDomain>() {

            public void onFailure(Throwable caught) {
                assertFalse("SHOULD NOT BE HERE", true);
            }

            public void onSuccess(DummyDomain result) {
                assertSame(result, domain);
            }
        });

    }

    @Test
    public void testCreateFailure() {
        final DummyDomain domain = new DummyDomain("some");
        testingRpcDatabase.addToCreate(new UniqueException(), false);
        rpcDatabase.create(domain, DummyDomain.class, new AsyncCallback<DummyDomain>() {

            public void onFailure(Throwable caught) {
                assertTrue(caught instanceof UniqueException);
            }

            public void onSuccess(DummyDomain result) {
                throw new AssertionError();
            }
        });
    }

}
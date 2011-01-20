/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.client.rpc;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.logging.Logger;
import org.pjherring.mongogwt.client.gin.MongoGwtGinjector;
import org.pjherring.mongogwt.shared.domain.SimpleDomain;
import org.pjherring.mongogwt.shared.exception.NotFoundException;
import org.pjherring.mongogwt.shared.exception.NullableException;
import org.pjherring.mongogwt.shared.exception.UniqueException;
import org.pjherring.mongogwt.shared.query.Query;

/**
 *
 * @author pjherring
 */
public class GwtTestRpcDatabase extends GWTTestCase {

    private final static Logger LOG = Logger.getLogger(GwtTestRpcDatabase.class.getName());
    private RpcDatabase rpcDatabase;
    private MongoGwtGinjector injector;
    private HelperAsync helper;

    @Override
    public String getModuleName() {
        return "org.pjherring.mongogwt.Test";
    }

    @Override
    protected void gwtSetUp() {
        injector = GWT.create(MongoGwtGinjector.class);
        rpcDatabase = injector.getRpcDatabase();
        helper = injector.getHelper();
    }

    public void testCreateSuccess() {
        helper.dumpDatabase(new AsyncCallback<Void>() {

            public void onFailure(Throwable caught) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onSuccess(Void result) {
                SimpleDomain simple = new SimpleDomain();
                simple.setData("some data here");
                rpcDatabase
                    .create(simple, SimpleDomain.class, new AsyncCallback<SimpleDomain>() {

                        public void onFailure(Throwable caught) {
                            LOG.warning(caught.getClass().getName());
                            LOG.warning(caught.getMessage());
                            throw new AssertionError("Should not be in onFailure");
                        }

                        public void onSuccess(SimpleDomain result) {
                            assertNotNull(result.getId());
                            finishTest();
                        }
                    }
                );

            }
        });
        delayTestFinish(5000);
    }


    public void testCreateFailureNullException() {
        helper.dumpDatabase(new AsyncCallback<Void>() {

            public void onFailure(Throwable caught) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onSuccess(Void result) {
                SimpleDomain simple = new SimpleDomain();
                rpcDatabase
                    .create(simple, SimpleDomain.class, new AsyncCallback<SimpleDomain>() {

                        public void onFailure(Throwable caught) {
                            assertEquals(caught.getClass(), NullableException.class);
                            finishTest();
                        }

                        public void onSuccess(SimpleDomain result) {
                            LOG.warning("SUCCEEDED WHEN IT SHOULD NOT HAVE");
                            throw new AssertionError("SHOULD FAIL");
                        }
                    }
                );

            }
        });
        delayTestFinish(5000);
    }

    public void testCreateFailureUniqueException() {
        helper.dumpDatabase(new AsyncCallback<Void>() {

            public void onFailure(Throwable result) {}

            public void onSuccess(Void result) {
                SimpleDomain simple = new SimpleDomain();
                simple.setData("unique");

                rpcDatabase
                    .create(simple, SimpleDomain.class, new AsyncCallback<SimpleDomain>() {

                        public void onFailure(Throwable caught) {
                            LOG.warning("FAILED WHEN IT SHOULD NOT HAVE: " + caught.getClass().getName());
                            throw new AssertionError(caught.getClass().getName());
                        }

                        public void onSuccess(SimpleDomain result) {
                            rpcDatabase.create(result, SimpleDomain.class, new AsyncCallback<SimpleDomain>() {

                                public void onFailure(Throwable caught) {
                                    assertEquals(caught.getClass(), UniqueException.class);
                                    LOG.info("ABOU T TO FINISH TEST");
                                    finishTest();
                                }

                                public void onSuccess(SimpleDomain result) {
                                    LOG.warning("SUCCEEDED WHEN IT SHOULD HAVE FAILED");
                                    throw new AssertionError("SHOULD FAIL");
                                }
                            });
                        }
                    }
                );
            }
        });

        delayTestFinish(100000);
    }

    public void testReadSuccess() {
        helper.dumpDatabase(new AsyncCallback<Void>() {

            public void onFailure(Throwable caught) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onSuccess(Void result) {
                SimpleDomain simple = new SimpleDomain();
                simple.setData("testReadSuccess");
                rpcDatabase.create(simple, SimpleDomain.class, new AsyncCallback<SimpleDomain>() {

                    public void onFailure(Throwable caught) {
                        LOG.warning(caught.getClass().getName());
                        throw new AssertionError(caught.getClass().getName());
                    }

                    public void onSuccess(SimpleDomain result) {
                        final SimpleDomain copy = result;
                        rpcDatabase.findOne(
                            new Query().start("_id").is(result.getId()), SimpleDomain.class, true, new AsyncCallback<SimpleDomain>() {

                            public void onFailure(Throwable caught) {
                                LOG.warning(caught.getClass().getName());
                                throw new AssertionError(caught.getClass().getName());
                            }

                            public void onSuccess(SimpleDomain result) {
                                assertEquals(result.getId(), copy.getId());
                                finishTest();
                            }
                        });
                    }
                });

            }
        });
        delayTestFinish(5000);
    }

    public void testReadFailure() {
        helper.dumpDatabase(new AsyncCallback<Void>() {

            public void onFailure(Throwable caught) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onSuccess(Void result) {
                rpcDatabase.findOne(new Query().start("data").is("some bad value"), SimpleDomain.class, true, new AsyncCallback<SimpleDomain>() {

                    public void onFailure(Throwable caught) {
                        assertEquals(caught.getClass(), NotFoundException.class);
                        finishTest();
                    }

                    public void onSuccess(SimpleDomain result) {
                        LOG.warning("SUCCEEDED WHEN SHOULD HAVE FAILED");
                        throw new AssertionError("SHOULD NOT BE HERE DID NOT STORE");
                    }
                });
            }
        });
        delayTestFinish(5000);
    }

    public void testUpdateSuccess() {
        helper.dumpDatabase(new AsyncCallback<Void>() {

            public void onFailure(Throwable caught) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onSuccess(Void result) {
                SimpleDomain simple = new SimpleDomain();
                simple.setData("data");
                rpcDatabase.create(simple, SimpleDomain.class, new AsyncCallback<SimpleDomain>() {

                    public void onFailure(Throwable caught) {
                        throw new AssertionError("Creation Failed");
                    }

                    public void onSuccess(SimpleDomain result) {
                        finishTest();
                    }
                });
            }
        });
        delayTestFinish(5000);
    }
}

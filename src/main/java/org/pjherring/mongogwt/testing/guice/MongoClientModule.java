/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.testing.guice;


import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.pjherring.mongogwt.client.rpc.RpcDatabase;
import org.pjherring.mongogwt.server.rpc.TestingRpcDatabase;

/**
 *
 * @author pjherring
 */
public class MongoClientModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(RpcDatabase.class).to(TestingRpcDatabase.class).in(Singleton.class);
    }


}

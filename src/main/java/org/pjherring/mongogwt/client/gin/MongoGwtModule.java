/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.client.gin;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;
import org.pjherring.mongogwt.client.rpc.RpcDatabase;
import org.pjherring.mongogwt.client.rpc.RpcDatabaseImpl;

/**
 *
 * @author pjherring
 */
public class MongoGwtModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(RpcDatabase.class).to(RpcDatabaseImpl.class).in(Singleton.class);
    }

}

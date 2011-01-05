/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.client.gin;


import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import org.pjherring.mongogwt.client.rpc.HelperAsync;
import org.pjherring.mongogwt.client.rpc.RpcDatabase;

/**
 *
 * @author pjherring
 */
@GinModules({MongoGwtModule.class})
public interface MongoGwtGinjector extends Ginjector {
    RpcDatabase getRpcDatabase();
    HelperAsync getHelper();
}

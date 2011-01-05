/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.client.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 *
 * @author pjherring
 */
public interface HelperAsync {

    public void dumpDatabase(AsyncCallback<Void> asyncCallback);

}

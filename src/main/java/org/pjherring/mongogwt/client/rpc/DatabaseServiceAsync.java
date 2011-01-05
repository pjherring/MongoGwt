/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.client.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;
import org.pjherring.mongogwt.shared.IsDomainObject;
import org.pjherring.mongogwt.shared.query.Query;

/**
 *
 * @author pjherring
 */
public interface DatabaseServiceAsync {

    void create(IsDomainObject domainObject, AsyncCallback<IsDomainObject> callback);
    public void find(Query query, String type, boolean doFanOut, AsyncCallback<List<IsDomainObject>> asyncCallback);
    public void findOne(Query query, String type, boolean doFanOut, AsyncCallback<IsDomainObject> asyncCallback);
    public void update(IsDomainObject domainObject, AsyncCallback<IsDomainObject> asyncCallback);
    public void delete(Query query, String type, AsyncCallback<Void> asyncCallback);
    public void refresh(IsDomainObject domainObject, String type, AsyncCallback<IsDomainObject> asyncCallback);
    public void count(Query query, String type, AsyncCallback<Long> asyncCallback);
}

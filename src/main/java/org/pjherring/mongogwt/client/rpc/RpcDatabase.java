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
public interface RpcDatabase {

    public <T extends IsDomainObject> void create(
        final IsDomainObject domainObject,
        Class<T> type,
        final AsyncCallback<T> callback);

    public <T extends IsDomainObject> void find(
        final Query query,
        Class<T> type,
        boolean doFanOut,
        final AsyncCallback<List<T>> callback);

    public <T extends IsDomainObject> void findOne(
        final Query query,
        final Class<T> type,
        boolean doFanOut,
        final AsyncCallback<T> callback);

    public <T extends IsDomainObject> void update(
        final T isDomainObject,
        final AsyncCallback<T> callback);

    public void delete(
        final Query query,
        Class type,
        final AsyncCallback<Void> callback);

    public <T extends IsDomainObject> void refresh(
        final IsDomainObject domainObject,
        Class<T> type,
        final AsyncCallback<T> callback);
}

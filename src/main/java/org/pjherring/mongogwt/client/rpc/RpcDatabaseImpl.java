package org.pjherring.mongogwt.client.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.pjherring.mongogwt.shared.IsDomainObject;
import org.pjherring.mongogwt.shared.query.Query;

/**
 *
 * @author pjherring
 */
public class RpcDatabaseImpl implements RpcDatabase {

    protected DatabaseServiceAsync service;

    @Inject
    public RpcDatabaseImpl(DatabaseServiceAsync service) {
        this.service = service;
    }

    public <T extends IsDomainObject> void create(final IsDomainObject domainObject,
        Class<T> type,
        final AsyncCallback<T> callback) {
        service.create(domainObject, new AsyncCallback<IsDomainObject>() {

            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            public void onSuccess(IsDomainObject result) {
                domainObject.setId(result.getId());
                domainObject.setCreatedDatetime(result.getCreatedDatetime());
                callback.onSuccess((T) result);
            }
        });

    }

    public <T extends IsDomainObject> void find(
        final Query query,
        Class<T> type,
        boolean doFanOut,
        final AsyncCallback<List<T>> callback) {

        service.find(query, type.getName(), doFanOut, new AsyncCallback<List<IsDomainObject>>() {

            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            public void onSuccess(List<IsDomainObject> result) {
                List<T> castedResults = new ArrayList<T>();
                for (IsDomainObject isDomainObject : result) {
                    castedResults.add((T) isDomainObject);
                }
                callback.onSuccess(castedResults);
            }
        });

    }

    public <T extends IsDomainObject> void findOne(
        final Query query,
        final Class<T> type,
        boolean doFanOut,
        final AsyncCallback<T> callback) {
        service.findOne(query, type.getName(), doFanOut, new AsyncCallback<IsDomainObject>() {

            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            public void onSuccess(IsDomainObject result) {
                callback.onSuccess((T) result);
            }
        });
    }

    public <T extends IsDomainObject> void update(
        final T isDomainObject,
        final AsyncCallback<T> callback) {
        service.update(isDomainObject, new AsyncCallback<IsDomainObject>() {

            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            public void onSuccess(IsDomainObject result) {
                callback.onSuccess((T) result);
            }
        });
    }

    public void delete(final Query query, Class type, final AsyncCallback<Void> callback) {
        service.delete(query, type.getName(), callback);
    }

    public <T extends IsDomainObject> void refresh(
        final IsDomainObject domainObject,
        Class<T> type,
        final AsyncCallback<T> callback) {
        service.refresh(domainObject, type.getName(), new AsyncCallback<IsDomainObject>() {

            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            public void onSuccess(IsDomainObject result) {
                callback.onSuccess((T) result);
            }
        });
    }
}
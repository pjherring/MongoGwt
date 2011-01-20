package org.pjherring.mongogwt.client.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.pjherring.mongogwt.shared.IsEntity;
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

    public <T extends IsEntity> void create(final IsEntity domainObject,
        Class<T> type,
        final AsyncCallback<T> callback) {
        service.create(domainObject, new AsyncCallback<IsEntity>() {

            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            public void onSuccess(IsEntity result) {
                domainObject.setId(result.getId());
                domainObject.setCreatedDatetime(result.getCreatedDatetime());
                callback.onSuccess((T) result);
            }
        });

    }

    public <T extends IsEntity> void find(
        final Query query,
        Class<T> type,
        boolean doFanOut,
        final AsyncCallback<List<T>> callback) {

        service.find(query, type.getName(), doFanOut, new AsyncCallback<List<IsEntity>>() {

            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            public void onSuccess(List<IsEntity> result) {
                List<T> castedResults = new ArrayList<T>();
                for (IsEntity isDomainObject : result) {
                    castedResults.add((T) isDomainObject);
                }
                callback.onSuccess(castedResults);
            }
        });

    }

    public <T extends IsEntity> void findOne(
        final Query query,
        final Class<T> type,
        boolean doFanOut,
        final AsyncCallback<T> callback) {
        service.findOne(query, type.getName(), doFanOut, new AsyncCallback<IsEntity>() {

            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            public void onSuccess(IsEntity result) {
                callback.onSuccess((T) result);
            }
        });
    }

    public <T extends IsEntity> void update(
        final T isDomainObject,
        final AsyncCallback<T> callback) {
        service.update(isDomainObject, new AsyncCallback<IsEntity>() {

            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            public void onSuccess(IsEntity result) {
                callback.onSuccess((T) result);
            }
        });
    }

    public void delete(final Query query, Class type, final AsyncCallback<Void> callback) {
        service.delete(query, type.getName(), callback);
    }

    public <T extends IsEntity> void refresh(
        final IsEntity domainObject,
        Class<T> type,
        final AsyncCallback<T> callback) {
        service.refresh(domainObject, type.getName(), new AsyncCallback<IsEntity>() {

            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            public void onSuccess(IsEntity result) {
                callback.onSuccess((T) result);
            }
        });
    }
}
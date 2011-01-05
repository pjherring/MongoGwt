/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.client.rpc;


import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import org.pjherring.mongogwt.shared.IsDomainObject;
import org.pjherring.mongogwt.shared.query.Query;

/**
 *
 * @author pjherring
 */
public class TestingRpcDatabase implements RpcDatabase {

    public static class NoResponseStoredExpcetion extends RuntimeException {

        public NoResponseStoredExpcetion() {
            super("No response stored.");
        }

    }

    private Queue createQueue = new PriorityQueue();
    private Queue<Boolean> doCreateSuccessQueue = new PriorityBlockingQueue<Boolean>();

    private Queue findQueue = new PriorityQueue();
    private Queue<Boolean> doFindSuccessQueue
        = new PriorityBlockingQueue<Boolean>();

    private Queue updateQueue = new PriorityQueue();
    private Queue<Boolean> doUpdateSuccessQueue
        = new PriorityBlockingQueue<Boolean>();

    private Queue deleteQueue = new PriorityQueue();
    private Queue<Boolean> doDeleteSuccessQueue
        = new PriorityBlockingQueue<Boolean>();

    private Queue refreshQueue = new PriorityQueue();
    private Queue<Boolean> doRefreshSuccessQueue
        = new PriorityBlockingQueue<Boolean>();

    public void addToCreate(Object toBeReturned, boolean isSuccess) {
        createQueue.add(toBeReturned);
        doCreateSuccessQueue.add(isSuccess);
    }

    public void addToFind(Object toBeReturned, boolean isSuccess) {
        findQueue.add(toBeReturned);
        doFindSuccessQueue.add(isSuccess);
    }

    public void addToUpdate(Object toBeReturned, boolean isSuccess) {
        updateQueue.add(toBeReturned);
        doUpdateSuccessQueue.add(isSuccess);
    }

    public void addToDelete(Object toBeReturned, boolean isSuccess) {
        deleteQueue.add(toBeReturned);
        doDeleteSuccessQueue.add(isSuccess);
    }

    public void addToRefresh(Object toBeReturned, boolean isSuccess) {
        refreshQueue.add(toBeReturned);
        doRefreshSuccessQueue.add(isSuccess);
    }

    public <T extends IsDomainObject> void create(IsDomainObject domainObject, Class<T> type, AsyncCallback<T> callback) {
        if (createQueue.size() > 0 && doCreateSuccessQueue.size() > 0) {
            boolean doSuccess = doCreateSuccessQueue.remove();
            if (doSuccess) {
                callback.onSuccess((T) createQueue.remove());
            } else {
                callback.onFailure((Throwable) createQueue.remove());
            }
        } else {
            throw new NoResponseStoredExpcetion();
        }
    }

    public <T extends IsDomainObject> void find(Query query, Class<T> type, boolean doFanOut, AsyncCallback<List<T>> callback) {
        if (findQueue.size() > 0 && doFindSuccessQueue.size() > 0) {
            if (doFindSuccessQueue.remove()) {
                callback.onSuccess((List<T>) findQueue.remove());
            } else {
                callback.onFailure((Throwable) findQueue.remove());
            }
        } else {
            throw new NoResponseStoredExpcetion();
        }
    }

    public <T extends IsDomainObject> void findOne(Query query, Class<T> type, boolean doFanOut, AsyncCallback<T> callback) {
        if (findQueue.size() > 0 && doFindSuccessQueue.size() > 0) {
            if (doFindSuccessQueue.remove()) {
                callback.onSuccess((T) findQueue.remove());
            } else {
                callback.onFailure((Throwable) findQueue.remove());
            }
        } else {
            throw new NoResponseStoredExpcetion();
        }
    }

    public <T extends IsDomainObject> void update(T isDomainObject, AsyncCallback<T> callback) {
        if (updateQueue.size() > 0 && doUpdateSuccessQueue.size() > 0) {
            if (doUpdateSuccessQueue.remove()) {
                callback.onSuccess((T) updateQueue.remove());
            } else {
                callback.onFailure((Throwable) updateQueue.remove());
            }
        } else {
            throw new NoResponseStoredExpcetion();
        }
    }

    public void delete(Query query, Class type, AsyncCallback<Void> callback) {
        if (deleteQueue.size() > 0 && doDeleteSuccessQueue.size() > 0) {
            if (doDeleteSuccessQueue.remove()) {
                //NOTICE HERE WE ARE NOT PASSING IT BACK BECAUSE DELETE'S
                //CALLBACK HAS A VOID RETURN
                deleteQueue.remove();
                callback.onSuccess(null);
            } else {
                callback.onFailure((Throwable) deleteQueue.remove());
            }
        } else {
            throw new NoResponseStoredExpcetion();
        }
    }

    public <T extends IsDomainObject> void refresh(IsDomainObject domainObject, Class<T> type, AsyncCallback<T> callback) {
        if (refreshQueue.size() > 0 && doRefreshSuccessQueue.size() > 0) {
            if (doRefreshSuccessQueue.remove()) {
                callback.onSuccess((T) refreshQueue.remove());
            } else {
                callback.onFailure((Throwable) refreshQueue.remove());

            }
        }
    }

}

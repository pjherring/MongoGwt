/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.rpc;


import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.logging.Logger;

/**
 *
 * @author pjherring
 */
public class AsyncCallbackMock<T> implements AsyncCallback<T> {

    private final static Logger LOG = Logger.getLogger(AsyncCallbackMock.class.getName());
    public boolean doSuccess = true;

    Queue<Throwable> failureQueue = new PriorityQueue<Throwable>();
    Queue<T> successQueue = new PriorityQueue<T>();



    public void nextSuccess(T object) {
        successQueue.add(object);
        doSuccess = true;
    }

    public void nextFailure(Throwable caught) {
        failureQueue.add(caught);
        doSuccess = false;
    }

    public boolean doSuccess() {
        return doSuccess;
    }

    public T getSuccess() {
        return successQueue.remove();
    }

    public Throwable getFailure() {
        return failureQueue.remove();
    }

    public void onFailure(Throwable caught) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onSuccess(T result) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}

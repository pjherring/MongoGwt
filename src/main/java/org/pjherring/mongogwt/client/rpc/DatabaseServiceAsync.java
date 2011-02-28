/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.client.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.query.Query;

/**
 *
 * @author pjherring
 */
public interface DatabaseServiceAsync {

    public void create(IsEntity domainObject, AsyncCallback<IsEntity> asyncCallback);

    public void find(Query query, String type, boolean doFanOut, AsyncCallback<List<IsEntity>> asyncCallback);

    public void findOne(Query query, String type, boolean doFanOut, AsyncCallback<IsEntity> asyncCallback);

    public void findById(String id, String type, boolean doFanOut, AsyncCallback<IsEntity> asyncCallback);

    public void update(IsEntity domainObject, AsyncCallback<IsEntity> asyncCallback);

    public void delete(Query query, String type, AsyncCallback<Void> asyncCallback);

    public void delete(IsEntity entity, AsyncCallback<Void> asyncCallback);

    public void refresh(IsEntity domainObject, String type, AsyncCallback<IsEntity> asyncCallback);

}

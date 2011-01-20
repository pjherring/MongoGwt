/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.hook;

import org.pjherring.mongogwt.shared.query.Query;
import org.pjherring.mongogwt.shared.IsEntity;

/**
 *
 * @author pjherring
 */
public abstract class BaseDataAccessHook<T extends IsEntity> {

    protected T domainObject;
    protected Query query;

    public abstract void run();

    public void setDomainObject(T domainObject) {
        this.domainObject = domainObject;
    }

    public void setQuery(Query query) {
        this.query = query;
    }
}

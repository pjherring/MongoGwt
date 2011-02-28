/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.hook;

import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.query.Query;

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

    protected T getDomainObject() {
        return domainObject;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    protected Query getQuery() {
        return query;
    }

    public boolean doRun() {
        return true;
    }
}

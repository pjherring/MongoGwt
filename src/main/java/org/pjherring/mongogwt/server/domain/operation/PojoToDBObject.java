/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;

import com.google.inject.ImplementedBy;
import com.mongodb.DBObject;
import org.pjherring.mongogwt.shared.IsStorable;

/**
 *
 * @author pjherring
 */
@ImplementedBy(PojoToDBObjectImpl.class)
public interface PojoToDBObject {
    public long getCacheHitCount();
    public long getCacheMissCount();
    public void resetCahce();
    public <T extends IsStorable> DBObject translate(T isStorable);
}

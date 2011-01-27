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
@ImplementedBy(DBObjectToPojoImpl.class)
public interface DBObjectToPojo {

    <T extends IsStorable> T translate(DBObject dbObject, Class<T> type, boolean doFanOut);
    long getCacheHits();
    long getCacheMisses();
    void resetCache();
}

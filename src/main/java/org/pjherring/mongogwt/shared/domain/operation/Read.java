/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.domain.operation;

import com.google.inject.ImplementedBy;
import java.util.List;
import org.pjherring.mongogwt.server.domain.operation.ReadImpl;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.query.Query;

/**
 *
 * @author pjherring
 */
@ImplementedBy(ReadImpl.class)
public interface Read {
    <T extends IsEntity> List<T> find(Query query, Class<T> clazz, boolean doFanOut);
    <T extends IsEntity> T findOne(Query query, Class<T> clazz, boolean doFanOut);
    <T extends IsEntity> T findById(String id, Class<T> clazz, boolean doFanOut);
}

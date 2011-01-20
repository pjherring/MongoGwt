/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.domain.operation;

import org.pjherring.mongogwt.shared.IsEntity;
import java.util.List;
import org.pjherring.mongogwt.shared.query.Query;

/**
 *
 * @author pjherring
 */
public interface DoesRead {
    <T extends IsEntity> List<T>
        find(Query query, Class<T> type, boolean doFanOut);

    <T extends IsEntity> T
        findOne(Query query, Class<T> type, boolean doFanOut);

    <T extends IsEntity> T
        findById(String id, Class<T> type, boolean doFanOut);

    <T extends IsEntity>
        Long count(Query query, Class<T> type);
}

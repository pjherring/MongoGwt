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
public interface Database {

    void create(IsEntity domainObject);

    void update(IsEntity domainObject);

    <T extends IsEntity> List<T>
        find(Query query, Class<T> type, boolean doFanOut);

    <T extends IsEntity> T
        findOne(Query query, Class<T> type, boolean doFanOut);

    void delete(Query query, Class<? extends IsEntity> type);

    <T extends IsEntity> void delete(T domainObject);

    <T extends IsEntity> T refresh(IsEntity domainObject, Class<T> type);

    <T extends IsEntity> Long count(Query query, Class<T> type);

}
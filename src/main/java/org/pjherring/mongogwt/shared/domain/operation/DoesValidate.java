/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.domain.operation;

import org.pjherring.mongogwt.shared.annotations.Column;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.IsDomainObject;

/**
 *
 * @author pjherring
 */
public interface DoesValidate {
    void validatePojo(IsDomainObject pojoToValidate);
    void validateColumn(
        Column columnAnnotation,
        Object value,
        Entity domainCollectionAnnotation
    );
    void validateCollection(Entity domainCollection);
    void validateCollection(Class<? extends IsDomainObject> clazz);

}

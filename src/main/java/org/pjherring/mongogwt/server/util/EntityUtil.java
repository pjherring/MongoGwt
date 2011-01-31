/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.util;


import java.util.logging.Logger;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.exception.InvalidEntity;

/**
 *
 * @author pjherring
 */
public class EntityUtil {

    private final static Logger LOG = Logger.getLogger(EntityUtil.class.getName());

    public String getCollectionName(IsEntity entity) {
        if (entity.getClass().isAnnotationPresent(Entity.class)) {
            return entity.getClass().getAnnotation(Entity.class)
                .name();
        }

        LOG.warning(entity.getClass().getName() + " is not annotated with @Entity");
        throw new InvalidEntity(entity.getClass().getName());
    }

}

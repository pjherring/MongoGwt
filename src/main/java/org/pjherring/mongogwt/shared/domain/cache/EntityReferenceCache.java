/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.domain.cache;


import com.google.inject.Singleton;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.annotations.Column;
import org.pjherring.mongogwt.shared.annotations.Reference;
import org.pjherring.mongogwt.shared.annotations.enums.ReferenceType;

/**
 *
 * @author pjherring
 */
@Singleton
public class EntityReferenceCache {

    private final static Logger LOG = Logger.getLogger(EntityReferenceCache.class.getName());

    /*
     * This is a simple pojo to carry data about a Reference
     */
    public static class ReferenceData {

        private Class<? extends IsEntity> referencedClass;
        private Reference reference;
        private Method getter;
        private Method setter;

        public Class<? extends IsEntity> getReferencedClass() {
            return referencedClass;
        }

        public void setReferencedClass(Class<? extends IsEntity> referencedClass) {
            this.referencedClass = referencedClass;
        }

        public Reference getReference() {
            return reference;
        }

        public void setReference(Reference reference) {
            this.reference = reference;
        }

        public Method getGetter() {
            return getter;
        }

        public void setGetter(Method getter) {
            this.getter = getter;
        }

        public Method getSetter() {
            return setter;
        }

        public void setSetter(Method setter) {
            this.setter = setter;
        }
    }

    protected Map<Class<? extends IsEntity>, List<ReferenceData>> entityCacheMap
        = new HashMap<Class<? extends IsEntity>, List<ReferenceData>>();

    /*
     * Convenience method to clear the cache. This is mostly for testing, and
     * should not be called in the context of the application.
     */
    public void clearCache() {
        entityCacheMap.clear();
    }

    public List<ReferenceData> getReferences(Class<? extends IsEntity> clazz) {
        if (!entityCacheMap.containsKey(clazz)) {
            entityCacheMap.put(clazz, getEntityReferences(clazz));
        }

        return entityCacheMap.get(clazz);
    }

    /*
     * This composes a list of ReferenceData for each entity. Should only run
     * once for each entity.
     *
     * @param clazz The class of the entity to compose list for.
     *
     * @return The list of ReferenceData
     */
    protected List<ReferenceData> getEntityReferences(Class<? extends IsEntity> clazz) {

        List<ReferenceData> referenceData = new ArrayList<ReferenceData>();

        for (Method getter : clazz.getMethods()) {

            if (getter.isAnnotationPresent(Reference.class)) {

                Reference reference = getter.getAnnotation(Reference.class);

                /*
                 * Make sure this is a non managed entity
                 */
                if (!reference.managedBy().equals("")
                    && !getter.isAnnotationPresent(Column.class)) {

                    ReferenceData data = new ReferenceData();

                    data.setReference(reference);
                    data.setGetter(getter);

                    //get the Class of the entity referenced
                    if (reference.type().equals(ReferenceType.ONE_TO_MANY)) {
                        /*
                         * have to use the getter here to get the type of
                         * the entity referenced
                         */
                        ParameterizedType parameterizedType
                            = (ParameterizedType) getter.getGenericReturnType();
                        Type[] type =
                            parameterizedType.getActualTypeArguments();
                        Class referenceType = (Class) type[0];
                        data.setReferencedClass(referenceType);

                    } else {
                        data.setReferencedClass(
                            (Class<? extends IsEntity>) getter.getReturnType()
                        );
                    }

                    //get the setter
                    try {
                        data.setSetter(
                            clazz.getMethod(
                                "s" + getter.getName().substring(1),
                                getter.getReturnType()
                            )
                        );
                    } catch (Exception e) {
                        LOG.warning("Problems getting setter for: "
                            + clazz.getName() + " off of getter: "
                            + getter.getName()
                        );
                        throw new RuntimeException(e);
                    }

                    referenceData.add(data);
                }
            }

        }

        return referenceData;
    }

}

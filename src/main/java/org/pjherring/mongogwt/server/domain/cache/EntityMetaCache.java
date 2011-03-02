/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.cache;


import com.google.inject.Singleton;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.IsStorable;
import org.pjherring.mongogwt.shared.annotations.Column;
import org.pjherring.mongogwt.shared.annotations.Reference;
import org.pjherring.mongogwt.shared.annotations.enums.ReferenceType;
import org.pjherring.mongogwt.shared.domain.validator.Validator;
import org.pjherring.mongogwt.shared.domain.validator.ValidatorHook;

/**
 *
 * @author pjherring
 */
@Singleton
public class EntityMetaCache {

    private final static Logger LOG = Logger.getLogger(EntityMetaCache.class.getName());
    public static class ColumnMeta {
        private Method getter;
        private Method setter;
        private Column columnAnnotation;

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

        public Column getColumnAnnotation() {
            return columnAnnotation;
        }

        public void setColumnAnnotation(Column columnAnnotation) {
            this.columnAnnotation = columnAnnotation;
        }
    }

    public static class ReferenceMeta {

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

    protected Map<Class<? extends IsStorable>, Set<ColumnMeta>> entityColumnMetaCacheMap
        = new HashMap<Class<? extends IsStorable>, Set<ColumnMeta>>();
    private Long entityColumnMetaCacheHits = 0L;
    private Long entityColumnMetaCacheMisses = 0L;

    protected Map<Class<? extends IsEntity>, Set<ReferenceMeta>> entityColumnReferenceMetaCacheMap
        = new HashMap<Class<? extends IsEntity>, Set<EntityMetaCache.ReferenceMeta>>();

    protected Long entityColumnReferenceMetaCacheHits = 0L;
    protected Long entityColumnReferenceMetaCacheMisses = 0L;

    protected Map<Class<? extends IsEntity>, Set<ReferenceMeta>> entityNonColumnReferenceMetaCacheMap
        = new HashMap<Class<? extends IsEntity>, Set<EntityMetaCache.ReferenceMeta>>();
    protected Long entityNonColumnReferenceMetaCacheHits = 0L;
    protected Long entityNonColumnReferenceMetaCacheMisses = 0L;

    protected Map<Class<? extends IsStorable>, List<Validator>> validatorCache
        = new HashMap<Class<? extends IsStorable>, List<Validator>>();
    protected Long validationCacheHit = 0L;
    protected Long validationCacheMiss = 0L;


    public Set<ColumnMeta> getColumnMetaSet(Class<? extends IsStorable> clazz) {

        if (!entityColumnMetaCacheMap.containsKey(clazz)) {
            entityColumnMetaCacheMisses++;
            entityColumnMetaCacheMap.put(clazz, createEntityColumnMetaMap(clazz));
        } else {
            entityColumnMetaCacheHits++;
        }

        return entityColumnMetaCacheMap.get(clazz);
    }

    public Set<ReferenceMeta> getNonColumnReferenceMetaSet(Class<? extends IsEntity> clazz) {
        //check cache
        if (!entityNonColumnReferenceMetaCacheMap.containsKey(clazz)) {
            entityNonColumnReferenceMetaCacheMisses++;
            entityNonColumnReferenceMetaCacheMap.put(clazz, doGetNonColumnReferenceMetaSet(clazz));
        } else {
            entityNonColumnReferenceMetaCacheHits++;
        }

        return entityNonColumnReferenceMetaCacheMap.get(clazz);

    }

    public Set<ReferenceMeta> getColumnReferenceMetaSet(Class<? extends IsEntity> clazz) {
        //check cache
        if (!entityColumnReferenceMetaCacheMap.containsKey(clazz)) {
            entityColumnReferenceMetaCacheMisses++;
            entityColumnReferenceMetaCacheMap.put(clazz, doGetColumnReferenceMetaSet(clazz));
        } else {
            entityColumnReferenceMetaCacheHits++;
        }

        return entityColumnReferenceMetaCacheMap.get(clazz);
    }

    public Set<ReferenceMeta> getReferenceMetaSet(Class<? extends IsEntity> clazz) {
        Set<ReferenceMeta> toReturn = new HashSet<ReferenceMeta>();
        toReturn.addAll(getNonColumnReferenceMetaSet(clazz));
        toReturn.addAll(getColumnReferenceMetaSet(clazz));

        return toReturn;
    }

    public List<Validator> getValidatorList(Class<? extends IsStorable> clazz) {
        if (!validatorCache.containsKey(clazz)) {
            validationCacheMiss++;
            validatorCache.put(clazz, doGetValidatorList(clazz));
        } else {
            validationCacheHit++;
        }

        return validatorCache.get(clazz);
    }

    protected <T extends IsStorable> List<Validator> doGetValidatorList(Class<T> clazz) {
        if (clazz.isAnnotationPresent(ValidatorHook.class)) {

            List<Validator> validatorList = new ArrayList<Validator>();
            ValidatorHook validatorHook = clazz.getAnnotation(ValidatorHook.class);

            for (Class validatorClass : validatorHook.value()) {
                try {
                    //unsafe cast
                    Validator validator = (Validator) validatorClass.newInstance();
                    validatorList.add(validator);
                } catch (Exception e) {
                    LOG.warning("Error when trying to create an instance of validator " + validatorClass.getName());
                    throw new RuntimeException(e);
                }
            }

            return validatorList;
        }

        return null;
    }

    /*
     * This composes a list of ReferenceData for each entity. Should only run
     * once for each entity.
     *
     * @param clazz The class of the entity to compose list for.
     *
     * @return The list of ReferenceData
     */
    protected Set<ReferenceMeta> doGetNonColumnReferenceMetaSet(
        Class<? extends IsEntity> clazz) {

        Set<ReferenceMeta> referenceMetaSet = new HashSet<ReferenceMeta>();

        for (Method getter : clazz.getMethods()) {

            if (getter.isAnnotationPresent(Reference.class)) {

                Reference reference = getter.getAnnotation(Reference.class);

                /*
                 * Make sure this is a non managed entity
                 */
                if (!getter.isAnnotationPresent(Column.class)) {

                    ReferenceMeta referenceMeta = new ReferenceMeta();

                    referenceMeta.setReference(reference);
                    referenceMeta.setGetter(getter);
                    referenceMeta.setReferencedClass(getReferencedClass(
                        reference,
                        getter
                    ));

                    //get the setter
                    try {
                        referenceMeta.setSetter(
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

                    referenceMetaSet.add(referenceMeta);
                }
            }

        }

        return referenceMetaSet;
    }

    protected Set<ReferenceMeta> doGetColumnReferenceMetaSet(
        Class<? extends IsEntity> clazz) {

        Set<ReferenceMeta> referenceMetaSet = new HashSet<ReferenceMeta>();
        Set<ColumnMeta> columnMetaSet = getColumnMetaSet(clazz);

        //use the column meta set so we don't have to iterate through every method
        for (ColumnMeta columnMeta : columnMetaSet) {
            if (columnMeta.getGetter().isAnnotationPresent(Reference.class)) {
                ReferenceMeta referenceMeta = new ReferenceMeta();
                referenceMeta.setReference(
                    columnMeta.getGetter().getAnnotation(Reference.class)
                );
                referenceMeta.setGetter(columnMeta.getGetter());
                referenceMeta.setSetter(columnMeta.getSetter());
                referenceMeta.setReferencedClass(
                    getReferencedClass(referenceMeta.getReference(),
                    referenceMeta.getGetter())
                );

                referenceMetaSet.add(referenceMeta);
            }
        }

        return referenceMetaSet;
    }

    /*
     * Helper method for getting the Class<? extends IsEntity> that is referenced
     * by another entity.
     * 
     * @param reference The reference annotation
     * @param getter The method used to get this reference from an entity
     * 
     */
    protected Class<? extends IsEntity> getReferencedClass(
        Reference reference, Method getter) {

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
            return referenceType;

        } else {
            return (Class<? extends IsEntity>) getter.getReturnType();
        }

    }

    /*
     * Actually creates the translation map. In essence, this method links each
     * column (represented by a Column Annotation) with a Getter. The output
     * is a map with a String (column name) pointing to a Method (the getter).
     *
     * @param isStorable the Object we are creating a translation map for.
     *
     * @return the translation map
     */
    private Set<ColumnMeta> createEntityColumnMetaMap(Class<? extends IsStorable> clazz) {

        Set<ColumnMeta> columnMetaSet = new HashSet<ColumnMeta>();

        for (Method getter : clazz.getMethods()) {
            /*
             * We want this method if its a getter meaning no parameters
             * and annotated with column.
             */
            if (getter.isAnnotationPresent(Column.class)
                && getter.getParameterTypes().length == 0) {

                Column column = getter.getAnnotation(Column.class);
                ColumnMeta columnMeta = new ColumnMeta();
                columnMeta.setColumnAnnotation(column);
                columnMeta.setGetter(getter);

                try {
                    Method setter = clazz.getMethod(
                        "s" + getter.getName().substring(1),
                        getter.getReturnType()
                    );
                    columnMeta.setSetter(setter);
                } catch (Exception e) {
                    LOG.warning("FAILURE IN GETTING SETTER ON " + clazz.getName());
                    throw new RuntimeException(e);
                }
                
                columnMetaSet.add(columnMeta);
            }
        }

        return columnMetaSet;
    }

    public Long getEntityColumnMetaCacheHits() {
        return entityColumnMetaCacheHits;
    }

    public Long getEntityColumnMetaCacheMisses() {
        return entityColumnMetaCacheMisses;
    }

    public Long getNonColumnReferenceMetaCacheHits() {
        return entityNonColumnReferenceMetaCacheHits;
    }

    public Long getNonColumnReferenceMetaCacheMisses() {
        return entityNonColumnReferenceMetaCacheMisses;
    }

    public Long getColumnReferenceMetaCacheHits() {
        return entityColumnReferenceMetaCacheHits;
    }

    public Long getColumnReferenceMetaCacheMisses() {
        return entityColumnReferenceMetaCacheMisses;
    }

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.cache;

import org.pjherring.mongogwt.shared.IsEntity;
import java.util.List;
import org.pjherring.mongogwt.server.domain.cache.EntityMetaCache.ReferenceMeta;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pjherring.mongogwt.server.domain.cache.EntityMetaCache.ColumnMeta;
import org.pjherring.mongogwt.shared.BaseDomainObject;
import org.pjherring.mongogwt.shared.annotations.Column;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.annotations.Reference;
import org.pjherring.mongogwt.shared.annotations.enums.ReferenceType;
import static org.junit.Assert.*;

/**
 *
 * @author pjherring
 */
public class EntityMetaCacheTest {

    EntityMetaCache entityMetaCache;

    public EntityMetaCacheTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        entityMetaCache = new EntityMetaCache();
    }

    @After
    public void tearDown() {
    }

    @Entity(name="simple")
    public static class SimpleEntity extends BaseDomainObject {

        private String columnOne;
        private Integer columnTwo;
        private Date dateOne;

        @Column(name="columnOne")
        public String getColumnOne() {
            return columnOne;
        }

        public void setColumnOne(String columnOne) {
            this.columnOne = columnOne;
        }

        @Column(name="columnTwo")
        public Integer getColumnTwo() {
            return columnTwo;
        }

        public void setColumnTwo(Integer columnTwo) {
            this.columnTwo = columnTwo;
        }

        @Column(name="dateOne")
        public Date getDateOne() {
            return dateOne;
        }

        public void setDateOne(Date dateOne) {
            this.dateOne = dateOne;
        }

    }

    @Entity(name="ref")
    public static class RefEntity extends BaseDomainObject {
        private Set<SimpleEntity> simpleEntitySet;
        private List<SimpleEntity> simpleEntityList;
        private SimpleEntity nonColumnSimple;
        private SimpleEntity simpleManyToOne;
        private List<SimpleEntity> nonColumnSimpleEntityList;

        @Column(name="simpleSet")
        @Reference(type=ReferenceType.ONE_TO_MANY)
        public Set<SimpleEntity> getSimpleEntitySet() {
            return simpleEntitySet;
        }

        public void setSimpleEntitySet(Set<SimpleEntity> simpleEntitySet) {
            this.simpleEntitySet = simpleEntitySet;
        }

        @Column(name="simpleList")
        @Reference(type=ReferenceType.ONE_TO_MANY)
        public List<SimpleEntity> getSimpleEntityList() {
            return simpleEntityList;
        }

        public void setSimpleEntityList(List<SimpleEntity> simpleEntityList) {
            this.simpleEntityList = simpleEntityList;
        }

        @Reference(type=ReferenceType.ONE_TO_MANY)
        public List<SimpleEntity> getNonColumnSimpleEntityList() {
            return nonColumnSimpleEntityList;
        }

        public void setNonColumnSimpleEntityList(List<SimpleEntity> nonColumnSimpleEntityList) {
            this.nonColumnSimpleEntityList = nonColumnSimpleEntityList;
        }

        @Reference(type=ReferenceType.ONE_TO_ONE)
        public SimpleEntity getNonColumnSimple() {
            return nonColumnSimple;
        }

        public void setNonColumnSimple(SimpleEntity nonColumnSimple) {
            this.nonColumnSimple = nonColumnSimple;
        }

        @Reference(type=ReferenceType.MANY_TO_ONE)
        @Column(name="simpleManyToOne")
        public SimpleEntity getSimpleManyToOne() {
            return simpleManyToOne;
        }

        public void setSimpleManyToOne(SimpleEntity simpleManyToOne) {
            this.simpleManyToOne = simpleManyToOne;
        }
    }

    @Test
    public void testGetColumnMapMeta() {
        Set<ColumnMeta> columnMetaList =
            entityMetaCache.getColumnMetaSet(SimpleEntity.class);

        for (ColumnMeta column : columnMetaList) {

            String columnName = column.getColumnAnnotation().name();
            assertTrue(columnName.equals("columnOne")
                || columnName.equals("columnTwo")
                || columnName.equals("dateOne"));

            if (columnName.equals("columnOne")) {
                assertEquals("getColumnOne", column.getGetter().getName());
                assertEquals("setColumnOne", column.getSetter().getName());
            } else if (columnName.equals("columnTwo")) {
                assertEquals("getColumnTwo", column.getGetter().getName());
                assertEquals("setColumnTwo", column.getSetter().getName());
            } else if (columnName.equals("dateOne")) {
                assertEquals("getDateOne", column.getGetter().getName());
                assertEquals("setDateOne", column.getSetter().getName());
            }

        }

        assertEquals(new Long(0L), entityMetaCache.getEntityColumnMetaCacheHits());
        assertEquals(new Long(1L), entityMetaCache.getEntityColumnMetaCacheMisses());

        entityMetaCache.getColumnMetaSet(SimpleEntity.class);
        assertEquals(new Long(1L), entityMetaCache.getEntityColumnMetaCacheHits());
        assertEquals(new Long(1L), entityMetaCache.getEntityColumnMetaCacheMisses());
    }

    @Test
    public void testNonColumnReferenceMeta() {
        Set<ReferenceMeta> referenceMetaSet
            = entityMetaCache.getNonColumnReferenceMetaSet(RefEntity.class);

        assertEquals(2, referenceMetaSet.size());

        for (ReferenceMeta referenceMeta : referenceMetaSet) {
            String getterName = referenceMeta.getGetter().getName();
            String setterName = referenceMeta.getSetter().getName();
            Class<? extends IsEntity> entityReferenceClass
                = referenceMeta.getReferencedClass();
            assertTrue(getterName.equals("getNonColumnSimpleEntityList")
                || getterName.equals("getNonColumnSimple"));

            assertTrue(setterName.equals("setNonColumnSimpleEntityList")
                || setterName.equals("setNonColumnSimple"));

            assertEquals(SimpleEntity.class, entityReferenceClass);
        }

        assertEquals(new Long(1), entityMetaCache.getNonColumnReferenceMetaCacheMisses());
        assertEquals(new Long(0), entityMetaCache.getNonColumnReferenceMetaCacheHits());

        entityMetaCache.getNonColumnReferenceMetaSet(RefEntity.class);
        assertEquals(new Long(1), entityMetaCache.getNonColumnReferenceMetaCacheMisses());
        assertEquals(new Long(1), entityMetaCache.getNonColumnReferenceMetaCacheHits());
    }

    @Test
    public void testColumnReferenceMeta() {
        Set<ReferenceMeta> referenceMetaSet
            = entityMetaCache.getColumnReferenceMetaSet(RefEntity.class);

        assertEquals(3, referenceMetaSet.size());

        for (ReferenceMeta referenceMeta : referenceMetaSet) {
            String getterName = referenceMeta.getGetter().getName();
            String setterName = referenceMeta.getSetter().getName();
            Class<? extends IsEntity> entityReferenceClass
                = referenceMeta.getReferencedClass();
            assertTrue(getterName.equals("getSimpleEntitySet")
                || getterName.equals("getSimpleEntityList")
                || getterName.equals("getSimpleManyToOne"));

            assertTrue(setterName.equals("setSimpleEntitySet")
                || setterName.equals("setSimpleEntityList")
                || setterName.equals("setSimpleManyToOne"));

            assertEquals(SimpleEntity.class, entityReferenceClass);
        }

        assertEquals(new Long(0), entityMetaCache.getColumnReferenceMetaCacheHits());
        assertEquals(new Long(1), entityMetaCache.getColumnReferenceMetaCacheMisses());

        entityMetaCache.getColumnReferenceMetaSet(RefEntity.class);
        assertEquals(new Long(1), entityMetaCache.getColumnReferenceMetaCacheHits());
        assertEquals(new Long(1), entityMetaCache.getColumnReferenceMetaCacheMisses());
    }

    @Test
    public void testReferenceMeta() {
        Set<ReferenceMeta> referenceMetaSet 
            = entityMetaCache.getReferenceMetaSet(RefEntity.class);

        assertEquals(5, referenceMetaSet.size());

        for (ReferenceMeta referenceMeta : referenceMetaSet) {

            String getterName = referenceMeta.getGetter().getName();
            String setterName = referenceMeta.getSetter().getName();
            Class<? extends IsEntity> entityReferenceClass
                = referenceMeta.getReferencedClass();
            assertTrue(getterName.equals("getSimpleEntitySet")
                || getterName.equals("getSimpleEntityList")
                || getterName.equals("getSimpleManyToOne")
                || getterName.equals("getNonColumnSimpleEntityList")
                || getterName.equals("getNonColumnSimple"));

            assertTrue(setterName.equals("setSimpleEntitySet")
                || setterName.equals("setSimpleEntityList")
                || setterName.equals("setSimpleManyToOne")
                || setterName.equals("setNonColumnSimpleEntityList")
                || setterName.equals("setNonColumnSimple"));

            assertEquals(SimpleEntity.class, entityReferenceClass);

        }

        assertEquals(new Long(0), entityMetaCache.getColumnReferenceMetaCacheHits());
        assertEquals(new Long(1), entityMetaCache.getColumnReferenceMetaCacheMisses());

        assertEquals(new Long(1), entityMetaCache.getNonColumnReferenceMetaCacheMisses());
        assertEquals(new Long(0), entityMetaCache.getNonColumnReferenceMetaCacheHits());


        entityMetaCache.getReferenceMetaSet(RefEntity.class);

        assertEquals(new Long(1), entityMetaCache.getColumnReferenceMetaCacheHits());
        assertEquals(new Long(1), entityMetaCache.getColumnReferenceMetaCacheMisses());
        assertEquals(new Long(1), entityMetaCache.getNonColumnReferenceMetaCacheMisses());
        assertEquals(new Long(1), entityMetaCache.getNonColumnReferenceMetaCacheHits());
    }
}
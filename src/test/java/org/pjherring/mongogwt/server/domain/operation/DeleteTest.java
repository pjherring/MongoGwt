/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;

import com.mongodb.DB;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pjherring.mongogwt.server.domain.operation.DBObjectToPojoTest.EmbeddedEntity;
import org.pjherring.mongogwt.server.guice.MongoDatabaseModule;
import org.pjherring.mongogwt.shared.BaseDomainObject;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.annotations.Column;
import org.pjherring.mongogwt.shared.annotations.Embedded;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.annotations.Reference;
import org.pjherring.mongogwt.shared.annotations.enums.ReferenceType;
import org.pjherring.mongogwt.shared.domain.operation.Create;
import org.pjherring.mongogwt.shared.domain.operation.Delete;
import org.pjherring.mongogwt.shared.domain.operation.Read;
import org.pjherring.mongogwt.shared.exception.InvalidEntity;
import org.pjherring.mongogwt.shared.exception.NotFoundException;
import org.pjherring.mongogwt.shared.exception.NotPersistedException;
import org.pjherring.mongogwt.shared.query.Query;
import static org.junit.Assert.*;

/**
 *
 * @author pjherring
 */
public class DeleteTest {

    private final static Logger LOG = Logger.getLogger(DeleteTest.class.getName());
    private static final Injector injector
        = Guice.createInjector(
        new DatabaseTestModule()
    );

    private static DB mongoDb;
    private static Delete delete;
    private static Create create;
    private static Read read;

    public DeleteTest() {
    }

    public static class DatabaseTestModule extends MongoDatabaseModule {

        @Override
        protected String getHostName() {
            return "localhost";
        }

        @Override
        protected String getDatabaseName() {
            return DeleteTest.class.getSimpleName();
        }

        @Override
        protected List<Class<? extends IsEntity>> getEntityList() {
            List<Class<? extends IsEntity>> entityList
                = new ArrayList<Class<? extends IsEntity>>();

            entityList.add(SimpleEntity.class);
            entityList.add(WithSimpleRef.class);
            entityList.add(WithSimpleRefCascade.class);

            return entityList;
        }

    }

    @Entity(name="simple")
    public static class SimpleEntity extends BaseDomainObject {

        private String data;
        private int intData;
        private Set<WithSimpleRef> refSet;
        private EmbeddedEntity embeddedEntity;

        @Column(name="data")
        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        @Column(name="intData")
        public int getIntData() {
            return intData;
        }

        public void setIntData(int intData) {
            this.intData = intData;
        }

        @Reference(type=ReferenceType.ONE_TO_MANY, managedBy="simple")
        public Set<WithSimpleRef> getRefSet() {
            return refSet;
        }

        public void setRefSet(Set<WithSimpleRef> refSet) {
            this.refSet = refSet;
        }

        @Embedded
        @Column(name="embed")
        public EmbeddedEntity getEmbeddedEntity() {
            return embeddedEntity;
        }

        public void setEmbeddedEntity(EmbeddedEntity embeddedEntity) {
            this.embeddedEntity = embeddedEntity;
        }
    }

    @Entity(name="withSimleReference")
    public static class WithSimpleRef extends BaseDomainObject {
        private SimpleEntity simple;

        @Column(name="simple")
        @Reference(type=ReferenceType.MANY_TO_ONE, doCascadeDelete=false)
        public SimpleEntity getSimple() {
            return simple;
        }

        public void setSimple(SimpleEntity simple) {
            this.simple = simple;
        }
    }

    @Entity(name="withSimpleRefCascade")
    public static class WithSimpleRefCascade extends BaseDomainObject {
        private SimpleEntity simple;
        private Set<SimpleEntity> simpleSet;
        private SimpleEntity simpleOneToOne;

        @Column(name="simple")
        @Reference(type=ReferenceType.MANY_TO_ONE, doCascadeDelete=true)
        public SimpleEntity getSimple() {
            return simple;
        }

        public void setSimple(SimpleEntity simple) {
            this.simple = simple;
        }

        @Column(name="simpleSet")
        @Reference(type=ReferenceType.ONE_TO_MANY, doCascadeDelete=true)
        public Set<SimpleEntity> getSimpleSet() {
            return simpleSet;
        }

        public void setSimpleSet(Set<SimpleEntity> simpleSet) {
            this.simpleSet = simpleSet;
        }

        @Column(name="simpleOneToOne")
        @Reference(type=ReferenceType.ONE_TO_ONE, doCascadeDelete=true)
        public SimpleEntity getSimpleOneToOne() {
            return simpleOneToOne;
        }

        public void setSimpleOneToOne(SimpleEntity simpleOneToOne) {
            this.simpleOneToOne = simpleOneToOne;
        }
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        mongoDb = injector.getInstance(DB.class);
        delete = injector.getInstance(Delete.class);
        read = injector.getInstance(Read.class);
        create = injector.getInstance(Create.class);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {

        mongoDb
            .getCollection(SimpleEntity.class.getAnnotation(Entity.class).name())
            .drop();
        mongoDb
            .getCollection(WithSimpleRef.class.getAnnotation(Entity.class).name())
            .drop();
        mongoDb
            .getCollection(WithSimpleRefCascade.class.getAnnotation(Entity.class).name())
            .drop();
    }

    @After
    public void tearDown() {
    }

    @Test(expected=NotFoundException.class)
    public void testDeleteEntity_NoReferences() {
        SimpleEntity simpleEntity = new SimpleEntity();
        simpleEntity.setData("data");

        create.doCreate(simpleEntity);

        String id = simpleEntity.getId();

        assertNotNull(simpleEntity.getId());

        delete.delete(simpleEntity);
        assertNull(simpleEntity.getId());

        read.findById(id, SimpleEntity.class, true);
    }

    @Test(expected=NotPersistedException.class)
    public void test_invalidDeleteNotPersisted() {
        SimpleEntity entity = new SimpleEntity();
        delete.delete(entity);
    }

    @Test(expected=InvalidEntity.class)
    public void test_invalidDeleteNotAnnotated() {
        IsEntity entity = new BaseDomainObject(){};
        entity.setId("someId");
        delete.delete(entity);
    }

    @Test
    public void test_deleteWithReference_NotManaged_noCascade() {
        SimpleEntity entity = new SimpleEntity();
        create.doCreate(entity);
        WithSimpleRef withRef = new WithSimpleRef();
        withRef.setSimple(entity);
        create.doCreate(withRef);
        String id = withRef.getId();

        delete.delete(withRef);
        SimpleEntity found
            = read.findById(entity.getId(), SimpleEntity.class, true);
        assertEquals(found.getId(), entity.getId());

        Exception exception = null;
        try {
            read.findById(id, WithSimpleRef.class, true);
        } catch (Exception e) {
            exception = e;
        }

        assertNotNull(exception);
        assertTrue(exception instanceof NotFoundException);
    }

    @Test
    public void test_deleteWitHReference_Managed_Cascade() {
        SimpleEntity manyToOneEntity = new SimpleEntity();
        create.doCreate(manyToOneEntity);

        SimpleEntity inSetEntity = new SimpleEntity();
        SimpleEntity inSetEntityTwo = new SimpleEntity();
        create.doCreate(inSetEntity);
        create.doCreate(inSetEntityTwo);

        SimpleEntity oneToOneEntity = new SimpleEntity();
        create.doCreate(oneToOneEntity);

        WithSimpleRefCascade withRef = new WithSimpleRefCascade();

        withRef.setSimple(manyToOneEntity);
        Set<SimpleEntity> simpleSet = new HashSet<SimpleEntity>();
        simpleSet.add(inSetEntity);
        simpleSet.add(inSetEntityTwo);
        withRef.setSimpleSet(simpleSet);

        withRef.setSimpleOneToOne(oneToOneEntity);

        create.doCreate(withRef);
        String id = withRef.getId();

        delete.delete(withRef);

        Exception exception = null;

        try {
            read.findById(manyToOneEntity.getId(), SimpleEntity.class, true);
        } catch (Exception e) {
            exception = e;
        }
        assertNotNull(exception);
        assertTrue(exception instanceof NotFoundException);
        exception = null;

        try {
            read.findById(inSetEntity.getId(), SimpleEntity.class, true);
        } catch (Exception e) {
            exception = e;
        }
        assertNotNull(exception);
        assertTrue(exception instanceof NotFoundException);
        exception = null;

        try {
            read.findById(inSetEntityTwo.getId(), SimpleEntity.class, true);
        } catch (Exception e) {
            exception = e;
        }
        assertNotNull(exception);
        assertTrue(exception instanceof NotFoundException);
        exception = null;

        try {
            read.findById(oneToOneEntity.getId(), SimpleEntity.class, true);
        } catch (Exception e) {
            exception = e;
        }

        assertNotNull(exception);
        assertTrue(exception instanceof NotFoundException);
        exception = null;

        try {
            read.findById(id, WithSimpleRefCascade.class, true);
        } catch (Exception e) {
            exception = e;
        }

        assertNotNull(exception);
        assertTrue(exception instanceof NotFoundException);
        exception = null;
    }

    @Test(expected=NotFoundException.class)
    public void test_delete_with_query() {
        String data = "Data";
        SimpleEntity simple = new SimpleEntity();
        simple.setData(data);

        create.doCreate(simple);

        SimpleEntity simpleTwo = new SimpleEntity();
        simpleTwo.setData(data);
        create.doCreate(simpleTwo);

        Query query = new Query()
            .start("data").is(data);

        delete.delete(query, SimpleEntity.class);

        read.find(query, SimpleEntity.class, true);

    }

}
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.domain.operation;

import com.mongodb.DB;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pjherring.mongogwt.server.domain.operation.DBObjectToPojoTest.EmbeddedEntity;
import org.pjherring.mongogwt.server.guice.DataAccessTestModule;
import org.pjherring.mongogwt.server.guice.DatabaseModule;
import org.pjherring.mongogwt.shared.BaseDomainObject;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.annotations.Column;
import org.pjherring.mongogwt.shared.annotations.Embedded;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.annotations.Reference;
import org.pjherring.mongogwt.shared.annotations.enums.ReferenceType;
import org.pjherring.mongogwt.shared.exception.InvalidEntity;
import org.pjherring.mongogwt.shared.exception.NotFoundException;
import org.pjherring.mongogwt.shared.exception.NotPersistedException;
import static org.junit.Assert.*;

/**
 *
 * @author pjherring
 */
public class DeleteTest {

    private final static Logger LOG = Logger.getLogger(DeleteTest.class.getName());
    private static final Injector injector
        = Guice.createInjector(
        new DataAccessTestModule(),
        new DatabaseTestModule()
    );

    private DB mongoDb;
    private Delete delete;
    private Create create;
    private Read read;

    public DeleteTest() {
    }

    public static class DatabaseTestModule extends DatabaseModule {

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

        @Column(name="simple")
        @Reference(type=ReferenceType.MANY_TO_ONE, doCascadeDelete=true)
        public SimpleEntity getSimple() {
            return simple;
        }

        public void setSimple(SimpleEntity simple) {
            this.simple = simple;
        }
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        delete = injector.getInstance(Delete.class);
        read = injector.getInstance(Read.class);
        create = injector.getInstance(Create.class);

        mongoDb = injector.getInstance(DB.class);
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
        LOG.info("test_deleteWitHReference_Managed_Cascade");
        SimpleEntity entity = new SimpleEntity();
        create.doCreate(entity);
        WithSimpleRefCascade withRef = new WithSimpleRefCascade();
        withRef.setSimple(entity);
        create.doCreate(withRef);
        String id = withRef.getId();

        delete.delete(withRef);

        Exception exception = null;

        try {
            read.findById(entity.getId(), SimpleEntity.class, true);
        } catch (Exception e) {
            exception = e;
        }

        assertNotNull(exception);
        assertTrue(exception instanceof NotFoundException);

        try {
            read.findById(id, WithSimpleRefCascade.class, true);
        } catch (Exception e) {
            exception = e;
        }

        assertNotNull(exception);
        assertTrue(exception instanceof NotFoundException);
    }

}
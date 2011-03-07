/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;

import org.pjherring.mongogwt.shared.domain.operation.Read;
import java.util.Set;
import org.pjherring.mongogwt.shared.domain.operation.Create;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mongodb.DB;
import java.util.ArrayList;
import java.util.List;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pjherring.mongogwt.server.guice.MongoDatabaseModule;
import org.pjherring.mongogwt.shared.BaseDomainObject;
import org.pjherring.mongogwt.shared.IsEmbeddable;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.annotations.Column;
import org.pjherring.mongogwt.shared.annotations.Embedded;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.annotations.Reference;
import org.pjherring.mongogwt.shared.annotations.enums.ReferenceType;
import org.pjherring.mongogwt.shared.exception.NotFoundException;
import org.pjherring.mongogwt.shared.query.Query;
import static org.junit.Assert.*;

/**
 *
 * @author pjherring
 */
public class ReadTest extends EasyMockSupport {

    private static final Injector injector
        = Guice.createInjector(
        new DatabaseTestModule()
    );

    private static DB mongoDb;
    private static Read read;
    private static Create create;

    public ReadTest() {
    }

    public static class DatabaseTestModule extends MongoDatabaseModule {

        @Override
        protected String getHostName() {
            return "localhost";
        }

        @Override
        protected String getDatabaseName() {
            return ReadTest.class.getSimpleName();
        }

        @Override
        protected List<Class<? extends IsEntity>> getEntityList() {
            List<Class<? extends IsEntity>> entityList
                = new ArrayList<Class<? extends IsEntity>>();

            entityList.add(SimpleEntity.class);
            entityList.add(WithSimpleRef.class);

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
        @Reference(type=ReferenceType.MANY_TO_ONE)
        public SimpleEntity getSimple() {
            return simple;
        }

        public void setSimple(SimpleEntity simple) {
            this.simple = simple;
        }
    }

    public static class EmbeddedEntity implements IsEmbeddable {
        private String data;

        @Column(name="data")
        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        mongoDb = injector.getInstance(DB.class);
        read = injector.getInstance(Read.class);
        create = injector.getInstance(Create.class);

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        mongoDb.getCollection(
            SimpleEntity.class.getAnnotation(Entity.class).name()
        ).drop();
        mongoDb.getCollection(
            WithSimpleRef.class.getAnnotation(Entity.class).name()
        ).drop();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testFind() {
        SimpleEntity entity = new SimpleEntity();
        entity.setData("data");

        create.doCreate(entity);

        Query query = new Query()
            .start("data").is("data");

        List<SimpleEntity> results = read.find(query, SimpleEntity.class, true);
        assertEquals(1, results.size());
        assertEquals("data", results.get(0).getData());
    }

    @Test(expected=NotFoundException.class)
    public void testNotFoundForFind() {
        Query query = new Query()
            .start("data").is("data");

        read.find(query, SimpleEntity.class, true);
    }

    @Test
    public void testLimit() {
        SimpleEntity entity = new SimpleEntity();
        entity.setData("data");

        create.doCreate(entity);

        SimpleEntity entityTwo = new SimpleEntity();
        entityTwo.setData("data");
        create.doCreate(entityTwo);

        Query query = new Query()
            .start("data").is("data")
            .setLimit(1);

        List<SimpleEntity> results = read.find(query, SimpleEntity.class, true);
        assertEquals(1, results.size());
    }

    @Test
    public void testSort() {
        SimpleEntity entity = new SimpleEntity();
        entity.setData("data");
        entity.setIntData(5);

        create.doCreate(entity);

        SimpleEntity entityTwo = new SimpleEntity();
        entityTwo.setData("data");
        entityTwo.setIntData(10);
        create.doCreate(entityTwo);

        Query query = new Query()
            .start("data").is("data")
            .addSort("intData", Query.Sort.DESC);

        List<SimpleEntity> results = read.find(query, SimpleEntity.class, true);
        assertEquals(2, results.size());
        assertEquals(10, results.get(0).getIntData());
        assertEquals(5, results.get(1).getIntData());

        Query queryTwo = new Query()
            .start("data").is("data")
            .addSort("intData", Query.Sort.ASC);

        results = read.find(queryTwo, SimpleEntity.class, true);
        assertEquals(2, results.size());
        assertEquals(5, results.get(0).getIntData());
        assertEquals(10, results.get(1).getIntData());
    }

    @Test
    public void testFindOne() {
        SimpleEntity entity = new SimpleEntity();
        entity.setData("data");
        entity.setIntData(5);
        create.doCreate(entity);

        SimpleEntity entityTwo = new SimpleEntity();
        entityTwo.setData("other");
        entityTwo.setIntData(10);
        create.doCreate(entityTwo);

        Query query = new Query()
            .start("data").is("data");

        SimpleEntity found = read.findOne(query, SimpleEntity.class, true);
        assertEquals(5, found.getIntData());
    }

    @Test(expected=NotFoundException.class)
    public void testFindOne_NotFound() {
        Query query = new Query()
            .start("data").is("data");

        read.findOne(query, SimpleEntity.class, true);
    }

    @Test
    public void testFindById() {
        SimpleEntity entity = new SimpleEntity();
        entity.setData("data");
        entity.setIntData(5);
        create.doCreate(entity);

        SimpleEntity found = read.findById(entity.getId(), SimpleEntity.class, true);
        assertEquals(entity.getId(), entity.getId());
    }

    @Test
    public void testFindReferences_doFanOut() {
        SimpleEntity simple = new SimpleEntity();
        simple.setData("data");

        create.doCreate(simple);

        WithSimpleRef withRef = new WithSimpleRef();
        withRef.setSimple(simple);

        create.doCreate(withRef);

        Query query = new Query()
            .start("data").is("data");

        SimpleEntity found = read.findOne(query, SimpleEntity.class, true);
        assertEquals(found.getId(), simple.getId());
        assertNotNull(found.getRefSet());

        Set<WithSimpleRef> set = found.getRefSet();
        assertEquals(1, set.size());
        assertEquals(withRef.getId(), set.iterator().next().getId());

        Query queryForWithRef = new Query()
            .start("simple").is(new Query.Reference(simple.getId(), SimpleEntity.class));

        WithSimpleRef foundWithRef = read.findOne(queryForWithRef, WithSimpleRef.class, true);
        assertNotNull(foundWithRef.getSimple());
        assertEquals(simple.getId(), foundWithRef.getSimple().getId());
    }

    @Test
    public void testFindReferences_doNoFanOut() {
        SimpleEntity simple = new SimpleEntity();
        simple.setData("data");

        create.doCreate(simple);

        WithSimpleRef withRef = new WithSimpleRef();
        withRef.setSimple(simple);

        create.doCreate(withRef);

        Query query = new Query()
            .start("data").is("data");

        SimpleEntity found = read.findOne(query, SimpleEntity.class, false);
        assertEquals(found.getId(), simple.getId());
        assertNotNull(found.getRefSet());
        assertNotNull(found.getData());

        Set<WithSimpleRef> set = found.getRefSet();
        assertEquals(1, set.size());
        WithSimpleRef withRefFromSet = set.iterator().next();
        assertEquals(withRef.getId(), withRefFromSet.getId());
        assertNull(withRefFromSet.getSimple());

        Query queryForWithRef = new Query()
            .start("simple").is(new Query.Reference(simple.getId(), SimpleEntity.class));

        WithSimpleRef foundWithRef = read.findOne(queryForWithRef, WithSimpleRef.class, false);
        assertNotNull(foundWithRef.getSimple());
        assertEquals(simple.getId(), foundWithRef.getSimple().getId());
        assertNull(foundWithRef.getSimple().getData());
    }

    @Test
    public void testRead_WithEmbed() {
        SimpleEntity simple = new SimpleEntity();
        simple.setData("new Data");
        simple.setEmbeddedEntity(new EmbeddedEntity());
        simple.getEmbeddedEntity().setData("embedData");

        create.doCreate(simple);

        SimpleEntity found = read.findById(simple.getId(), SimpleEntity.class, true);
        assertEquals(simple.getId(), found.getId());
        assertNotNull(found.getEmbeddedEntity());
        assertEquals("embedData", found.getEmbeddedEntity().getData());
    }


}
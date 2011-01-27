/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;

import org.pjherring.mongogwt.shared.IsEntity;
import java.util.Date;
import java.util.Set;
import java.lang.reflect.Method;
import org.easymock.Capture;
import java.util.Map;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.bson.types.ObjectId;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pjherring.mongogwt.server.guice.DataAccessTestModule;
import org.pjherring.mongogwt.server.guice.DatabaseModule;
import org.pjherring.mongogwt.shared.BaseDomainObject;
import org.pjherring.mongogwt.shared.IsEmbeddable;
import org.pjherring.mongogwt.shared.annotations.Column;
import org.pjherring.mongogwt.shared.annotations.Embedded;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.annotations.Reference;
import org.pjherring.mongogwt.shared.annotations.enums.ReferenceType;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

/**
 *
 * @author pjherring
 */
public class DBObjectToPojoTest extends EasyMockSupport {

    private final static Injector injector
        = Guice.createInjector(new DatabaseTestModule(), new DataAccessTestModule());
    private DBObjectToPojo dbObjectToPojo;
    private PojoToDBObject pojoToDBObject;
    private DB mongoDb;


    public static class DatabaseTestModule extends DatabaseModule {

        @Override
        protected String getHostName() {
            return "localhost";
        }

        @Override
        protected String getDatabaseName() {
            return DBObjectToPojoTest.class.getSimpleName();
        }

        @Override
        protected List<Class<? extends IsEntity>> getEntityList() {
            List<Class<? extends IsEntity>> list = new ArrayList<Class<? extends IsEntity>>();
            list.add(SimpleEntity.class);
            list.add(EntityOneToMany.class);
            list.add(EntityManyToOne.class);
            list.add(WithEmbedded.class);

            return list;
        }

    }

    public DBObjectToPojoTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        pojoToDBObject = injector.getInstance(PojoToDBObject.class);
        dbObjectToPojo = injector.getInstance(DBObjectToPojo.class);
        dbObjectToPojo.resetCache();
        mongoDb = injector.getInstance(DB.class);
    }

    @After
    public void tearDown() {
    }

    @Entity(name="simple")
    public static class SimpleEntity extends BaseDomainObject {
        private String data;
        private Set<WithSimpleReference> referenceSet;

        @Column(name="data")
        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        @Reference(managedBy="simple", type=ReferenceType.ONE_TO_MANY)
        public Set<WithSimpleReference> getReferenceSet() {
            return referenceSet;
        }

        public void setReferenceSet(Set<WithSimpleReference> refereceSet) {
            this.referenceSet = refereceSet;
        }
    }

    @Entity(name="withReference")
    public static class WithSimpleReference extends BaseDomainObject {
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

    @Entity(name="oneToMany")
    public static class EntityOneToMany extends BaseDomainObject {

        private Set<SimpleEntity> simpleEntitySet;
        private List<SimpleEntity> simpleEntityList;

        @Reference(type=ReferenceType.ONE_TO_MANY)
        @Column(name="set")
        public Set<SimpleEntity> getSimpleEntitySet() {
            return simpleEntitySet;
        }

        public void setSimpleEntitySet(Set<SimpleEntity> simpleEntitySet) {
            this.simpleEntitySet = simpleEntitySet;
        }

        @Reference(type=ReferenceType.ONE_TO_MANY)
        @Column(name="list")
        public List<SimpleEntity> getSimpleEntityList() {
            return simpleEntityList;
        }

        public void setSimpleEntityList(List<SimpleEntity> simpleEntityList) {
            this.simpleEntityList = simpleEntityList;
        }
    }

    @Entity(name="manyToOne")
    public static class EntityManyToOne extends BaseDomainObject {
        private SimpleEntity manyToOne;

        @Reference(type=ReferenceType.MANY_TO_ONE)
        @Column(name="manyToOne")
        public SimpleEntity getManyToOne() {
            return manyToOne;
        }

        public void setManyToOne(SimpleEntity manyToOne) {
            this.manyToOne = manyToOne;
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

    public static class EntityWithCollections extends BaseDomainObject {

        private Set<String> stringSet;
        private List<Integer> integerList;
        private Date[] dateArray;

        @Column(name="stringSet")
        public Set<String> getStringSet() {
            return stringSet;
        }

        public void setStringSet(Set<String> stringSet) {
            this.stringSet = stringSet;
        }

        @Column(name="integerList")
        public List<Integer> getIntegerList() {
            return integerList;
        }

        public void setIntegerList(List<Integer> integerList) {
            this.integerList = integerList;
        }

        @Column(name="dateArray")
        public Date[] getDateArray() {
            return dateArray;
        }

        public void setDateArray(Date[] dateArray) {
            this.dateArray = dateArray;
        }
    }

    @Entity(name="withEmbed")
    public static class WithEmbedded extends BaseDomainObject {

        private EmbeddedEntity embed;

        @Column(name="embed")
        @Embedded
        public EmbeddedEntity getEmbed() {
            return embed;
        }

        public void setEmbed(EmbeddedEntity embed) {
            this.embed = embed;
        }

    }

    public static class DBObjectToPojoWithMock extends DBObjectToPojoImpl {

        @Inject
        public DBObjectToPojoWithMock(@DatabaseModule.EntityList List<Class<? extends IsEntity>> enttiyList) {
            super(enttiyList);
        }

        public void setMap(Map map) {
            cacheMap = map;
        }
    }

    @Test
    public void test_simpleEntityTranslationCreation() {
        //we set the cache map to be our mock map
        Map mockMap = createMock(Map.class);
        dbObjectToPojo = injector.getInstance(DBObjectToPojoWithMock.class);
        ((DBObjectToPojoWithMock) dbObjectToPojo).setMap(mockMap);


        expect(mockMap.containsKey(SimpleEntity.class)).andReturn(false);

        Capture<Map> mapCapture = new Capture<Map>();
        expect(mockMap.put(eq(SimpleEntity.class), capture(mapCapture))).andReturn(null);

        expect(mockMap.containsKey(SimpleEntity.class)).andReturn(true);


        /*
         * This will be our entityTranslationMap, we do this to avoid testing
         * the actual building of the pojo.
         */
        Map entityTranslationMapMock = createMock(Map.class);
        expect(mockMap.get(SimpleEntity.class)).andReturn(entityTranslationMapMock);

        //return an empty Set so we don't build anything
        expect(entityTranslationMapMock.keySet()).andReturn(new HashSet());
        
        replayAll();

        DBObject dbObject = new BasicDBObject();
        dbObject.put("data", "somedata");
        dbObjectToPojo.translate(dbObject, SimpleEntity.class);

        verifyAll();

        Map<String, Method> entityTranslationMap = mapCapture.getValue();
        assertEquals(1, entityTranslationMap.keySet().size());
        assertEquals("setData", entityTranslationMap.get("data").getName());
    }

    @Test
    public void testSimpleTranslation_noId() {
        String dataStr = "dataStr";
        DBObject dbObject = new BasicDBObject();
        dbObject.put("data", dataStr);

        SimpleEntity translated = dbObjectToPojo.translate(dbObject, SimpleEntity.class);
        assertEquals(dataStr, translated.getData());
        assertNull(translated.getId());
        assertNull(translated.getCreatedDatetime());
    }

    @Test
    public void test_simpleEntity_withId() {
        SimpleEntity simple = new SimpleEntity();
        simple.setData("data");

        DBObject simpleDb = pojoToDBObject.translate(simple);
        mongoDb
            .getCollection(SimpleEntity.class.getAnnotation(Entity.class).name())
            .insert(simpleDb);

        ObjectId objectId = (ObjectId) simpleDb.get("_id");

        SimpleEntity constructed = dbObjectToPojo.translate(simpleDb, SimpleEntity.class);

        assertEquals(simple.getData(), constructed.getData());
        assertEquals(objectId.toString(), constructed.getId());
        assertEquals(objectId.getTime(), constructed.getCreatedDatetime().getTime());
    }

    @Test
    public void test_EntityWithOneToManyReference() {
        SimpleEntity simpleTwo = new SimpleEntity();
        simpleTwo.setData("data");

        DBObject simpleTranslateTwo = pojoToDBObject.translate(simpleTwo);
        mongoDb
            .getCollection("simple")
            .insert(simpleTranslateTwo);
        ObjectId simpleIdTwo = (ObjectId) simpleTranslateTwo.get("_id");
        simpleTwo.setId(simpleIdTwo.toString());
        simpleTwo.setCreatedDatetime(new Date(simpleIdTwo.getTime()));

        SimpleEntity simpleOne = new SimpleEntity();
        simpleOne.setData("data");
        DBObject simpleTranslate = pojoToDBObject.translate(simpleOne);
        mongoDb
            .getCollection("simple")
            .insert(simpleTranslate);
        ObjectId simpleId = (ObjectId) simpleTranslate.get("_id");
        simpleOne.setId(simpleId.toString());
        simpleOne.setCreatedDatetime(new Date(simpleId.getTime()));

        EntityOneToMany entity = new EntityOneToMany();
        entity.setSimpleEntityList(Arrays.asList(new SimpleEntity[]{simpleOne, simpleTwo}));
        entity.setSimpleEntitySet(new HashSet<SimpleEntity>(entity.getSimpleEntityList()));

        DBObject entityTranslated = pojoToDBObject.translate(entity);

        EntityOneToMany constructed =
            dbObjectToPojo.translate(entityTranslated, EntityOneToMany.class);

        assertEquals(simpleOne.getId(), constructed.getSimpleEntityList().get(0).getId());
        assertEquals(simpleTwo.getId(), constructed.getSimpleEntityList().get(1).getId());
        assertEquals(2, constructed.getSimpleEntitySet().size());

        for (SimpleEntity simpleEntityFromSet : constructed.getSimpleEntitySet()) {
            if (!simpleEntityFromSet.getId().equals(simpleOne.getId())
                && !simpleEntityFromSet.getId().equals(simpleTwo.getId())) {
                throw new AssertionError("The set does not have either of the entities");
            }
        }
    }

    @Test
    public void test_EntityWithManyToOneReference() {
        String data = "someData";
        SimpleEntity simple = new SimpleEntity();
        simple.setData(data);
        DBObject simpleDB = pojoToDBObject.translate(simple);
        mongoDb.getCollection("simple").insert(simpleDB);
        ObjectId simpleId = (ObjectId) simpleDB.get("_id");
        simple.setId(simpleId.toString());
        simple.setCreatedDatetime(new Date(simpleId.getTime()));

        EntityManyToOne entity = new EntityManyToOne();
        entity.setManyToOne(simple);
        DBObject entityDb = pojoToDBObject.translate(entity);

        EntityManyToOne constructed = dbObjectToPojo.translate(entityDb, EntityManyToOne.class);
        assertEquals(simple.getId(), constructed.getManyToOne().getId());
    }

    @Test
    public void test_EntityWithEmbedded() {
        EmbeddedEntity embedded = new EmbeddedEntity();
        embedded.setData("data");
        WithEmbedded entity = new WithEmbedded();
        entity.setEmbed(embedded);
        DBObject dbObject = pojoToDBObject.translate(entity);
        WithEmbedded constructed = dbObjectToPojo.translate(dbObject, WithEmbedded.class);
        assertEquals("data", constructed.getEmbed().getData());
    }

    @Test
    public void test_EntityWithCollectionsArrays() {
        EntityWithCollections entity = new EntityWithCollections();
        entity.setStringSet(new HashSet(Arrays.asList(new String[]{"one", "two", "three"})));
        entity.setIntegerList(Arrays.asList(new Integer[]{1, 2, 3}));
        entity.setDateArray(new Date[]{new Date(23532532), new Date(232432423), new Date(91293123)});

        DBObject entityAsDB = pojoToDBObject.translate(entity);
        EntityWithCollections constructed
            = dbObjectToPojo.translate(entityAsDB, EntityWithCollections.class);
        assertEquals(entity.getStringSet(), constructed.getStringSet());
        assertArrayEquals(entity.getDateArray(), constructed.getDateArray());
        assertEquals(entity.getIntegerList(), constructed.getIntegerList());
    }

    @Test
    public void test_cacheStats() {
        DBObject simpleDB = new BasicDBObject();
        simpleDB.put("data", "simple");

        dbObjectToPojo.translate(simpleDB, SimpleEntity.class);
        assertEquals(1, dbObjectToPojo.getCacheMisses());
        assertEquals(0, dbObjectToPojo.getCacheHits());
        dbObjectToPojo.translate(simpleDB, SimpleEntity.class);
        assertEquals(1, dbObjectToPojo.getCacheMisses());
        assertEquals(1, dbObjectToPojo.getCacheHits());
        
    }
}
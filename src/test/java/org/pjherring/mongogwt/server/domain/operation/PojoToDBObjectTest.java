/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;

import java.util.logging.Logger;
import org.bson.types.ObjectId;
import java.util.Set;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.annotations.Column;
import org.pjherring.mongogwt.shared.annotations.Embedded;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.annotations.Enumerated;
import org.pjherring.mongogwt.shared.annotations.Reference;
import org.pjherring.mongogwt.shared.annotations.enums.ReferenceType;
import org.pjherring.mongogwt.shared.exception.InvalidReference;
import static org.junit.Assert.*;

/**
 *
 * @author pjherring
 */
public class PojoToDBObjectTest extends EasyMockSupport {

    private static final Injector injector =
        Guice.createInjector(new DatabaseTestModule(), new DataAccessTestModule());
    private final static Logger LOG = Logger.getLogger(PojoToDBObjectTest.class.getSimpleName());
    private PojoToDBObject pojoToDBObject;
    private DB mongoDb;

    public static class DatabaseTestModule extends DatabaseModule {

        @Override
        protected String getHostName() {
            return "localhost";
        }

        @Override
        protected String getDatabaseName() {
            return PojoToDBObjectTest.class.getSimpleName();
        }

        @Override
        protected List<Class<? extends IsEntity>> getEntityList() {
            List<Class<? extends IsEntity>> entities = new ArrayList<Class<? extends IsEntity>>();
            entities.add(BasicEntity.class);
            return entities;
        }

    }

    @Entity(name="basic")
    public static class BasicEntity extends BaseDomainObject {
        private String data;

        @Column(name="data")
        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    @Entity(name="withReference")
    public static class ManyReference extends BaseDomainObject {
        private Set<BasicEntity> basicEntitySet;

        @Reference(type=ReferenceType.ONE_TO_MANY)
        @Column(name="set")
        public Set<BasicEntity> getBasicEntitySet() {
            return basicEntitySet;
        }

        public void setBasicEntitySet(Set<BasicEntity> basicEntitySet) {
            this.basicEntitySet = basicEntitySet;
        }

    }

    @Entity(name="manyToOne")
    public static class OneToOneReference extends BaseDomainObject {
        private BasicEntity basic;

        @Reference(type=ReferenceType.MANY_TO_ONE)
        @Column(name="ref")
        public BasicEntity getBasic() {
            return basic;
        }

        public void setBasic(BasicEntity basic) {
            this.basic = basic;
        }
    }

    public enum TestEnum {
        ONE, TWO, THREE;
    }

    @Entity(name="enum")
    public static class EntityEnum extends BaseDomainObject {
        private TestEnum testEnum;
        private TestEnum otherEnum;

        @Column(name="testEnum")
        @Enumerated
        public TestEnum getTestEnum() {
            return testEnum;
        }

        public void setTestEnum(TestEnum testEnum) {
            this.testEnum = testEnum;
        }

        @Column(name="otherEnum")
        @Enumerated
        public TestEnum getOtherEnum() {
            return otherEnum;
        }

        public void setOtherEnum(TestEnum otherEnum) {
            this.otherEnum = otherEnum;
        }
    }

    @Entity(name="embedded")
    public static class EntityWithEmbedded extends BaseDomainObject {
        private EmbeddedObject embedOne;

        @Column(name="embed")
        @Embedded
        public EmbeddedObject getEmbedOne() {
            return embedOne;
        }

        public void setEmbedOne(EmbeddedObject embedOne) {
            this.embedOne = embedOne;
        }
    }

    public static class EmbeddedObject implements IsEmbeddable {
        private String someData;

        @Column(name="data")
        public String getSomeData() {
            return someData;
        }

        public void setSomeData(String someData) {
            this.someData = someData;
        }
    }

    public static class EntityWithCollections extends BaseDomainObject {
        private Set<String> stringSet;
        private List<Integer> integerList;
        private Integer[] intArray;

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

        @Column(name="intArray")
        public Integer[] getIntArray() {
            return intArray;
        }

        public void setIntArray(Integer[] intArray) {
            this.intArray = intArray;
        }
    }

    public PojoToDBObjectTest() {
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
        mongoDb = injector.getInstance(DB.class);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void BasicEntityToDBObject() {
        String data = "someData";
        BasicEntity basicEntity = new BasicEntity();
        basicEntity.setData(data);

        DBObject dbObject = pojoToDBObject.translate(basicEntity);

        assertEquals(data, dbObject.get("data"));
    }

    @Test
    public void testManReferenceTranslation() {
        BasicEntity basicEntity = new BasicEntity();
        basicEntity.setData("someData");
        DBObject basicEntityDBObject = pojoToDBObject.translate(basicEntity);
        /*
         * We need to persist this before referencing it. We also know that
         * translation of this Entity is correct because of previous tests.
         */
        mongoDb.getCollection(BasicEntity.class.getAnnotation(Entity.class).name())
            .insert(basicEntityDBObject);
        basicEntity.setId(((ObjectId)basicEntityDBObject.get("_id")).toString());
        basicEntity.setCreatedDatetime(
            new Date(
                ((ObjectId) basicEntityDBObject.get("_id")).getTime()
            )
        );

        ManyReference withReference = new ManyReference();
        withReference.setBasicEntitySet(
            new HashSet<BasicEntity>(
                Arrays.asList(new BasicEntity[]{basicEntity})
            )
        );

        DBObject dBObject = pojoToDBObject.translate(withReference);

        assertTrue(dBObject.containsField("set"));
        assertEquals(BasicDBList.class, dBObject.get("set").getClass());
        BasicDBList refList = (BasicDBList) dBObject.get("set");
        assertEquals(1, refList.size());
        DBRef ref = (DBRef) refList.get(0);
        DBObject objFromRef = ref.fetch();
        assertEquals(basicEntity.getId(), ((ObjectId) ref.getId()).toString());
    }

    @Test(expected=InvalidReference.class)
    public void test_invalidReference_noIdSet() {
        BasicEntity basicEntity = new BasicEntity();
        basicEntity.setData("someData");

        ManyReference withReference = new ManyReference();
        withReference.setBasicEntitySet(
            new HashSet<BasicEntity>(
                Arrays.asList(new BasicEntity[]{basicEntity})
            )
        );

        DBObject dBObject = pojoToDBObject.translate(withReference);
    }

    @Test
    public void test_manyToOneReference() {
        OneToOneReference domain = new OneToOneReference();
        BasicEntity basic = new BasicEntity();
        basic.setData("data");
        DBObject basicDbObject = pojoToDBObject.translate(basic);
        mongoDb
            .getCollection(BasicEntity.class.getAnnotation(Entity.class).name())
            .insert(basicDbObject);
        ObjectId basicId = (ObjectId) basicDbObject.get("_id");
        basic.setId(basicId.toString());
        basic.setCreatedDatetime(new Date(basicId.getTime()));
        domain.setBasic(basic);

        DBObject constructed = pojoToDBObject.translate(domain);

        assertEquals(DBRef.class, constructed.get("ref").getClass());
        DBRef basicAsRef = (DBRef) constructed.get("ref");
        DBObject constructedBasic = basicAsRef.fetch();
        assertEquals(basicId, basicAsRef.getId());
        assertEquals("data", constructedBasic.get("data"));
    }

    @Test(expected=InvalidReference.class)
    public void test_manyToOne_noId() {
        OneToOneReference domain = new OneToOneReference();
        BasicEntity basic = new BasicEntity();
        basic.setData("data");
        domain.setBasic(basic);

        pojoToDBObject.translate(domain);
    }

    @Test
    public void test_entityWithEnum() {
        EntityEnum entityEnum = new EntityEnum();
        entityEnum.setTestEnum(TestEnum.ONE);
        entityEnum.setOtherEnum(TestEnum.TWO);

        DBObject enumDbObject = pojoToDBObject.translate(entityEnum);

        assertEquals(TestEnum.ONE.name().toUpperCase(), enumDbObject.get("testEnum"));
        assertEquals(TestEnum.TWO.name().toUpperCase(), enumDbObject.get("otherEnum"));
    }

    @Test
    public void testEmbedded() {
        String dataStr = "asdfjkal";
        EntityWithEmbedded entity = new EntityWithEmbedded();
        entity.setEmbedOne(new EmbeddedObject());
        entity.getEmbedOne().setSomeData(dataStr);

        DBObject entityAsDBObject = pojoToDBObject.translate(entity);

        //assertEquals(DBObject.class, entityAsDBObject.get("embed").getClass());
        assertTrue(DBObject.class.isAssignableFrom(entityAsDBObject.get("embed").getClass()));
        DBObject embeddedAsDB = (DBObject) entityAsDBObject.get("embed");
        assertEquals(dataStr, embeddedAsDB.get("data"));
    }

    @Test
    public void test_IdAndDatePresentForPersisted() {
        BasicEntity basicEntity = new BasicEntity();
        basicEntity.setData("data");
        DBObject basicEntityAsDBObject = pojoToDBObject.translate(basicEntity);
        mongoDb
            .getCollection(BasicEntity.class.getAnnotation(Entity.class).name())
            .insert(basicEntityAsDBObject);
        ObjectId objectId = (ObjectId) basicEntityAsDBObject.get("_id");
        basicEntity.setId(objectId.toString());
        basicEntity.setCreatedDatetime(new Date(objectId.getTime()));

        DBObject translated = pojoToDBObject.translate(basicEntity);

        assertNotNull(translated.get("_id"));
        assertEquals(ObjectId.class, translated.get("_id").getClass());
        ObjectId translatedId = (ObjectId) translated.get("_id");
        assertEquals(objectId, translatedId);
    }

    @Test
    public void test_notEntityNoId() {
        EmbeddedObject embeddedObject = new EmbeddedObject();
        embeddedObject.setSomeData("bleh");

        DBObject embeddedAsDb = pojoToDBObject.translate(embeddedObject);

        assertNull(embeddedAsDb.get("_id"));
    }

    @Test
    public void test_notPersistedNoId() {
        BasicEntity basicEntity = new BasicEntity();
        basicEntity.setData("data");
        DBObject translated = pojoToDBObject.translate(basicEntity);

        assertNull(translated.get("_id"));
    }

    @Test
    public void testCollectionOfBuiltInTypes() {
        EntityWithCollections entity = new EntityWithCollections();
        entity.setIntArray(new Integer[]{1,2,3});
        entity.setStringSet(new HashSet(Arrays.asList(new String[]{"one", "two", "three"})));
        entity.setIntegerList(Arrays.asList(new Integer[]{1,2,3}));

        DBObject constructed = pojoToDBObject.translate(entity);
        assertEquals(BasicDBList.class, constructed.get("integerList").getClass());
        assertEquals(BasicDBList.class, constructed.get("intArray").getClass());
        assertEquals(BasicDBList.class, constructed.get("stringSet").getClass());

        BasicDBList stringSetList = (BasicDBList) constructed.get("stringSet");
        assertTrue(stringSetList.contains("one"));
        assertTrue(stringSetList.contains("two"));
        assertTrue(stringSetList.contains("three"));

        BasicDBList intListDB = (BasicDBList) constructed.get("intArray");
        assertEquals(entity.getIntArray()[0], intListDB.get(0));
    }

    @Test
    public void test_emptyCollection() {
        EntityWithCollections entity = new EntityWithCollections();
        DBObject constructed = pojoToDBObject.translate(entity);
        assertNull(constructed.get("integerList"));
        assertNull(constructed.get("stringSet"));
        assertNull(constructed.get("intArray"));
    }

}
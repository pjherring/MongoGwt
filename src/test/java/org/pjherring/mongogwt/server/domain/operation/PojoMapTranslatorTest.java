/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;

import java.util.Set;
import java.util.List;
import org.bson.types.ObjectId;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.logging.Logger;
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
import static org.junit.Assert.*;

/**
 *
 * @author pjherring
 */
public class PojoMapTranslatorTest {


    private final static Logger LOG = Logger.getLogger(PojoMapTranslatorTest.class.getName());
    private static final Injector injector = Guice.createInjector(new DataAccessTestModule(), new DatabaseTestModule());
    private DB mongoDb;
    private PojoMapTranslatorOld pojoMapTranslator;

    public PojoMapTranslatorTest() {
        mongoDb = injector.getInstance(DB.class);
        pojoMapTranslator = injector.getInstance(PojoMapTranslatorOld.class);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        mongoDb.getCollection(PojoMapDomain.class.getAnnotation(Entity.class).name()).drop();
    }

    @After
    public void tearDown() {
    }

    public static class DatabaseTestModule extends DatabaseModule {

        @Override
        protected String getHostName() {
            return "localhost";
        }

        @Override
        protected String getDatabaseName() {
            return "pojoMapTranslatorTest";
        }

        @Override
        protected List<Class<? extends IsEntity>> getEntityList() {
            List<Class<? extends IsEntity>> entitiyList = new ArrayList<Class<? extends IsEntity>>();
            entitiyList.add(PojoMapDomain.class);
            entitiyList.add(PojoMapDomainWithEmbeddedEnumerated.class);
            entitiyList.add(SimplePojo.class);
            entitiyList.add(ReferencePojo.class);
            return entitiyList;
        }

    }

    @Entity(name="pojoMapDomain")
    public static class PojoMapDomain extends BaseDomainObject {

        private String stringOne;
        private String stringTwo;
        private Integer intData;
        private Date date;

        @Column(name="stringOne")
        public String getStringOne() {
            return stringOne;
        }

        public void setStringOne(String stringOne) {
            this.stringOne = stringOne;
        }

        @Column(name="stringTwo")
        public String getStringTwo() {
            return stringTwo;
        }

        public void setStringTwo(String stringTwo) {
            this.stringTwo = stringTwo;
        }

        @Column(name="intData")
        public Integer getIntData() {
            return intData;
        }

        public void setIntData(Integer intData) {
            this.intData = intData;
        }

        @Column(name="date")
        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

    }

    @Entity(name="withEmbeddedEnumerated")
    public static class PojoMapDomainWithEmbeddedEnumerated extends BaseDomainObject {

        private EmbeddedDomain embeddedDomain;
        private TestEnum testEnum;

        @Column(name="embeddedDomain")
        @Embedded
        public EmbeddedDomain getEmbeddedDomain() {
            return embeddedDomain;
        }

        public void setEmbeddedDomain(EmbeddedDomain embeddedDomain) {
            this.embeddedDomain = embeddedDomain;
        }

        @Column(name="testEnumField")
        @Enumerated
        public TestEnum getTestEnum() {
            return testEnum;
        }

        public void setTestEnum(TestEnum testEnum) {
            this.testEnum = testEnum;
        }
    }

    @Entity(name="embeddedDomain")
    public static class EmbeddedDomain implements IsEmbeddable {
        private String embeddedString;
        private Integer embeddedInteger;

        @Column(name="embeddedString")
        public String getEmbeddedString() {
            return embeddedString;
        }

        public void setEmbeddedString(String embeddedString) {
            this.embeddedString = embeddedString;
        }

        @Column(name="embeddedInteger")
        public Integer getEmbeddedInteger() {
            return embeddedInteger;
        }

        public void setEmbeddedInteger(Integer embeddedInteger) {
            this.embeddedInteger = embeddedInteger;
        }
    }

    @Entity(name="simplePojo")
    public static class SimplePojo extends BaseDomainObject {

        private String data;

        @Column(name="data")
        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    @Entity(name="domainWithReference")
    public static class ReferencePojo extends BaseDomainObject {

        private Set<SimplePojo> simplePojoSet;
        private List<SimplePojo> simplePojoList;

        @Reference(type=ReferenceType.ONE_TO_MANY)
        @Column(name="pojoSet")
        public Set<SimplePojo> getSimplePojoSet() {
            return simplePojoSet;
        }

        public void setSimplePojoSet(Set<SimplePojo> simplePojoSet) {
            this.simplePojoSet = simplePojoSet;
        }

        @Reference(type=ReferenceType.ONE_TO_MANY)
        @Column(name="pojoList")
        public List<SimplePojo> getSimplePojoList() {
            return simplePojoList;
        }

        public void setSimplePojoList(List<SimplePojo> simplePojoList) {
            this.simplePojoList = simplePojoList;
        }
    }

    public enum TestEnum {
        ONE, TWO, THREE;
    }

    @Test
    public void test_noReference_noEmbedded_noEnumerated() {
        String stringOne = "string";
        String stringTwo = "stringTwo";
        Integer intData = 5;
        Date date = new Date();

        PojoMapDomain pojo = new PojoMapDomain();
        pojo.setDate(date);
        pojo.setIntData(intData);
        pojo.setStringOne(stringOne);
        pojo.setStringTwo(stringTwo);

        DBObject dBObject = new BasicDBObject();
        dBObject.put("stringOne", stringOne);
        dBObject.put("stringTwo", stringTwo);
        dBObject.put("intData", intData);
        dBObject.put("date", date);

        DBCollection pojoCollection =
            mongoDb.getCollection(PojoMapDomain.class.getAnnotation(Entity.class).name());

        pojoCollection.insert(dBObject);

        PojoMapDomain pojoConstructed = pojoMapTranslator.DBObjToPojo(dBObject, PojoMapDomain.class, true);

        assertEquals(((ObjectId) dBObject.get("_id")).toString(), pojoConstructed.getId());
        assertEquals(((ObjectId) dBObject.get("_id")).getTime(), pojoConstructed.getCreatedDatetime().getTime());
        assertEquals(stringOne, pojoConstructed.getStringOne());
        assertEquals(stringTwo, pojoConstructed.getStringTwo());
        assertEquals(date.getTime(), pojoConstructed.getDate().getTime());
        assertEquals(intData, pojoConstructed.getIntData());

        DBObject dbObjectConstructed = pojoMapTranslator.pojoToDBObject(pojo);
        assertEquals(stringOne, dbObjectConstructed.get("stringOne"));
        assertEquals(stringTwo, dbObjectConstructed.get("stringTwo"));
        assertEquals(intData, dbObjectConstructed.get("intData"));
        assertEquals(date, dbObjectConstructed.get("date"));

    }

    @Test
    public void test_noReference_Embedded_Enumerated() {
        Integer embeddedInteger = 5;
        String embeddedString = "testString";
        TestEnum testEnum = TestEnum.THREE;

        PojoMapDomainWithEmbeddedEnumerated pojo =
            new PojoMapDomainWithEmbeddedEnumerated();
        pojo.setEmbeddedDomain(new EmbeddedDomain());
        pojo.getEmbeddedDomain().setEmbeddedInteger(embeddedInteger);
        pojo.getEmbeddedDomain().setEmbeddedString(embeddedString);
        pojo.setTestEnum(testEnum);

        DBObject embedded = new BasicDBObject();
        embedded.put("embeddedInteger", embeddedInteger);
        embedded.put("embeddedString", embeddedString);
        DBObject dBObject = new BasicDBObject();
        dBObject.put("embeddedDomain", embedded);
        dBObject.put("testEnumField", testEnum.name());

        DBCollection dBCollection =
            mongoDb.getCollection(PojoMapDomainWithEmbeddedEnumerated.class.getAnnotation(Entity.class).name());
        dBCollection.insert(dBObject);

        PojoMapDomainWithEmbeddedEnumerated pojoConstructed =
            pojoMapTranslator.DBObjToPojo(dBObject, PojoMapDomainWithEmbeddedEnumerated.class, true);

        assertEquals(((ObjectId)dBObject.get("_id")).toString(), pojoConstructed.getId());
        assertEquals(((ObjectId) dBObject.get("_id")).getTime(), pojoConstructed.getCreatedDatetime().getTime());
        assertEquals(testEnum, pojoConstructed.getTestEnum());
        assertEquals(embeddedInteger, pojoConstructed.getEmbeddedDomain().getEmbeddedInteger());
        assertEquals(embeddedString, pojoConstructed.getEmbeddedDomain().getEmbeddedString());

        DBObject dbObjectConstructed = pojoMapTranslator.pojoToDBObject(pojo);
        DBObject embeddedConstructed = (DBObject) dbObjectConstructed.get("embeddedDomain");
        assertEquals(embeddedInteger, embeddedConstructed.get("embeddedInteger"));
        assertEquals(embeddedString, embeddedConstructed.get("embeddedString"));
        assertEquals(testEnum.name().toUpperCase(), dbObjectConstructed.get("testEnumField"));
    }

    @Test
    public void test_reference_embeddedSet() {
        final String baseDataString = "baseData";
        ReferencePojo referencePojo = new ReferencePojo();

        int[] intArray = new int[]{1, 2, 3, 4, 5};
        Set<SimplePojo> simplePojoSet = new HashSet<SimplePojo>();
        List<SimplePojo> simplePojoList = new ArrayList<SimplePojo>();

        for (int i : intArray) {
            SimplePojo simplePojo = new SimplePojo();
            simplePojo.setData(baseDataString + i);
            simplePojoList.add(simplePojo);
            simplePojoSet.add(simplePojo);
        }

        referencePojo.setSimplePojoList(simplePojoList);
        referencePojo.setSimplePojoSet(simplePojoSet);

        DBObject referenceDBObject = pojoMapTranslator.pojoToDBObject(referencePojo);
    }
}
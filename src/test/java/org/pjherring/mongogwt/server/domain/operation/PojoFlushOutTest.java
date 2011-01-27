/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;

import org.pjherring.mongogwt.shared.domain.operation.Create;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.pjherring.mongogwt.server.guice.DataAccessTestModule;
import com.mongodb.DB;
import java.util.Map;
import org.easymock.Capture;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pjherring.mongogwt.server.guice.DatabaseModule;
import org.pjherring.mongogwt.shared.BaseDomainObject;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.annotations.Column;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.annotations.Reference;
import org.pjherring.mongogwt.shared.annotations.enums.ReferenceType;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 *
 * @author pjherring
 */
public class PojoFlushOutTest extends EasyMockSupport {

    private static final Injector injector
        = Guice.createInjector(
        new DataAccessTestModule(),
        new DatabaseTestModule()
    );

    private Create create;
    private PojoFlushOut pojoFlushOut;

    public PojoFlushOutTest() {
    }

    public static class DatabaseTestModule extends DatabaseModule {

        @Override
        protected String getHostName() {
            return "localhost";
        }

        @Override
        protected String getDatabaseName() {
            return PojoFlushOutTest.class.getSimpleName();
        }

        @Override
        protected List<Class<? extends IsEntity>> getEntityList() {
            List<Class<? extends IsEntity>> entityList
                = new ArrayList<Class<? extends IsEntity>>();

            entityList.add(SimpleEntity.class);
            entityList.add(WithSimpleRef.class);
            entityList.add(OtherEntity.class);
            entityList.add(OtherSimple.class);

            return entityList;
        }

    }

    @Entity(name="simple")
    public static class SimpleEntity extends BaseDomainObject {

        private String data;
        private int intData;
        private Set<WithSimpleRef> refSet;

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
    }

    @Entity(name="withSimleReference")
    public static class WithSimpleRef extends BaseDomainObject {
        private SimpleEntity simple;
        private String data;

        @Column(name="simple")
        @Reference(type=ReferenceType.MANY_TO_ONE)
        public SimpleEntity getSimple() {
            return simple;
        }

        public void setSimple(SimpleEntity simple) {
            this.simple = simple;
        }

        @Column(name="data")
        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    @Entity(name="withManyToOneNotManaged")
    public static class OtherEntity extends BaseDomainObject {

        private String data;
        private List<OtherSimple> simples;

        @Column(name="data")
        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        @Column(name="simples")
        @Reference(type=ReferenceType.ONE_TO_MANY)
        public List<OtherSimple> getSimples() {
            return simples;
        }

        public void setSimples(List<OtherSimple> simples) {
            this.simples = simples;
        }
    }

    @Entity(name="otherSimple")
    public static class OtherSimple extends BaseDomainObject {
        private String data;
        private OtherEntity other;

        @Column(name="data")
        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        @Reference(type=ReferenceType.MANY_TO_ONE, managedBy="simples")
        public OtherEntity getOther() {
            return other;
        }

        public void setOther(OtherEntity other) {
            this.other = other;
        }
    }

    public static class PojoFlushOutMock extends PojoFlushOut {

        @Inject
        public PojoFlushOutMock(DB mongoDb, DBObjectToPojo translate) {
            super(mongoDb, translate);
        }

        public void setMap(Map map) {
            this.entityCacheMap = map;
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
        create = injector.getInstance(Create.class);
        pojoFlushOut = injector.getInstance(PojoFlushOut.class);
        pojoFlushOut.clearCache();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConstructingOfEntityReferenceMap() {
        PojoFlushOutMock pojoFlushOutMock = injector.getInstance(PojoFlushOutMock.class);
        Map mockMap = createMock(Map.class);
        pojoFlushOutMock.setMap(mockMap);

        Capture<List> entityReferenceListCapture = new Capture<List>();
        expect(mockMap.containsKey(eq(SimpleEntity.class))).andReturn(false);
        expect(mockMap.containsKey(eq(SimpleEntity.class))).andReturn(true);
        expect(mockMap.get(eq(SimpleEntity.class))).andReturn(new ArrayList<PojoFlushOut.ReferenceData>());
        expect(mockMap.put(eq(SimpleEntity.class), capture(entityReferenceListCapture))).andReturn(null);

        replay(mockMap);

        SimpleEntity entity = new SimpleEntity();
        pojoFlushOutMock.flush(entity, SimpleEntity.class, true);

        verify(mockMap);

        List<PojoFlushOut.ReferenceData> referenceList = entityReferenceListCapture.getValue();
        assertEquals(1, referenceList.size());
        PojoFlushOut.ReferenceData data = referenceList.get(0);
        assertEquals(WithSimpleRef.class, data.getReferencedClass());
        assertEquals("simple", data.getReference().managedBy());
    }

    @Test
    public void testConstructionOfEntityReferenceMap_ManyToOne() {
        PojoFlushOutMock pojoFlushOutMock = injector.getInstance(PojoFlushOutMock.class);
        Map mockMap = createMock(Map.class);
        pojoFlushOutMock.setMap(mockMap);

        Capture<List> entityReferenceListCapture = new Capture<List>();
        expect(mockMap.containsKey(eq(OtherSimple.class))).andReturn(false);
        expect(mockMap.containsKey(eq(OtherSimple.class))).andReturn(true);
        expect(mockMap.get(eq(OtherSimple.class))).andReturn(new ArrayList<PojoFlushOut.ReferenceData>());
        expect(mockMap.put(eq(OtherSimple.class), capture(entityReferenceListCapture))).andReturn(null);

        replayAll();

        OtherSimple entity = new OtherSimple();
        pojoFlushOutMock.flush(entity, OtherSimple.class, true);

        verifyAll();

        List<PojoFlushOut.ReferenceData> referenceList
            = entityReferenceListCapture.getValue();
        assertEquals(1, referenceList.size());
        PojoFlushOut.ReferenceData data = referenceList.get(0);
        assertEquals(OtherEntity.class, data.getReferencedClass());
        assertEquals("simples", data.getReference().managedBy());
        assertEquals(ReferenceType.MANY_TO_ONE, data.getReference().type());
    }

    @Test
    public void test_OneToManyNotManaged() {
        SimpleEntity simple = new SimpleEntity();
        simple.setData("data");
        simple.setIntData(5);
        create.doCreate(simple);

        WithSimpleRef withSimpleRef = new WithSimpleRef();
        withSimpleRef.setSimple(simple);
        create.doCreate(withSimpleRef);

        pojoFlushOut.flush(simple, SimpleEntity.class, true);
        assertNotNull(simple.getRefSet());
        assertEquals(1, simple.getRefSet().size());
        WithSimpleRef found = simple.getRefSet().iterator().next();
        assertEquals(withSimpleRef.getId(), found.getId());
    }

    @Test
    public void test_ManyToOneNotManaged() {
        OtherEntity otherEntity = new OtherEntity();
        otherEntity.setData("some data");

        OtherSimple simple = new OtherSimple();
        simple.setData("data");
        create.doCreate(simple);

        otherEntity.setSimples(Arrays.asList(new OtherSimple[]{simple}));
        create.doCreate(otherEntity);

        pojoFlushOut.flush(simple, OtherSimple.class, true);
        assertEquals(otherEntity.getId(), simple.getOther().getId());
    }

    @Test
    public void test_OneToManyNotManaged_doNotFanOut() {
        SimpleEntity simple = new SimpleEntity();
        simple.setData("data");
        simple.setIntData(5);
        create.doCreate(simple);

        WithSimpleRef withSimpleRef = new WithSimpleRef();
        withSimpleRef.setSimple(simple);
        withSimpleRef.setData("data");
        create.doCreate(withSimpleRef);

        pojoFlushOut.flush(simple, SimpleEntity.class, false);
        assertNotNull(simple.getRefSet());
        assertEquals(1, simple.getRefSet().size());
        WithSimpleRef found = simple.getRefSet().iterator().next();
        assertEquals(withSimpleRef.getId(), found.getId());
        assertNull(withSimpleRef.getData(), found.getData());
    }

    @Test
    public void test_MnayToOneNotManaged_doNotFanOut() {
        OtherEntity otherEntity = new OtherEntity();
        otherEntity.setData("some data");

        OtherSimple simple = new OtherSimple();
        simple.setData("data");
        create.doCreate(simple);

        otherEntity.setSimples(Arrays.asList(new OtherSimple[]{simple}));
        create.doCreate(otherEntity);

        pojoFlushOut.flush(simple, OtherSimple.class, false);
        assertEquals(otherEntity.getId(), simple.getOther().getId());
        assertNull(simple.getOther().getData());
    }
}
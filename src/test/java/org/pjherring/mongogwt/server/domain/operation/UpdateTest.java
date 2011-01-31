/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;

import org.pjherring.mongogwt.shared.domain.operation.Read;
import org.pjherring.mongogwt.shared.domain.operation.Update;
import java.util.Arrays;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mongodb.DB;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
import org.pjherring.mongogwt.shared.annotations.Reference;
import org.pjherring.mongogwt.shared.annotations.enums.ReferenceType;
import org.pjherring.mongogwt.shared.domain.operation.Create;
import org.pjherring.mongogwt.shared.exception.ValidationException;
import static org.junit.Assert.*;

/**
 *
 * @author pjherring
 */
public class UpdateTest {

    private static final Injector injector
        = Guice.createInjector(
        new DataAccessTestModule(),
        new DatabaseTestModule()
    );

    private DB mongoDb;
    private Update update;
    private Read read;
    private Create create;

    public static class DatabaseTestModule extends DatabaseModule {

        @Override
        protected String getHostName() {
            return "localhost";
        }

        @Override
        protected String getDatabaseName() {
            return UpdateTest.class.getSimpleName();
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
        private EmbedEntity embed;
        private List<String> words;

        @Column(name="data", allowNull=false)
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
            this.setRefSet(refSet);
        }

        @Embedded
        @Column(name="embed")
        public EmbedEntity getEmbed() {
            return embed;
        }

        public void setEmbed(EmbedEntity embed) {
            this.embed = embed;
        }

        @Column(name="words")
        public List<String> getWords() {
            return words;
        }

        public void setWords(List<String> words) {
            this.words = words;
        }
    }

    public static class EmbedEntity implements IsEmbeddable {
        private String data;

        @Column(name="data")
        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
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


    public UpdateTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        mongoDb = injector.getInstance(DB.class);

        mongoDb.getCollection(SimpleEntity.class.getAnnotation(Entity.class).name()).drop();
        mongoDb.getCollection(WithSimpleRef.class.getAnnotation(Entity.class).name()).drop();

        update = injector.getInstance(Update.class);
        create = injector.getInstance(Create.class);
        read = injector.getInstance(Read.class);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testUpdate_NoReferences() {
        SimpleEntity entity = new SimpleEntity();
        entity.setData("data");
        entity.setIntData(5);

        create.doCreate(entity);

        entity.setData("new Data");
        update.doUpdate(entity);

        SimpleEntity found = read.findById(entity.getId(), SimpleEntity.class, true);

        assertEquals(entity.getId(), found.getId());
        assertNotSame(entity.getData(), found.getData());
    }

    @Test
    public void testUpdate_WithReferences() {
        WithSimpleRef entity = new WithSimpleRef();
        SimpleEntity simple = new SimpleEntity();
        simple.setData("data");
        simple.setIntData(5);

        create.doCreate(simple);
        entity.setSimple(simple);
        create.doCreate(entity);

        SimpleEntity other = new SimpleEntity();
        other.setData("new");
        other.setIntData(5);

        create.doCreate(other);
        entity.setSimple(other);

        update.doUpdate(entity);

        WithSimpleRef found = read.findById(entity.getId(), WithSimpleRef.class, true);
        assertEquals(entity.getId(), found.getId());
        assertNotSame(entity.getSimple(), found.getSimple());
        assertEquals(other.getId(), found.getSimple().getId());
    }

    @Test
    public void testUpdate_withEmbedChange_Array() {
        SimpleEntity simple = new SimpleEntity();
        simple.setData("data");
        simple.setEmbed(new EmbedEntity());
        simple.getEmbed().setData("data");
        simple.setWords(Arrays.asList(new String[]{"one", "two", "three"}));

        create.doCreate(simple);
        assertNotNull(simple.getEmbed());
        assertEquals("data", simple.getEmbed().getData());

        simple.getEmbed().setData("some other data");

        update.doUpdate(simple);

        SimpleEntity foundOne = read.findById(simple.getId(), SimpleEntity.class, true);

        assertNotNull(foundOne.getEmbed());
        assertNotSame(simple.getEmbed(), foundOne.getEmbed());
        assertEquals(simple.getId(), foundOne.getId());
        assertEquals("some other data", foundOne.getEmbed().getData());

        simple = foundOne;
        simple.setWords(Arrays.asList(new String[]{"just one"}));
        update.doUpdate(simple);

        SimpleEntity foundTwo = read.findById(simple.getId(), SimpleEntity.class, true);

        assertNotSame(simple.getWords(), foundTwo.getWords());
        assertEquals(simple.getId(), foundTwo.getId());
    }

    @Test(expected=ValidationException.class)
    public void testUpdate_InvalidEntity() {
        SimpleEntity simple = new SimpleEntity();
        simple.setData("someData");

        create.doCreate(simple);

        simple.setData(null);

        update.doUpdate(simple);
    }

}
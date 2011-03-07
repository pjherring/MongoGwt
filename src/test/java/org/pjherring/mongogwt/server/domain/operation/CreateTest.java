/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
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
import org.pjherring.mongogwt.shared.domain.operation.Create;
import org.pjherring.mongogwt.shared.exception.AlreadyPersistedException;
import org.pjherring.mongogwt.shared.exception.InvalidEntity;
import static org.junit.Assert.*;

/**
 *
 * @author pjherring
 */
public class CreateTest {

    private static final Injector injector =
        Guice.createInjector(new DatabaseTestModule());
    private static DB mongoDb;
    private static Create create;

    public CreateTest() {
    }

    public static class DatabaseTestModule extends MongoDatabaseModule {

        @Override
        protected String getHostName() {
            return "localhost";
        }

        @Override
        protected String getDatabaseName() {
            return CreateTest.class.getSimpleName();
        }

        @Override
        protected List<Class<? extends IsEntity>> getEntityList() {
            List<Class<? extends IsEntity>> entityList =
                new ArrayList<Class<? extends IsEntity>>();
            entityList.add(SimpleDomain.class);
            entityList.add(WithEmbed.class);

            return entityList;
        }

    }

    @Entity(name="simple")
    public static class SimpleDomain extends BaseDomainObject {
        private String data;

        @Column(name="data")
        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    public static class NotAnnotatedEntity extends BaseDomainObject {}

    @Entity(name="notInList")
    public static class NotInList extends BaseDomainObject {}

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

    @Entity(name="withEmbed")
    public static class WithEmbed extends BaseDomainObject {

        private EmbedEntity embedEntity;

        @Column(name="embed")
        @Embedded
        public EmbedEntity getEmbedEntity() {
            return embedEntity;
        }

        public void setEmbedEntity(EmbedEntity embedEntity) {
            this.embedEntity = embedEntity;
        }
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        create = injector.getInstance(Create.class);
        mongoDb = injector.getInstance(DB.class);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testPersistance() {
        SimpleDomain simple = new SimpleDomain();
        simple.setData("data");

        create.doCreate(simple);
        assertNotNull(simple.getId());
        assertNotNull(simple.getCreatedDatetime());

        DBObject queryObject = new BasicDBObject();
        queryObject.put("_id", new ObjectId(simple.getId()));

        DBObject found = mongoDb
            .getCollection(SimpleDomain.class.getAnnotation(Entity.class).name())
            .findOne(queryObject);

        assertEquals(simple.getData(), found.get("data"));
    }

    @Test(expected=InvalidEntity.class)
    public void invalidEntity_notAnnotated() {
        NotAnnotatedEntity entity = new NotAnnotatedEntity();
        create.doCreate(entity);
    }

    @Test(expected=AlreadyPersistedException.class)
    public void invalidEntity_alreadyPersisted() {
        SimpleDomain simple = new SimpleDomain();
        simple.setData("data");

        create.doCreate(simple);
        create.doCreate(simple);
    }

    @Test(expected=InvalidEntity.class)
    public void invalidEntity_notInList() {
        NotInList entity = new NotInList();
        create.doCreate(entity);
    }

    @Test
    public void persistWithEmbedded() {
        WithEmbed entity = new WithEmbed();
        entity.setEmbedEntity(new EmbedEntity());
        entity.getEmbedEntity().setData("data");

        create.doCreate(entity);
        assertEquals("data", entity.getEmbedEntity().getData());
    }

}
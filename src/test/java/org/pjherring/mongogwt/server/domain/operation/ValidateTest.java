/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;

import org.pjherring.mongogwt.server.domain.translate.PojoToDBObject;
import org.pjherring.mongogwt.shared.domain.operation.Validate;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import org.pjherring.mongogwt.shared.annotations.Unique;
import org.pjherring.mongogwt.shared.domain.validator.Validator;
import org.pjherring.mongogwt.shared.domain.validator.ValidatorHook;
import org.pjherring.mongogwt.shared.exception.LengthException;
import org.pjherring.mongogwt.shared.exception.NullableException;
import org.pjherring.mongogwt.shared.exception.RegexpException;
import org.pjherring.mongogwt.shared.exception.UniqueException;
import org.pjherring.mongogwt.shared.exception.ValidationException;
import static org.junit.Assert.*;

/**
 *
 * @author pjherring
 */
public class ValidateTest {

    private static final Injector injector =
        Guice.createInjector(new DatabaseTestModule());
    private static Validate validate;
    private static DB mongoDb;
    private static PojoToDBObject translatePojoToDb;

    public static class DatabaseTestModule extends MongoDatabaseModule {

        @Override
        protected String getHostName() {
            return "localhost";
        }

        @Override
        protected String getDatabaseName() {
            return ValidateTest.class.getSimpleName();
        }

        @Override
        protected List<Class<? extends IsEntity>> getEntityList() {
            List<Class<? extends IsEntity>> entityList =
                new ArrayList<Class<? extends IsEntity>>();

            return entityList;
        }

    }

    @Entity(name="validate")
    public static class ValidateEntity extends BaseDomainObject {

        private String data;

        @Column(name="data", minLength=5, maxLength=40, allowNull=false, regexp="\\d{2,}", unique=true)
        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

    }

    @Entity(name="unique")
    @Unique({"uniqueOne", "uniqueTwo"})
    public static class UniqueEntity extends BaseDomainObject {
        private String uniqueOne;
        private String uniqueTwo;

        @Column(name="uniqueOne")
        public String getUniqueOne() {
            return uniqueOne;
        }

        public void setUniqueOne(String uniqueOne) {
            this.uniqueOne = uniqueOne;
        }

        @Column(name="uniqueTwo")
        public String getUniqueTwo() {
            return uniqueTwo;
        }

        public void setUniqueTwo(String uniqueTwo) {
            this.uniqueTwo = uniqueTwo;
        }
    }

    @Entity(name="withEmbedded")
    public static class EntityWEmbedded extends BaseDomainObject {
        private EmbeddedEntity embeddedEntity;

        @Column(name="embedded", allowNull=false)
        @Embedded
        public EmbeddedEntity getEmbeddedEntity() {
            return embeddedEntity;
        }

        public void setEmbeddedEntity(EmbeddedEntity embeddedEntity) {
            this.embeddedEntity = embeddedEntity;
        }
    }

    public static class EmbeddedEntity implements IsEmbeddable {
        private String data;

        @Column(name="data", allowNull=false, regexp="[a-d]")
        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    @Entity(name="withValidator")
    @ValidatorHook({ValidatorTest.class})
    public static class EntityValidatorHook extends BaseDomainObject {
        private String dataOne;
        private String dataTwo;

        @Column(name="dataOne", allowNull=true)
        public String getDataOne() {
            return dataOne;
        }

        public void setDataOne(String dataOne) {
            this.dataOne = dataOne;
        }

        @Column(name="dataTwo", allowNull=true)
        public String getDataTwo() {
            return dataTwo;
        }

        public void setDataTwo(String dataTwo) {
            this.dataTwo = dataTwo;
        }
    }

    public static class ValidatorTest extends Validator<EntityValidatorHook> {

        @Override
        public boolean isValid(EntityValidatorHook entity) {
            if (entity.getDataOne() == null && entity.getDataTwo() == null) {
                return false;
            }

            return true;
        }

    }

    public ValidateTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        validate = injector.getInstance(Validate.class);
        mongoDb = injector.getInstance(DB.class);
        translatePojoToDb = injector.getInstance(PojoToDBObject.class);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        mongoDb.getCollection(ValidateEntity.class.getAnnotation(Entity.class).name()).drop();
        mongoDb.getCollection(UniqueEntity.class.getAnnotation(Entity.class).name()).drop();
    }

    @After
    public void tearDown() {
    }

    @Test(expected=ValidationException.class)
    public void testValidatorHookFailure() {
        EntityValidatorHook entity = new EntityValidatorHook();
        validate.validate(entity);
    }

    @Test
    public void testValidatorHookSuccess() {
        EntityValidatorHook entity = new EntityValidatorHook();
        entity.setDataOne("data");
        validate.validate(entity);
    }

    @Test(expected=NullableException.class)
    public void test_nullable() {
        ValidateEntity entity = new ValidateEntity();
        validate.validate(entity);
    }

    @Test(expected=LengthException.class)
    public void test_MinLengthException() {
        ValidateEntity entity = new ValidateEntity();
        entity.setData("d22");
        validate.validate(entity);
    }

    @Test(expected=LengthException.class)
    public void test_MaxLengthException() {
        ValidateEntity entity = new ValidateEntity();
        entity.setData("data data data data data da22ta data data data data ");
        validate.validate(entity);
    }

    @Test(expected=RegexpException.class)
    public void test_regularExpression() {
        ValidateEntity entity = new ValidateEntity();
        //will fail because of no numbers
        entity.setData("asdfasdfa");
        validate.validate(entity);
    }

    @Test(expected=UniqueException.class)
    public void test_uniqueExceptionInColumnAnnotation() {
        String dataValue = "unique22";
        DBObject dBObject = new BasicDBObject();
        dBObject.put("data", dataValue);

        mongoDb.getCollection(ValidateEntity.class.getAnnotation(Entity.class).name())
            .insert(dBObject);

        ValidateEntity entity = new ValidateEntity();
        entity.setData(dataValue);
        validate.validate(entity);
    }

    @Test(expected=UniqueException.class)
    public void test_uniqueExceptionInUniqueAnnotation() {
        String uniqueOne = "uniqueOne";
        String uniqueTwo = "uniqueTwo";
        UniqueEntity uniqueEntity = new UniqueEntity();
        uniqueEntity.setUniqueOne(uniqueOne);
        uniqueEntity.setUniqueTwo(uniqueTwo);

        mongoDb.getCollection(UniqueEntity.class.getAnnotation(Entity.class).name())
            .insert(translatePojoToDb.translate(uniqueEntity));

        validate.validate(uniqueEntity);
    }

    @Test
    public void test_cancelValidation() {
        ValidateEntity entity = new ValidateEntity();
        validate.validate(entity, false);
    }

    @Test
    public void test_validationMap() {
        ValidateEntity entity = new ValidateEntity();
        validate.validate(entity, false);
        Map<String, List<ValidationException>> validationErrorMap
            = validate.getValidationErrorMap();

        assertEquals(NullableException.class, validationErrorMap.get("data").get(0).getClass());

        entity.setData("d33");
        validate.validate(entity, false);
        validationErrorMap = validate.getValidationErrorMap();
        assertEquals(LengthException.class, validationErrorMap.get("data").get(0).getClass());

        entity.setData("datata");
        validate.validate(entity, false);
        validationErrorMap = validate.getValidationErrorMap();
        assertEquals(RegexpException.class, validationErrorMap.get("data").get(0).getClass());

        entity.setData("too long too long too long too long too long too long too long too long too long too long too long ");
        validate.validate(entity, false);
        validationErrorMap = validate.getValidationErrorMap();
        for (ValidationException exception : validationErrorMap.get("data")) {
            assertTrue(exception.getClass().getName(), exception.getClass().equals(LengthException.class)
                || exception.getClass().equals(RegexpException.class));
        }
    }

    @Test
    public void test_uniqueValidationMap() {
        String dataValue = "unique22";
        DBObject dBObject = new BasicDBObject();
        dBObject.put("data", dataValue);

        mongoDb.getCollection(ValidateEntity.class.getAnnotation(Entity.class).name())
            .insert(dBObject);

        ValidateEntity entity = new ValidateEntity();
        entity.setData(dataValue);
        validate.validate(entity, false);
        Map<String, List<ValidationException>> map
            = validate.getValidationErrorMap();
        assertEquals(UniqueException.class, map.get("data").get(0).getClass());
    }

    @Test
    public void test_uniqueValidationMapUniqueAnnotation() {
        String uniqueOne = "uniqueOne";
        String uniqueTwo = "uniqueTwo";
        UniqueEntity uniqueEntity = new UniqueEntity();
        uniqueEntity.setUniqueOne(uniqueOne);
        uniqueEntity.setUniqueTwo(uniqueTwo);

        mongoDb.getCollection(UniqueEntity.class.getAnnotation(Entity.class).name())
            .insert(translatePojoToDb.translate(uniqueEntity));

        validate.validate(uniqueEntity, false);
        Map<String, List<ValidationException>> map
            = validate.getValidationErrorMap();
        assertEquals(UniqueException.class, map.get("uniqueOne").get(0).getClass());
        assertEquals(UniqueException.class, map.get("uniqueTwo").get(0).getClass());
    }

    @Test(expected=NullableException.class)
    public void test_EmbeddedValue_nullValue() {
        EntityWEmbedded entity = new EntityWEmbedded();
        validate.validate(entity);
    }

    @Test(expected=NullableException.class)
    public void test_EmbeddedValue_NullExceptionOfEmbed() {
        EntityWEmbedded entity = new EntityWEmbedded();
        entity.setEmbeddedEntity(new EmbeddedEntity());
        validate.validate(entity);
    }
}
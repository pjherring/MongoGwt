/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.guice;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.mongodb.DB;
import com.mongodb.DBAddress;
import com.mongodb.Mongo;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Logger;
import org.pjherring.mongogwt.shared.IsEntity;

/**
 *
 * @author pjherring
 */
public abstract class DatabaseModule extends AbstractModule {

    private static final Logger LOG
        = Logger.getLogger(DatabaseModule.class.getName());

    protected TypeLiteral entityListTypeLiteral = new TypeLiteral<List<Class<? extends IsEntity>>>(){;};

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @BindingAnnotation
    public @interface EntityList {}

    @Override
    protected void configure() {
        bind(entityListTypeLiteral).annotatedWith(EntityList.class)
            .toInstance(getEntityList());
    }

    @Provides
    @Singleton
    protected DB getDatabase() {
        try {
            return Mongo.connect(new DBAddress(getHostName(), getDatabaseName()));
        } catch (UnknownHostException e) {
            throw new Error(e);
        }
    }


    protected abstract String getHostName();
    protected abstract String getDatabaseName();
    protected abstract List<Class<? extends IsEntity>> getEntityList();

}

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
import org.pjherring.mongogwt.server.domain.operation.Delete;
import org.pjherring.mongogwt.server.domain.DatabaseImpl;
import org.pjherring.mongogwt.server.domain.operation.Read;
import org.pjherring.mongogwt.server.domain.operation.Save;
import org.pjherring.mongogwt.server.domain.operation.Validate;
import org.pjherring.mongogwt.shared.domain.operation.DoesCreate;
import org.pjherring.mongogwt.shared.domain.operation.DoesDelete;
import org.pjherring.mongogwt.shared.domain.operation.DoesRead;
import org.pjherring.mongogwt.shared.domain.operation.DoesUpdate;
import org.pjherring.mongogwt.shared.domain.operation.DoesValidate;
import org.pjherring.mongogwt.shared.domain.operation.Database;

/**
 *
 * @author pjherring
 */
public abstract class DatabaseModule extends AbstractModule {

    private static final Logger LOG = Logger.getLogger("Guice DatabaseModule");

    protected TypeLiteral stringList = new TypeLiteral<List<String>>(){;};

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @BindingAnnotation
    public @interface CollectionNames {}

    @Override
    protected void configure() {
        bind(DoesCreate.class).to(Save.class);
        bind(DoesUpdate.class).to(Save.class);
        bind(DoesDelete.class).to(Delete.class);
        bind(DoesRead.class).to(Read.class);
        bind(DoesValidate.class).to(Validate.class);

        bind(Database.class).to(DatabaseImpl.class).in(Singleton.class);

        bindCollectionNames();
    }

    @Provides
    @Singleton
    public DB getDatabaseProvider() {
        try {
            return Mongo.connect(new DBAddress(getHostName(), getDatabaseName()));
        } catch (UnknownHostException e) {
            throw new Error(e);
        }
    }

    protected abstract String getHostName();
    protected abstract String getDatabaseName();
    protected abstract void bindCollectionNames();

}

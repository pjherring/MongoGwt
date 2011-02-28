/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;


import com.google.inject.Inject;
import java.util.List;
import java.util.logging.Logger;
import org.bson.types.ObjectId;
import org.pjherring.mongogwt.server.domain.hook.DataAccessHookRunner;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.domain.hook.DataAccessHook.What;
import org.pjherring.mongogwt.shared.domain.hook.DataAccessHook.When;
import org.pjherring.mongogwt.shared.domain.operation.Create;
import org.pjherring.mongogwt.shared.domain.operation.Delete;
import org.pjherring.mongogwt.shared.domain.operation.Read;
import org.pjherring.mongogwt.shared.domain.operation.Update;
import org.pjherring.mongogwt.shared.domain.operation.Validate;
import org.pjherring.mongogwt.shared.exception.NotFoundException;
import org.pjherring.mongogwt.shared.exception.NotPersistedException;
import org.pjherring.mongogwt.shared.exception.ValidationException;
import org.pjherring.mongogwt.shared.query.Query;

/**
 *
 * @author pjherring
 */
public class Database {

    private final static Logger LOG = Logger.getLogger(Database.class.getName());

    protected Create create;
    protected Read read;
    protected Update update;
    protected Delete delete;
    protected DataAccessHookRunner dataAccessHookRunner;

    @Inject
    public Database(
        Create create,
        Read read,
        Update update,
        Delete delete,
        DataAccessHookRunner dataAccessHookRunner) {

        this.create = create;
        this.read = read;
        this.update = update;
        this.delete = delete;
        this.dataAccessHookRunner = dataAccessHookRunner;

    }

    public IsEntity create(IsEntity domainObject) throws ValidationException {

        dataAccessHookRunner.runDataAccessHooks(domainObject, domainObject.getClass(), When.PRE, What.CREATE);
        create.doCreate(domainObject);
        dataAccessHookRunner.runDataAccessHooks(domainObject, domainObject.getClass(), When.POST, What.CREATE);
        return domainObject;
    }

    public <T extends IsEntity> List<T> find(Query query, Class<T> clazz, boolean doFanOut) throws NotFoundException {
        dataAccessHookRunner.runDataAccessHooks(query, clazz, When.PRE, What.READ);
        List<T> results = read.find(query, clazz, doFanOut);
        dataAccessHookRunner.runDataAccessHooks(results, clazz, When.POST, What.READ);

        return results;
    }

    public <T extends IsEntity> T findOne(Query query, Class<T> clazz, boolean doFanOut) throws NotFoundException {

        dataAccessHookRunner.runDataAccessHooks(query, clazz, When.PRE, What.READ);
        T toReturn =  read.findOne(query, clazz, doFanOut);
        dataAccessHookRunner.runDataAccessHooks(toReturn, clazz, When.POST, What.READ);

        return toReturn;
    }

    public <T extends IsEntity> T findById(String id, Class<T> clazz, boolean doFanOut) throws NotFoundException {
        Query findByIdQuery = new Query().start("_id").is(new ObjectId(id));

        dataAccessHookRunner.runDataAccessHooks(findByIdQuery, clazz, When.PRE, What.READ);
        T toReturn =  read.findById(id, clazz, doFanOut);
        dataAccessHookRunner.runDataAccessHooks(toReturn, clazz, When.POST, What.READ);

        return toReturn;
    }

    public <T extends IsEntity> T update(T domainObject) throws ValidationException {
        dataAccessHookRunner.runDataAccessHooks(domainObject, domainObject.getClass(), When.PRE, What.UPDATE);
        T toReturn = update(domainObject);
        dataAccessHookRunner.runDataAccessHooks(toReturn, domainObject.getClass(), When.POST, What.UPDATE);
        return toReturn;
    }

    public void delete(Query query, Class<? extends IsEntity> clazz) throws NotFoundException, NotPersistedException {
        dataAccessHookRunner.runDataAccessHooks(query, clazz, When.PRE, What.DELETE);
        delete.delete(query, clazz);
        dataAccessHookRunner.runDataAccessHooks(query, clazz, When.POST, What.DELETE);
    }

    public void delete(IsEntity domainObject) throws NotPersistedException {
        dataAccessHookRunner.runDataAccessHooks(domainObject, domainObject.getClass(), When.PRE, What.DELETE);
        delete.delete(domainObject);
        dataAccessHookRunner.runDataAccessHooks(domainObject, domainObject.getClass(), When.POST, What.DELETE);
    }

    public <T extends IsEntity> T refresh(IsEntity domainObject, Class<T> clazz) throws NotFoundException, NotPersistedException {
        return findById(domainObject.getId(), clazz, true);
    }
}

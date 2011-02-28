/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.hook;


import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import org.pjherring.mongogwt.shared.domain.hook.DataAccessHook.Hooks;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.domain.hook.DataAccessHook;
import org.pjherring.mongogwt.shared.query.Query;

/**
 * I'm sure this class coudl refactored to be smaller, but it seems dumb
 * to have this entangled logic one method all of the public methods would call.
 *
 * @author pjherring
 */
public class DataAccessHookRunner {

    private final static Logger LOG = Logger.getLogger(DataAccessHookRunner.class.getName());
    private final Injector injector = Guice.createInjector();

    /*
     * @param entity The entity to use while running the hooks.
     * @param clazz The class of the entity.
     * @param when Either PRE OR POST
     * @param what Create, Read, Update, Delete
     */
    public void runDataAccessHooks(
        IsEntity entity,
        Class<? extends IsEntity> clazz,
        DataAccessHook.When when,
        DataAccessHook.What what) {


        if (clazz.isAnnotationPresent(Hooks.class)) {
            Class[] hooks = getHooks(
                clazz.getAnnotation(Hooks.class),
                when,
                what
            );

            for (Class hook : hooks) {
                if (BaseDataAccessHook.class.isAssignableFrom(hook)) {
                    BaseDataAccessHook baseDataHook =
                        injector.getInstance((Class<? extends BaseDataAccessHook>) hook);

                    baseDataHook.setDomainObject(entity);

                    if (baseDataHook.doRun()) {
                        baseDataHook.run();
                    }
                } else {
                    throw new RuntimeException("Invalid hook: " + hook.getName() + ". Must implement " + BaseDataAccessHook.class.getName());
                }
            }
        }
    }

    /*
     * @param query The query to use while running the hooks.
     * @param clazz The class of the entity.
     * @param when Either PRE OR POST
     * @param what Create, Read, Update, Delete
     */
    public void runDataAccessHooks(
        Query query,
        Class<? extends IsEntity>  clazz,
        DataAccessHook.When when,
        DataAccessHook.What what) {

        if (clazz.isAnnotationPresent(Hooks.class)) {
            Class[] hooks = getHooks(
                clazz.getAnnotation(Hooks.class),
                when,
                what
            );

            for (Class hook : hooks) {
                if (BaseDataAccessHook.class.isAssignableFrom(hook)) {
                    BaseDataAccessHook baseDataHook =
                        injector.getInstance((Class<? extends BaseDataAccessHook>) hook);

                    baseDataHook.setQuery(query);

                    if (baseDataHook.doRun()) {
                        baseDataHook.run();
                    }

                } else {
                    throw new RuntimeException("Invalid hook: " + hook.getName() + ". Must implement " + BaseDataAccessHook.class.getName());
                }
            }
        }
    }

    /*
     * A convenience methods for collections. Could just loop through in database, but
     * this actually only gets the hooks once and runs them all. Rather than looping
     * through all the hooks.
     *
     * @param collection The collection to run hooks on.
     * @param clazz The class of the entity that the collection contains
     * @param when PRE OR POST
     * @param what CREATE, READ, UPDATE, OR DELETE
     */
    public <T extends IsEntity> void runDataAccessHooks(
        Collection<T> collection,
        Class<T> clazz,
        DataAccessHook.When when,
        DataAccessHook.What what) {

        if (clazz.isAnnotationPresent(Hooks.class)) {
            Class[] hooks = getHooks(
                clazz.getAnnotation(Hooks.class),
                when,
                what
            );

            for (Class hook : hooks) {
                if (BaseDataAccessHook.class.isAssignableFrom(hook)) {
                    BaseDataAccessHook baseDataHook =
                        injector.getInstance((Class<? extends BaseDataAccessHook>) hook);

                    List<T> entityList = new ArrayList<T>(collection);

                    for (T entity : entityList) {
                        baseDataHook.setDomainObject(entity);
                        if (baseDataHook.doRun()) {
                            baseDataHook.run();
                        }
                    }

                } else {
                    throw new RuntimeException("Invalid hook: " + hook.getName() + ". Must implement " + BaseDataAccessHook.class.getName());
                }
            }
        }
    }

    protected Class[] getHooks(Hooks hooks, DataAccessHook.When when, DataAccessHook.What what) {
        switch (what) {
            case CREATE:
                return when.equals(DataAccessHook.When.PRE)
                    ? hooks.preCreate()
                    : hooks.postCreate();
            case READ:
                return when.equals(DataAccessHook.When.PRE)
                    ? hooks.preRead()
                    : hooks.postRead();
            case UPDATE:
                return when.equals(DataAccessHook.When.PRE)
                    ? hooks.preUpdate()
                    : hooks.postUpdate();
            case DELETE:
                return when.equals(DataAccessHook.When.PRE)
                    ? hooks.preDelete()
                    : hooks.postDelete();
        }

        //should never get here
        throw new RuntimeException("Invalid Hooks.");
    }

}

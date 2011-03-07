/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;


import com.google.inject.Inject;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.logging.Logger;
import org.pjherring.mongogwt.server.domain.cache.ColumnMeta;
import org.pjherring.mongogwt.server.domain.cache.EntityMetaCache;
import org.pjherring.mongogwt.server.domain.cache.EntitySqlCache;
import org.pjherring.mongogwt.server.domain.cache.StatementMeta;
import org.pjherring.mongogwt.shared.IsEntity;

/**
 *
 * @author pjherring
 */
public class SqlCreate {

    private final static Logger LOG = Logger.getLogger(SqlCreate.class.getName());
    protected EntityMetaCache entityMetaCache;
    protected EntitySqlCache sqlCache;

    @Inject
    public SqlCreate(EntityMetaCache entityMetaCache, EntitySqlCache sqlCache) {
        this.entityMetaCache = entityMetaCache;
        this.sqlCache = sqlCache;
    }

    public <T extends IsEntity> void create(T entity) {
        StatementMeta statementMeta
            = sqlCache.getInsertStatement(entity.getClass());
        Set<ColumnMeta> columnMetaSet =
            entityMetaCache.getColumnMetaSet(entity.getClass());

        for (ColumnMeta columnMeta : columnMetaSet) {
            int index = statementMeta.getColumns()
                .indexOf(columnMeta.getColumnAnnotation().name());

            Method getter = columnMeta.getGetter();

            Object value = null;

            try {
                value = getter.invoke(entity);
            } catch (Exception e) {
                LOG.warning("ERROR IN INVOKING GETTER");
                throw new RuntimeException(e);
            }

            try {
                statementMeta.getPreparedStatement().setObject(index, value);
            } catch (Exception e) {
                LOG.warning("ERROR IN SETTING COLUMN "
                    + columnMeta.getColumnAnnotation().name());
                throw new RuntimeException(e);
            }
        }

        try {
            statementMeta.getPreparedStatement().execute();
        } catch (Exception e) {
            LOG.warning("ERROR IN EXECUTING INSERT");
            throw new RuntimeException(e);
        }
    }

}

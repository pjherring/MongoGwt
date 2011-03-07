/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.cache;


import com.google.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.exception.InvalidEntity;

/**
 *
 * @author pjherring
 */
public class EntitySqlCache {

    private final static Logger LOG = Logger.getLogger(EntitySqlCache.class.getName());

    protected EntityMetaCache entityMetaCache;
    protected Connection connection;

    //cache maps
    protected Map<Class<? extends IsEntity>, StatementMeta> insertCache =
        new HashMap<Class<? extends IsEntity>, StatementMeta>();
    protected Long insertCacheHit = 0L;
    protected Long insertCacheMiss = 0L;

    @Inject
    public EntitySqlCache(Connection connection, EntityMetaCache entityMetaCache) {
        this.entityMetaCache = entityMetaCache;
        this.connection = connection;
    }

    public StatementMeta getInsertStatement(Class<? extends IsEntity> clazz) {

        if (!insertCache.containsKey(clazz)) {
            insertCacheMiss++;
            insertCache.put(clazz, constructStatementMeta(clazz));
        } else {
            insertCacheHit++;
        }

        return insertCache.get(clazz);
    }

    protected StatementMeta constructStatementMeta(Class<? extends IsEntity> clazz) {
        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new InvalidEntity(clazz.getName());
        }

        Set<ColumnMeta> columnMetaSet =
            entityMetaCache.getColumnMetaSet(clazz);

        StringBuilder sqlBuilder = new StringBuilder();
        StringBuilder valueBuilder = new StringBuilder();

        List<String> columns = new ArrayList<String>();


        Map<String, String> keyValuePairs = new HashMap<String, String>();

        //iterate through to get column names and values
        for (ColumnMeta columnMeta : columnMetaSet) {
            //adding the comma here will prevent trailing commas
            if (sqlBuilder.toString().length() > 0) {
                sqlBuilder.append(", ");
                valueBuilder.append(", ");
            }

            //add the column name
            String columnName = columnMeta.getColumnAnnotation().name();
            columns.add(columnName);
            sqlBuilder.append(columnName);
            valueBuilder.append("?");

        }


        sqlBuilder.insert(0, "(");
        sqlBuilder.append(") ");
        valueBuilder.insert(0, "(");
        valueBuilder.append(")");
        sqlBuilder.insert(0,
            "INSERT "
                + clazz.getAnnotation(Entity.class).name()
                + " "
        );
        sqlBuilder.append("VALUES ");
        sqlBuilder.append(valueBuilder.toString());

        try {
            PreparedStatement preparedStatement =
                connection.prepareStatement(sqlBuilder.toString());

            StatementMeta statementMeta = new StatementMeta();
            statementMeta.setColumns(columns);
            statementMeta.setPreparedStatement(preparedStatement);

            return statementMeta;

        } catch (Exception e) {
            LOG.warning("error in getting connection");
            throw new RuntimeException(e);
        }

    }

}

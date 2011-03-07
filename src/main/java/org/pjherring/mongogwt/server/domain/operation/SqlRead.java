/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;


import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.domain.operation.Read;
import org.pjherring.mongogwt.shared.exception.InvalidQuery;
import org.pjherring.mongogwt.shared.query.Query;

/**
 *
 * @author pjherring
 */
public class SqlRead implements Read {

    private final static Logger LOG = Logger.getLogger(SqlRead.class.getName());

    protected Connection connection;

    public SqlRead(Connection connection) {
        this.connection = connection;
    }

    @Override
    public <T extends IsEntity> List<T> find(Query query, Class<T> clazz, boolean doFanOut) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T extends IsEntity> T findOne(Query query, Class<T> clazz, boolean doFanOut) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends IsEntity> T findById(String id, Class<T> clazz, boolean doFanOut) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected String queryToSql(Query query, Class<? extends IsEntity> clazz) {
        List<String> prefixes = new ArrayList<String>();
        Map<String, String> tableNameToPrefix = new HashMap<String, String>();

        //just get the first prefix
        String tableName = clazz.getAnnotation(Entity.class).name();
        String fromPrefix = tableName.substring(0, 1);
        tableNameToPrefix.put(tableName, fromPrefix);
            

        StringBuilder builder = new StringBuilder();

        if (query.getColumns().isEmpty()) {
            builder.append("SELECT *");
        } else {

            for (String columnName : query.getColumns()) {
                if (builder.toString().length() > 0) {
                    builder.append(", ");
                }


                if (columnName.contains(".")) {
                    String[] parts = columnName.split("\\.");
                    //get the first letter
                    String prefix = parts[0].substring(0, 1);

                    /*
                     * If we have another prefix that begins with the same letter
                     * add numbers 1 to it until we have a unique prefix
                     */
                    int count = 1;

                    while (tableNameToPrefix.values().contains(prefix)) {
                        prefix = prefix + "1";
                    }

                    tableNameToPrefix.put(parts[0], prefix);

                    builder.append(prefix);
                    builder.append(".");
                    builder.append(parts[1]);
                } else {
                    builder.append(fromPrefix);
                    builder.append(".");
                    builder.append(columnName);
                }
            }

            builder.insert(0, "SELECT ");
        }

        builder.append(" FROM ");
        builder.append(tableName);
        builder.append(" ");
        builder.append(fromPrefix);

        StringBuilder joinBuilder = new StringBuilder();

        //HANDLE JOINS
        for (String entityName : tableNameToPrefix.keySet()) {

            if (isListedEntity(entityName) && !entityName.equals(tableName)) {
                joinBuilder.append("JOIN ");
                joinBuilder.append(entityName);
                joinBuilder.append(" ");
                joinBuilder.append(tableNameToPrefix.get(entityName));
            }
        }

        if (joinBuilder.toString().length() != 0) {
            builder.append(" ");
            builder.append(joinBuilder.toString());
        }

        //HANDLE WHERE
        Map<String, Object> queryMap = query.getQueryMap();

        if (!query.getQueryMap().isEmpty()) {
            StringBuilder whereBuilder = new StringBuilder();
            builder.append(" WHERE");

            for (String columnName : queryMap.keySet()) {
                builder.append(" ");
                builder.append(fromPrefix);
                builder.append(".");
                builder.append(columnName);
                builder.append(" = ?");
            }

        }

        return builder.toString();
    }

    protected boolean isListedEntity(String entityName) {
        return true;
    }


}

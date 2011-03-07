/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.cache;


import java.sql.PreparedStatement;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author pjherring
 */
public class StatementMeta {

    private final static Logger LOG = Logger.getLogger(StatementMeta.class.getName());

    private PreparedStatement preparedStatement;
    private List<String> columns;

    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    public void setPreparedStatement(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

}

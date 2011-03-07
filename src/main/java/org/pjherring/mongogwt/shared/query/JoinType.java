/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.query;

/**
 *
 * @author pjherring
 */
public enum JoinType {
    INNER("INNER JOIN"), LEFT("LEFT JOIN"), OUTER("OUTER JOIN"), LEFT_OUTER("LEFT OUTER JOIN");

    private String asSql;

    JoinType(String asSql) {
        this.asSql = asSql;
    }

    public String asSql() {
        return asSql;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared;

import java.util.Date;

/**
 *
 * @author pjherring
 */
public interface IsEntity extends IsStorable {

    String getId();
    void setId(String id);

    Date getCreatedDatetime();
    void setCreatedDatetime(Date createdDatetime);
}

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
public abstract class BaseDomainObject implements IsEntity {

    protected String id;
    protected String primaryId;
    protected Date createdDatetime;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getPrimaryId() {
        return primaryId;
    }

    @Override
    public void setPrimaryId(String primaryId) {
        this.primaryId = primaryId;
    }

    @Override
    public Date getCreatedDatetime() {
        return createdDatetime;
    }

    @Override
    public void setCreatedDatetime(Date createdDatetime) {
        this.createdDatetime = createdDatetime;
    }

}

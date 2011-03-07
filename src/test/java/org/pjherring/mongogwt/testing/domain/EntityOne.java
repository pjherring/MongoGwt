/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.testing.domain;


import java.util.Date;
import org.pjherring.mongogwt.shared.BaseDomainObject;
import org.pjherring.mongogwt.shared.annotations.Column;
import org.pjherring.mongogwt.shared.annotations.Entity;

/**
 *
 * @author pjherring
 */
@Entity(name="one")
public class EntityOne extends BaseDomainObject {


    private String data;
    private Date date;
    private boolean isSomething;

    @Column(name="data", allowNull=false)
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Column(name="date", allowNull=false)
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Column(name="isSomething", allowNull=false)
    public boolean getIsSomething() {
        return isSomething;
    }

    public void setIsSomething(boolean isSomething) {
        this.isSomething = isSomething;
    }
}

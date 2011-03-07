/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.testing.domain;


import java.util.Date;
import java.util.logging.Logger;
import org.pjherring.mongogwt.shared.BaseDomainObject;
import org.pjherring.mongogwt.shared.annotations.Column;
import org.pjherring.mongogwt.shared.annotations.Entity;

/**
 *
 * @author pjherring
 */
@Entity(name="two")
public class EntityTwo extends BaseDomainObject {

    private String data;
    private Date date;

    @Column(name="data", allowNull=true)
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

}

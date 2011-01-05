/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.domain;


import java.util.logging.Logger;
import org.pjherring.mongogwt.shared.BaseDomainObject;
import org.pjherring.mongogwt.shared.annotations.Column;
import org.pjherring.mongogwt.shared.annotations.Entity;

/**
 *
 * @author pjherring
 */
@Entity(name="simple")
public class SimpleDomain extends BaseDomainObject {

    private final static Logger LOG = Logger.getLogger(SimpleDomain.class.getName());

    private String data;


    @Column(name="data", allowNull=false, unique=true)
    public String getData() {
        return data;
    }

    public void setData(String data) {
        LOG.info("SETTING DATA: " + data);
        this.data = data;
    }

}

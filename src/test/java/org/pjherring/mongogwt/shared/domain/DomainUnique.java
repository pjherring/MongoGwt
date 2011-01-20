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
@Entity(name="uniqueEntity")
public class DomainUnique extends BaseDomainObject {

    private final static Logger LOG = Logger.getLogger(DomainUnique.class.getName());

    private String uniqueString;

    @Column(name="uniqueString", allowNull=false)
    public String getUniqueString() {
        return uniqueString;
    }

    public void setUniqueString(String uniqueString) {
        this.uniqueString = uniqueString;
    }

}

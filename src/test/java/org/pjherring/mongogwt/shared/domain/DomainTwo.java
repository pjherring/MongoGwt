/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.domain;


import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import org.pjherring.mongogwt.shared.BaseDomainObject;
import org.pjherring.mongogwt.shared.annotations.Column;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.annotations.Reference;
import org.pjherring.mongogwt.shared.annotations.enums.ReferenceType;

/**
 *
 * @author pjherring
 */
@Entity(doPersist=true, name="domainTwo")
public class DomainTwo extends BaseDomainObject {

    private final static Logger LOG = Logger.getLogger(DomainTwo.class.getName());

    private String stringData;
    private Long longData;
    private Date dateData;
    private List<DomainOne> domainOneCollection;

    @Column(name="stringDataRegexp", regexp=".*\\d.*")
    public String getStringData() {
        return stringData;
    }

    public void setStringData(String stringData) {
        this.stringData = stringData;
    }

    @Column(name="long")
    public Long getLongData() {
        return longData;
    }

    public void setLongData(Long longData) {
        this.longData = longData;
    }

    @Column(name="date")
    public Date getDateData() {
        return dateData;
    }

    public void setDateData(Date dateData) {
        this.dateData = dateData;
    }

    @Reference(type=ReferenceType.ONE_TO_MANY, managedBy="domainTwoRef")
    public List<DomainOne> getDomainOneCollection() {
        return domainOneCollection;
    }

    public void setDomainOneCollection(List<DomainOne> domainOneCollection) {
        this.domainOneCollection = domainOneCollection;
    }

}

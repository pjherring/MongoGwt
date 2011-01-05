/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.domain;


import java.util.ArrayList;
import java.util.Arrays;
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
@Entity(doPersist=true, name="domainOne")
public class DomainOne extends BaseDomainObject {

    private final static Logger LOG = Logger.getLogger(DomainOne.class.getName());

    private String stringData;
    private Long longData;
    private Date dateData;
    private DomainTwo domainTwo;
    private List<Integer> integers = new ArrayList<Integer>();

    @Column(name="stringData", unique=true)
    public String getStringData() {
        return stringData;
    }

    public void setStringData(String stringData) {
        this.stringData = stringData;
    }

    @Column(name="longData")
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

    @Column(name="domainTwoRef")
    @Reference(type=ReferenceType.ONE_TO_ONE)
    public DomainTwo getDomainTwo() {
        return domainTwo;
    }

    public void setDomainTwo(DomainTwo domainTwo) {
        this.domainTwo = domainTwo;
    }

    @Column(name="integers", allowNull=true)
    public List<Integer> getIntegers() {
        return integers;
    }

    public void setIntegers(List<Integer> integers) {
        this.integers = integers;
    }

    public void addIntgers(Integer... integersToAdd) {
        this.integers.addAll(Arrays.asList(integersToAdd));
    }


}

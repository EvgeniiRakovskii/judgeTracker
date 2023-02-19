package com.rakovsky.judgeTracker.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cases")
public class CourtCase implements Comparable<CourtCase> {


    public CourtCase() {
    }

    public CourtCase(String customName, String url, String caseNumber) {
        this.customName = customName;
        this.url = url;
        this.caseNumber = caseNumber;
    }

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;

    private String customName;

    private String url;

    private String caseNumber;

    private Integer numberOfColumn;

    private String motionOfCase;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getUrlForCase() {
        return url;
    }

    public void setUrlForCase(String urlForCase) {
        this.url = urlForCase;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public Integer getNumberOfColumn() {
        return numberOfColumn;
    }

    public void setNumberOfColumn(Integer numberOfColumn) {
        this.numberOfColumn = numberOfColumn;
    }

    public String getMotionOfCase() {
        return motionOfCase;
    }

    public void setMotionOfCase(String motionOfCase) {
        this.motionOfCase = motionOfCase;
    }

    @Override
    public String toString() {
        return "CourtCase{" +
                "id=" + id +
                ", customName='" + customName + '\'' +
                ", url='" + url + '\'' +
                ", caseNumber='" + caseNumber + '\'' +
                '}';
    }

    @Override
    public int compareTo(CourtCase o) {
        return Integer.compare(getId(), o.getId());

    }
}

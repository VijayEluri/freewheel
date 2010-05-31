package org.freewheelschedule.freewheel.common.model;

import javax.persistence.*;

@Entity
@Table(name="MACHINE")
public class Machine {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Long uid;
    @Column
    String name;
    @Column
    Long port;
    @Column
    String licenceKey;

    public String getLicenceKey() {
        return licenceKey;
    }

    public void setLicenceKey(String licenceKey) {
        this.licenceKey = licenceKey;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPort() {
        return port;
    }

    public void setPort(Long port) {
        this.port = port;
    }
}
package com.aiyolo.entity;

import javax.persistence.Entity;
import java.io.Serializable;

@Entity
public class DeviceCategory extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer category = 0;
    private Integer type = 0;
    private String code = "";

    protected DeviceCategory() {}

    public DeviceCategory(Integer category, Integer type, String code) {
        this.category = category;
        this.type = type;
        this.code = code;
    }

    @Override
    public String toString() {
        return String.format(
                "DeviceCategory[id=%d, category=%d, type=%d, code='%s']",
                id, category, type, code);
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}

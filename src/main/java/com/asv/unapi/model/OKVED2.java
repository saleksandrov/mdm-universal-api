package com.asv.unapi.model;

import com.asv.unapi.service.annotation.MdmField;
import com.asv.unapi.service.model.Item;

/**
 * @author alexandrov
 * @since 16.06.2016
 */
//@MdmTable("OKVED2")
public class OKVED2 extends Item {

    @MdmField(code = "Name")
    private String name;

    @MdmField(code = "PositionCode", updatable = true)
    private String positionCode;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPositionCode() {
        return positionCode;
    }

    public void setPositionCode(String positionCode) {
        this.positionCode = positionCode;
    }

    @Override
    public String toString() {
        return "OKVED2{" +
                "name='" + name + '\'' +
                ",  positionCode='" + positionCode + '\'' +
                '}';
    }
}

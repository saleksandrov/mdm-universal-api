package com.asv.unapi.model;

import com.asv.unapi.service.annotation.MdmField;
import com.asv.unapi.service.model.Item;

/**
 * @author Sergey Aleksandrov
 * @since 31.03.2016
 */
public class OKEI extends Item {

    @MdmField(code = "Name")
    private String name;

    @MdmField(code = "CodeNational")
    private String okeiCode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOkeiCode() {
        return okeiCode;
    }

    public void setOkeiCode(String okeiCode) {
        this.okeiCode = okeiCode;
    }
}

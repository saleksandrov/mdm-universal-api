package com.asv.unapi.model;

import com.asv.unapi.service.annotation.MdmField;
import com.asv.unapi.service.annotation.MdmTable;
import com.asv.unapi.service.model.Item;

import java.util.Date;

/**
 * @author Sergey Aleksandrov
 * @since 29.03.2016
 */
@MdmTable("WorksAndServices")
public class Service extends Item {

    @MdmField(code = "ShortName")
    private String name;

    @MdmField(code = "GID", updatable = false)
    private String id;

    @MdmField(code = "Role", updatable = false)
    private String role;

    @MdmField(code = "CreateDate", updatable = false)
    private Date createDate;

    @MdmField(code = "MeasureUnits", tableName = "MeasureUnits", type = Type.LOOKUP)
    private UOM uom;

    @MdmField(code = "OKVED2", tableName = "OKVED2", type = Type.LOOKUP)
    private OKVED2 okved2;

    @MdmField(code = "WorkAndServicesClassifier", tableName = "WorkAndServicesClassifier", type = Type.LOOKUP, updatable = false)
    private SrvClassifier classifier;

    public UOM getUom() {
        return uom;
    }

    public void setUom(UOM uom) {
        this.uom = uom;
    }

    public OKVED2 getOkved2() {
        return okved2;
    }

    public void setOkved2(OKVED2 okved2) {
        this.okved2 = okved2;
    }

    public SrvClassifier getClassifier() {
        return classifier;
    }

    public void setClassifier(SrvClassifier classifier) {
        this.classifier = classifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGID() {
        return id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Date getCreateDate() {
        return createDate;
    }
}

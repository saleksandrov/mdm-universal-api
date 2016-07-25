package com.asv.example.model;

import com.asv.unapi.service.annotation.MdmField;
import com.asv.unapi.service.annotation.MdmParent;
import com.asv.unapi.service.annotation.MdmTable;
import com.asv.unapi.service.model.Item;

/**
 * @author alexandrov
 * @since 25.07.2016
 */
@MdmTable("TNVED")
public class TNVED extends Item {

    @MdmField(code = "TNVEDCode")
    public String code;

    @MdmField(code = "TNVEDName")
    public String name;

    @MdmParent(keyCode = "TNVEDCode")
    public String parentId;



}

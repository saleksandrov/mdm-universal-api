package com.asv.example.model;

import com.asv.unapi.service.annotation.MdmField;
import com.asv.unapi.service.model.Item;

import java.util.Date;

/**
 * @author alexandrov
 * @since 28.06.2016
 */
public class RequestHistory extends Item {

    @MdmField(code = "Comment")
    public String comment;

    @MdmField(code = "CreateDate")
    public Date createDate;

    /*
    @MdmField(code = "RequestStatus", tableName = "RequestsStatuses", type = Type.LOOKUP, updatable = false)
    public RequestStatus rs;
    */

    @Override
    public String toString() {
        return "RequestHistory{" +
                "comment='" + comment + '\'' +
                ", createDate=" + createDate +
                '}';
    }
}

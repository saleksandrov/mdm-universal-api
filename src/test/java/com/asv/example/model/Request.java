package com.asv.example.model;

import com.asv.unapi.service.annotation.MdmField;
import com.asv.unapi.service.annotation.MdmTable;
import com.asv.unapi.service.model.Item;

import java.util.Collection;

/**
 * @author alexandrov
 * @since 25.06.2016
 */
@MdmTable("Requests")
public class Request extends Item {

    @MdmField(code = "GID")
    public String recordGid;

    @MdmField(code = "RequestHistory", tableName = "RequestHistory", type = Type.TUPLE, implClass = RequestHistory.class, updatable = false)
    public Collection<RequestHistory> requestHistory;

    @Override
    public String toString() {
        return "Request{" +
                "recordGid='" + recordGid + '\'' +
                ", requestHistory=" + requestHistory +
                '}';
    }
}

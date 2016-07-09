package com.asv.unapi.model;

import com.asv.unapi.service.annotation.MdmField;
import com.asv.unapi.service.model.Item;

/**
 * @author alexandrov
 * @since 28.06.2016
 */
public class RequestStatus extends Item {

    @MdmField(code = "Code")
    public String code;

    @Override
    public String toString() {
        return "RequestStatus{" +
                "code='" + code + '\'' +
                '}';
    }
}

package com.asv.unapi.service.model;

import com.sap.mdm.data.Record;

/**
 * @author alexandrov
 * @since 24.03.2016
 */
public class Item {

    private int id;
    private transient Record originalRecord;

    public enum Type {
        LOOKUP,
        TUPLE,
        MAIN,
        SIMPLE
    }

    public int getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    void setOriginalRecord(Record originalRecord) {
        this.originalRecord = originalRecord;
    }

    public Record getOriginalRecord() {
        return originalRecord;
    }

}

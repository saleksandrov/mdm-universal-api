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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        return id == item.id;

    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Item {" + "id=" + id + '}';
    }

}

package com.asv.example.model;

import com.asv.unapi.service.annotation.MdmField;
import com.asv.unapi.service.model.Item;

/**
 * @author Sergey Aleksandrov
 * @since 29.03.2016
 */
public class UOM extends Item {

    @MdmField(code = "SortPriority")
    public Integer sortPriority;

    @MdmField(code = "Name")
    public String name;

    @MdmField(code = "Symbol")
    public String symbol;


    @MdmField(code = "CodeOKEI", tableName = "OKEI", type = Type.LOOKUP)
    public OKEI okei;

    public UOM() {
    }

    public UOM(String symbol) {
        this.symbol = symbol;
    }

    public Integer getSortPriority() {
        return sortPriority;
    }

    public void setSortPriority(Integer sortPriority) {
        this.sortPriority = sortPriority;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public OKEI getOkei() {
        return okei;
    }

    public void setOkei(OKEI okei) {
        this.okei = okei;
    }

    @Override
    public String toString() {
        return "UOM{" +
                "id=" + sortPriority +
                ", name='" + name + '\'' +
                ", okei=" + okei +
                ", symbol=" + symbol +
                '}';
    }
}

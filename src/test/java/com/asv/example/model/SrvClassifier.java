package com.asv.example.model;

import com.asv.unapi.service.annotation.MdmField;
import com.asv.unapi.service.model.Item;

/**
 * @author alexandrov
 * @since 16.06.2016
 */
public class SrvClassifier extends Item {

    @MdmField(code = "ClassCode")
    private String classCode;

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    @Override
    public String toString() {
        return "SrvClassifier{" +
                "classCode='" + classCode + '\'' +
                '}';
    }
}

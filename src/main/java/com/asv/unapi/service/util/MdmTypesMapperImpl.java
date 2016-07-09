package com.asv.unapi.service.util;

import com.asv.unapi.dao.MdmTypesUtil;
import com.sap.mdm.valuetypes.*;

import java.util.Calendar;
import java.util.Date;

/**
 * Base implementation of types mapping from mdm to java type and from java type to mdm
 * <p>
 * Created by manager on 31.03.2016.
 */
public class MdmTypesMapperImpl implements MdmTypesMapper {

    @Override
    public Object mapMdmToBeanType(MdmValue value) {
        Object convertedValue;
        switch (value.getType()) {
            case MdmValue.Type.INTEGER:
                IntegerValue integerValue = MdmTypesUtil.castToType(value, IntegerValue.class);
                convertedValue = integerValue.getInt();
                break;
            case MdmValue.Type.BOOLEAN:
                convertedValue = MdmTypesUtil.getFieldBooleanValue(value);
                break;
            case MdmValue.Type.FLOAT:
                FloatValue floatValue = MdmTypesUtil.castToType(value, FloatValue.class);
                convertedValue = floatValue.getFloat();
                break;
            case MdmValue.Type.DOUBLE:
                DoubleValue doubleValue = MdmTypesUtil.castToType(value, DoubleValue.class);
                convertedValue = doubleValue.getDouble();
                break;
            case MdmValue.Type.DATE_TIME:
                convertedValue = MdmTypesUtil.getDateTimeValue(value);
                break;

            default:
                convertedValue = value.toString();
        }
        return convertedValue;
    }

    @Override
    public MdmValue mapBeanTypeToMdm(Object value) {
        if (value instanceof String) {
            return new StringValue(String.valueOf(value));
        } else if (value instanceof Integer) {
            return new IntegerValue((Integer) value);
        } else if (value instanceof Boolean) {
            return new BooleanValue((Boolean) value);
        } else if (value instanceof Boolean) {
            return new BooleanValue((Boolean) value);
        } else if (value instanceof Float) {
            return new FloatValue((Float) value);
        } else if (value instanceof Double) {
            return new DoubleValue((Double) value);
        } else if (value instanceof Date) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime((Date) value);
            return new DateTimeValue(calendar);
        }
        return null;

    }
}

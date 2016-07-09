package com.asv.unapi.dao;

import com.asv.config.Constants;
import com.sap.mdm.data.MdmValueTypeException;
import com.sap.mdm.data.MultilingualString;
import com.sap.mdm.data.Record;
import com.sap.mdm.data.RegionalString;
import com.sap.mdm.ids.FieldId;
import com.sap.mdm.net.ConnectionException;
import com.sap.mdm.schema.FieldProperties;
import com.sap.mdm.schema.RepositorySchema;
import com.sap.mdm.valuetypes.*;
import com.asv.tx.SchemaHolder;
import com.asv.util.DateUtils;

import java.util.Date;

/**
 * Util class for mdm types manipulation
 *
 * @author alexandrov
 * @since 27.06.2016
 */
public class MdmTypesUtil {

    public static String getTableName(Record record) {
        return record.getMetadata().getTable().getCode();
    }

    public static Boolean getFieldBooleanValue(Record record, String fieldName) {
        final MdmValue mdmValue = getFieldMdmValue(record, fieldName);
        return getFieldBooleanValue(mdmValue);
    }

    public static Boolean getFieldBooleanValue(MdmValue mdmValue) {
        if (mdmValue instanceof BooleanValue) {
            return ((BooleanValue) mdmValue).getBoolean();
        }
        return null;
    }


    public static MdmValue getFieldMdmValue(Record record, String fieldName) {
        MdmValue value;
        try {
            value = record.getFieldValue(record.getMetadata().getFieldId(fieldName));
        } catch (IllegalArgumentException e) {
            return null;
        }
        return value;
    }

    @Deprecated
    public static Date getFieldDateTimeValue(Record record, FieldId fieldId, boolean useGmtTimeZone) {
        return getDateTimeValue(record.getFieldValue(fieldId), useGmtTimeZone);
    }

    @Deprecated
    public static Date getFieldDateTimeValue(Record record, String fieldName, boolean useGmtTimeZone) {
        return getDateTimeValue(getFieldMdmValue(record, fieldName), useGmtTimeZone);
    }

    public static Date getFieldDateTimeValue(Record record, String fieldName) {
        return getDateTimeValue(getFieldMdmValue(record, fieldName));
    }

    public static Date getFieldDateTimeValue(Record record, String dateFieldName, String timeFieldName, boolean useGmtTimeZone) {
        Date date = getDateTimeValue(getFieldMdmValue(record, dateFieldName), useGmtTimeZone);
        Date time = getDateTimeValue(getFieldMdmValue(record, timeFieldName), useGmtTimeZone);
        if (date != null && time != null) {
            return DateUtils.getDate(date, time);
        } else {
            return null;
        }
    }

    public static Date getDateTimeValue(MdmValue mdmValue) {
        if (mdmValue instanceof DateTimeValue) {
            return ((DateTimeValue) mdmValue).getDate();
        }
        return null;
    }

    @Deprecated
    public static Date getDateTimeValue(MdmValue mdmValue, boolean useGmtTimeZone) {
        if (mdmValue instanceof DateTimeValue) {
            return ((DateTimeValue) mdmValue).getDate();
        }
        return null;
    }

    public static Integer getFieldIntegerValue(Record record, String fieldName) {
        String tableName = getSchema().getTable(record.getTable()).getCode();
        MdmValue mdmValue = record.getFieldValue(getFieldId(tableName, fieldName));
        IntegerValue integerValue = castToType(mdmValue, IntegerValue.class);
        return integerValue.getInt();
    }

    public static Float getFieldFloatValue(Record record, String fieldName) {
        String tableName = getSchema().getTable(record.getTable()).getCode();
        MdmValue mdmValue = record.getFieldValue(getFieldId(tableName, fieldName));

        FloatValue floatValue = castToType(mdmValue, FloatValue.class);
        return floatValue.getFloat();
    }

    public static Double getFieldDoubleValue(Record record, String fieldName) {
        String tableName = getSchema().getTable(record.getTable()).getCode();
        MdmValue mdmValue = record.getFieldValue(getFieldId(tableName, fieldName));

        DoubleValue doubleValue = castToType(mdmValue, DoubleValue.class);
        return doubleValue.getDouble();
    }

    @SuppressWarnings("unchecked")
    public static <T extends MdmValue> T castToType(MdmValue mdmValue, Class<T> typeClass) {
        if (mdmValue.isNull()) {
            return null;
        } else if (typeClass.isInstance(mdmValue)) {
            return (T) mdmValue;
        } else {
            throw new RuntimeException(String.format("Cannot cast mdmValue=%s to type=%s", mdmValue, typeClass));
        }
    }

    public static void setTupleMdmValue(Record record, String tupleName, String fieldName, MdmValue value) {
        try {
            TupleValue tupleValue = getTupleValue(record, tupleName);
            FieldId fieldId = tupleValue.getMetadata().getFieldId(fieldName);

            tupleValue.setFieldValue(fieldId, value);

            String tableName = getSchema().getTable(record.getTable()).getCode();
            record.setFieldValue(getFieldId(tableName, tupleName), tupleValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setFieldIntegerValue(Record record, String fieldName, Integer value) {
        String tableName = getSchema().getTable(record.getTable()).getCode();
        try {
            record.setFieldValue(getFieldId(tableName, fieldName), new IntegerValue(value.intValue()));
        } catch (MdmValueTypeException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setFieldStringValue(Record record, String fieldName, String value) {
        String tableName = getSchema().getTable(record.getTable()).getCode();
        try {
            record.setFieldValue(getFieldId(tableName, fieldName), new StringValue(value));
        } catch (MdmValueTypeException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setFieldBooleanValue(Record record, String fieldName, Boolean value) {
        String tableName = getSchema().getTable(record.getTable()).getCode();
        try {
            record.setFieldValue(getFieldId(tableName, fieldName), new BooleanValue(value));
        } catch (MdmValueTypeException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    public static TupleValue getTupleValue(Record record, String tupleFieldName) throws ConnectionException {
        return getTupleValueByIndex(record, tupleFieldName, 0);
    }

    public static TupleValue[] getTupleValues(Record record, String tupleFieldName) throws ConnectionException {
        FieldProperties fieldProperties = record.getMetadata().getField(tupleFieldName);
        MdmValue value = record.getFieldValue(fieldProperties.getId());
        if (value == null || value.isNull()) {
            return new TupleValue[0];
        } else if (value instanceof TupleValue) {
            return new TupleValue[]{(TupleValue) value};
        } else {
            MultiTupleValue tupleMultiTupleValue = (MultiTupleValue) value;

            TupleValue[] res = new TupleValue[tupleMultiTupleValue.getValuesCount()];
            for (int i = 0; i < tupleMultiTupleValue.getValuesCount(); i++) {
                res[i] = (TupleValue) tupleMultiTupleValue.getValue(i);
            }

            return res;
        }
    }

    public static TupleValue getTupleValueByIndex(Record record, String tupleFieldName, int index) throws ConnectionException {
        FieldProperties fieldProperties = record.getMetadata().getField(tupleFieldName);
        MdmValue value = record.getFieldValue(fieldProperties.getId());
        if (value == null || value.isNull()) {
            return null;
        } else if (value instanceof TupleValue) {
            return (TupleValue) value;
        } else {
            MultiTupleValue tupleMultiTupleValue = (MultiTupleValue) value;
            return tupleMultiTupleValue.getValuesCount() > 0 ? (TupleValue) tupleMultiTupleValue.getValue(index) : null;
        }
    }

    public static String getStringFromTuple(TupleValue tv, String fieldName) {
        FieldProperties fieldProperties = tv.getMetadata().getField(fieldName);
        MdmValue value = tv.getFieldValue(fieldProperties.getId());
        return value != null && !value.isNull() ? value.toString() : null;
    }

    public static MdmValue getValueFromTuple(TupleValue tv, String fieldName) {
        FieldProperties fieldProperties = tv.getMetadata().getField(fieldName);
        return tv.getFieldValue(fieldProperties.getId());
    }

    public static Date getDateFromTuple(TupleValue tv, String fieldName) {
        FieldProperties fieldProperties = tv.getMetadata().getField(fieldName);
        MdmValue value = tv.getFieldValue(fieldProperties.getId());
        if (value instanceof DateTimeValue) {
            return ((DateTimeValue) value).getDate();
        }
        return null;
    }

    public static Date getDateTimeValue(Record record, String dateFieldName, String timeFieldName) {
        Date date = null;
        MdmValue dateValue = getFieldMdmValue(record, dateFieldName);
        if (dateValue != null && !dateValue.isNull()) {
            date = ((DateTimeValue) dateValue).getDate();
        }

        Date time = null;
        MdmValue timeValue = getFieldMdmValue(record, timeFieldName);
        if (timeValue != null && !timeValue.isNull()) {
            time = ((DateTimeValue) timeValue).getDate();
        }

        if (date != null && time != null) {
            return DateUtils.getDate(date, time);
        } else {
            return null;
        }
    }

    public static Date getDateTimeFromTuple(TupleValue tv, String dateFieldName, String timeFieldName) {
        Date date = getDateFromTuple(tv, dateFieldName);
        Date time = getDateFromTuple(tv, timeFieldName);
        if (date != null && time != null) {
            return DateUtils.getDate(date, time);
        } else {
            return null;
        }
    }

    public static Integer getIntegerFromTuple(TupleValue tv, String fieldName) {
        FieldProperties fieldProperties = tv.getMetadata().getField(fieldName);
        MdmValue value = tv.getFieldValue(fieldProperties.getId());
        if (value instanceof IntegerValue) {
            return new Integer(((IntegerValue) value).getInt());
        }
        return null;
    }

    public static String getMultilingualStringValue(MultilingualString string, String regionCode) {
        RegionalString[] regionalStrings = string.getStrings();
        if (regionalStrings.length == 0) {
            return string.get();
        }

        RegionalString str = null;
        for (RegionalString regionalString : regionalStrings) {
            str = regionalString;
            if (str.getRegionCode().startsWith(regionCode)) {
                break;
            }
        }
        if (str == null) {
            return string.get();
        } else {
            return str.getString();
        }
    }

    public static String getMultilingualStringValue(MultilingualString string) {
        return getMultilingualStringValue(string, Constants.REGION_CODE);
    }

    static FieldId getFieldId(String tableName, String fieldName) {
        return getSchema().getField(tableName, fieldName).getId();
    }

    public static RepositorySchema getSchema() {
        return SchemaHolder.getInstance().getSchema();
    }
}

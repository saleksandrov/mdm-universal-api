package com.asv.unapi.service.model;

import com.asv.unapi.service.annotation.MdmField;
import com.asv.unapi.service.util.Assert;
import com.sap.mdm.data.Record;
import com.asv.unapi.service.annotation.MdmTable;

import java.lang.reflect.Field;
import java.util.*;

public class ItemProducer {

    private static final String UNKNOWN = "unknown";

    private final Map<String, Field> fieldsWithMdmCodes = new HashMap<String, Field>();
    private final Map<String, Field> fieldsWithFieldsNames = new HashMap<String, Field>();
    private final Map<String, String> lookupFieldsWithTablenames = new HashMap<String, String>();

    private final Item item;

    public static ItemProducer parse(Item item) {
        return new ItemProducer(item);
    }

    private ItemProducer(Item item) {
        Assert.notNull(item, "Cannot construct instance because child object is null");
        this.item = item;
        Field[] declaredFields = item.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(MdmField.class)) {
                String mdmCode = getMdmCodeFromField(field);
                fieldsWithMdmCodes.put(mdmCode, field);
                fieldsWithFieldsNames.put(field.getName(), field);
                MdmField annotation = field.getAnnotation(MdmField.class);
                if (annotation.type() == Item.Type.LOOKUP) {
                    lookupFieldsWithTablenames.put(annotation.tableName(), mdmCode);
                }

            }
        }
    }

    public static <T extends Item> String getMdmTableFromAnnotation(Class<T> sourceClass) {
        String table = null;
        if (sourceClass.isAnnotationPresent(MdmTable.class)) {
            MdmTable tableAnn = sourceClass.getAnnotation(MdmTable.class);
            table = tableAnn.value();
        }
        Assert.notNull(table, "Cannot read table name from annotation");
        return table;
    }

    public void setId(int id) {
        item.setId(id);
    }

    public void setOriginalRecord(Record originalRecord) {
        item.setOriginalRecord(originalRecord);
    }

    public void setValue(String mdmCode, Object value) {
        if (value == null) {
            return;
        }
        Field field = fieldsWithMdmCodes.get(mdmCode);
        if (field != null) {
            field.setAccessible(true);
            try {
                field.set(item, value);
            } catch (IllegalAccessException e) {
                // ignore
            }
        }
    }

    public Item getItem() {
        return item;
    }

    public void setItem(String mdmCode, Item childItem) {
        Field field = fieldsWithMdmCodes.get(mdmCode);
        if (field != null) {
            field.setAccessible(true);
            try {
                field.set(this.item, childItem);
            } catch (IllegalAccessException e) {
                // ignore
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void addItem(String mdmCode, Item childItem) {
        Field field = fieldsWithMdmCodes.get(mdmCode);
        if (field != null) {
            field.setAccessible(true);
            try {
                Object value = field.get(this.item);
                if (field.getType().equals(List.class) || field.getType().equals(Collection.class)) {
                    Collection<Item> valueList = (Collection<Item>) value;
                    if (value == null) {
                        valueList = new ArrayList<Item>();
                        field.set(this.item, valueList);
                    }
                    valueList.add(childItem);
                }
                // TODO add additional checking
            } catch (IllegalAccessException e) {
                // ignore
            }

        }

    }

    public String getMdmCodeByFieldName(String fieldName) {
        Field declaredField = fieldsWithFieldsNames.get(fieldName);
        if (declaredField != null) {
            declaredField.setAccessible(true);
            return getMdmCodeFromField(declaredField);
        } else {
            return UNKNOWN;
        }
    }

    public Class<? extends Item> getClassForMdmCode(String mdmCode) {
        Field declaredField = fieldsWithMdmCodes.get(mdmCode);
        if (declaredField != null) {
            declaredField.setAccessible(true);
            Class<? extends Item> classFromField = getClassFromField(declaredField);
            if (Collection.class.isAssignableFrom(classFromField)) {
                return getaClassFromAnnotation(declaredField);
            }
            return classFromField;

        } else {
            return null;
        }
    }

    private Class<? extends Item> getaClassFromAnnotation(Field declaredField) {
        MdmField annotation = declaredField.getAnnotation(MdmField.class);
        return annotation.implClass();
    }

    public String getTableName() {
        if (item.getClass().isAnnotationPresent(MdmTable.class)) {
            MdmTable table = item.getClass().getAnnotation(MdmTable.class);
            return table.value();
        }
        return null;
    }

    String getMdmCodeFromField(Field declaredField) {
        // we know(see constructor) that annotation MdmField is present
        MdmField annotation = declaredField.getAnnotation(MdmField.class);
        return annotation.code();

    }

    public Map<String, Field> getFieldsWithMdmCodes() {
        return fieldsWithMdmCodes;
    }

    /**
     * Returns not nullable values with keys for filed of simple type
     *
     * @return
     */
    public Map<String, Object> getSimpleFieldValues() {
        return getSimpleFieldValues(false);
    }

    public Map<String, Object> getSimpleFieldValues(boolean bypassUpdatableFlag) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Field> fieldsWithMdmCodesMap = getFieldsWithMdmCodes();
        for (String mdmCode : fieldsWithMdmCodesMap.keySet()) {
            Field field = fieldsWithMdmCodesMap.get(mdmCode);
            Assert.assertAnnotationPresent(field, MdmField.class);
            MdmField mdmField = field.getAnnotation(MdmField.class);
            if ((bypassUpdatableFlag || mdmField.updatable()) && mdmField.type() == Item.Type.SIMPLE) {
                Object value = getFieldValue(field);
                if (value == null)
                    continue;
                resultMap.put(mdmCode, value);
            }
        }
        return resultMap;
    }

    /**
     * Returns not nullable values with keys for lookup fields
     *
     * @return
     */
    public Map<String, Map<String, Object>> getLookupFieldValues() {
        return getLookupFieldValues(false);
    }

    public Map<String, Map<String, Object>> getLookupFieldValues(boolean bypassUpdatableFlag) {
        Map<String, Map<String, Object>> resultMap = new HashMap<String, Map<String, Object>>();
        Map<String, Field> fieldsWithMdmCodesMap = getFieldsWithMdmCodes();
        for (String mdmCode : fieldsWithMdmCodesMap.keySet()) {
            Field field = fieldsWithMdmCodesMap.get(mdmCode);
            Assert.assertAnnotationPresent(field, MdmField.class);
            MdmField mdmField = field.getAnnotation(MdmField.class);
            if ((bypassUpdatableFlag || mdmField.updatable()) && mdmField.type() == Item.Type.LOOKUP) {
                Object value = getFieldValue(field);
                if (value == null)
                    continue;
                Item lookupItem = (Item) value;
                ItemProducer lookupItemProducer = new ItemProducer(lookupItem);
                Map<String, Object> simpleValues = lookupItemProducer.getSimpleFieldValues();

                resultMap.put(mdmField.tableName(), simpleValues);
            }
        }
        return resultMap;
    }

    public String getMdmCodeOfLookupByTablename(String tableName) {
        return lookupFieldsWithTablenames.get(tableName);
    }

    public Object getFieldValue(Field field) {
        Object value;
        try {
            field.setAccessible(true);
            value = field.get(getItem());
        } catch (IllegalAccessException e) {
            return null;
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    Class<? extends Item> getClassFromField(Field declaredField) {
        try {
            return (Class<? extends Item>) declaredField.getType();
        } catch (Exception e) {
            return null;
        }
    }

}
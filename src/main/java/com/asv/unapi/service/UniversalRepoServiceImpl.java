package com.asv.unapi.service;

import com.asv.unapi.service.model.HierarchyItem;
import com.sap.mdm.commands.CommandException;
import com.sap.mdm.data.MdmValueTypeException;
import com.sap.mdm.data.Record;
import com.sap.mdm.data.RecordFactory;
import com.sap.mdm.data.RecordResultSet;
import com.sap.mdm.extension.data.RecordEx;
import com.sap.mdm.ids.FieldId;
import com.sap.mdm.ids.RecordId;
import com.sap.mdm.ids.TableId;
import com.sap.mdm.internal.protocol.commands.mdspublic.RequestEventsOnRepositoryCommand;
import com.sap.mdm.net.ConnectionException;
import com.sap.mdm.schema.FieldProperties;
import com.sap.mdm.schema.RepositorySchema;
import com.sap.mdm.schema.TableSchema;
import com.sap.mdm.valuetypes.LookupValue;
import com.sap.mdm.valuetypes.MdmValue;
import com.sap.mdm.valuetypes.MultiTupleValue;
import com.sap.mdm.valuetypes.TupleValue;
import com.asv.unapi.dao.BaseMdmDAO;
import com.asv.unapi.service.model.Item;
import com.asv.unapi.service.model.ItemProducer;
import com.asv.unapi.service.util.Assert;
import com.asv.unapi.service.util.MdmTypesMapper;

import java.util.*;

/**
 * Experimental API for Mdm To Bean mapping.
 *
 * @author alexandrov
 * @since 24.03.2016
 */
public class UniversalRepoServiceImpl<T extends Item> implements UniversalRepoService<T> {

    private final BaseMdmDAO baseDAO;
    private final MdmTypesMapper mapper;

    private final Class<T> typeParameterClass;
    private final LookupCache lookupCache = new LookupCache();


    private class LookupCache {

        private final Map<String, Record> lookupCacheMap = new HashMap<String, Record>();

        void put(String key, Record value) {
            lookupCacheMap.put(key, value);
        }

        Record get(String key) {
            return lookupCacheMap.get(key);
        }

        void clear() {
            lookupCacheMap.clear();
        }

    }


    public UniversalRepoServiceImpl(Class<T> typeParameterClass, BaseMdmDAO baseDAO, MdmTypesMapper mapper) {
        this.baseDAO = baseDAO;
        this.mapper = mapper;
        this.typeParameterClass = typeParameterClass;
    }

    public T recordToGenericPosition(RecordEx record, String[] supportingTables) {
        return recordToGenericPosition(record, supportingTables, 0, typeParameterClass);
    }

    @SuppressWarnings("unchecked")
    protected T recordToGenericPosition(RecordEx record, String[] supportingTables, int lookupLevel, Class itemClass) {
        Item item = getInstanceOfItem(itemClass);
        Assert.notNull(item, String.format("Cannot instantiate class by class %s. Object is NULL. Check does is implement the base interface.", itemClass.getName()));
        ItemProducer producer = ItemProducer.parse(item);
        producer.setId(record.getId().getIdValue());
        producer.setOriginalRecord(record);

        FieldId[] fields = record.getFields();
        for (FieldId field : fields) {
            RepositorySchema schema = baseDAO.getSchema();
            FieldProperties fp = schema.getField(record.getTable(), field);
            MdmValue value = record.getFieldValue(field);
            if (value.getType() != MdmValue.Type.NULL) {
                if ((fp.isTaxonomyLookup() || fp.isLookup()) && record.containsField(fp.getCode())) {
                    Record[] lookupRecords = record.findLookupRecords(field);
                    if (lookupRecords.length > 0) {
                        // Now we look through all the lookup record besides
                        // main tables or the tables which are not in supporting
                        // table list
                        for (Record currentLookupRecord : lookupRecords) {
                            RecordEx lookupRecord = (RecordEx) currentLookupRecord;
                            Class<? extends Item> classForMdmCode = producer.getClassForMdmCode(fp.getCode());
                            if (classForMdmCode != null) {
                                Item position = recordToGenericPosition(lookupRecord, supportingTables, lookupLevel + 1, classForMdmCode);
                                producer.setItem(fp.getCode(), position);
                            }
                        }
                    }
                } else if (fp.isTuple()) {
                    if (fp.isMultiValued()) {
                        MultiTupleValue mtv = (MultiTupleValue) record.getFieldValue(field);
                        MdmValue[] tupleValues = mtv.getValues();
                        for (MdmValue v : tupleValues) {
                            TupleValue tv = (TupleValue) v;
                            Class<? extends Item> classForMdmCode = producer.getClassForMdmCode(fp.getCode());
                            if (classForMdmCode != null) {
                                Item tupleItem = tupleToGenericPosition(tv, classForMdmCode);
                                producer.addItem(fp.getCode(), tupleItem);
                            }
                        }
                    } else {
                        TupleValue tv = (TupleValue) record.getFieldValue(field);
                        Class<? extends Item> classForMdmCode = producer.getClassForMdmCode(fp.getCode());
                        if (classForMdmCode != null) {
                            Item tupleItem = tupleToGenericPosition(tv, classForMdmCode);
                            producer.addItem(fp.getCode(), tupleItem);
                        }
                    }
                } else {
                    Object convertedValue = mapper.mapMdmToBeanType(value);
                    producer.setValue(fp.getCode(), convertedValue);
                }
            }
        }

        return (T) item;
    }


    protected Item tupleToGenericPosition(TupleValue tv, Class<? extends Item> itemClass) {
        Item tupleItem = getInstanceOfItem(itemClass);
        if (tupleItem == null) {
            return null;
        }
        ItemProducer tupleProducer = ItemProducer.parse(tupleItem);
        FieldId[] tupleFieldIds = tv.getFields();
        for (FieldId tupleField : tupleFieldIds) {
            FieldProperties tupleProperties = tv.getMetadata().getField(tupleField);
            MdmValue tupleValue = tv.getFieldValue(tupleField);
            if (!tupleValue.isNull()) {
                if (tupleProperties.isLookup()) {
                    Record[] tupleLookupRecords = tv.findLookupRecords(tupleField);
                    populateInnerLookup(tupleProducer, tupleProperties, tupleLookupRecords);
                } else if (tupleProperties.isTuple()) {
                    if (!tupleProperties.isMultiValued()) {
                        Item position = tupleToGenericPosition((TupleValue) tupleValue, tupleProducer.getClassForMdmCode(tupleProperties.getCode()));
                        tupleProducer.addItem(tupleProperties.getCode(), position);
                    } else {
                        MultiTupleValue mtv = (MultiTupleValue) tupleValue;
                        MdmValue[] tupleValues = mtv.getValues();
                        for (MdmValue v : tupleValues) {
                            TupleValue innerTv = (TupleValue) v;
                            Item innerTgp = tupleToGenericPosition(innerTv, tupleProducer.getClassForMdmCode(tupleProperties.getCode()));
                            tupleProducer.addItem(tupleProperties.getCode(), innerTgp);
                        }
                    }
                } else {
                    Object convertedValue = mapper.mapMdmToBeanType(tupleValue);
                    tupleProducer.setValue(tupleProperties.getCode(), convertedValue);
                }
            }
        }
        return tupleItem;
    }

    protected void populateInnerLookup(ItemProducer tupleProducer, FieldProperties tupleProperties, Record[] tupleLookupRecords) {

        if (tupleLookupRecords != null && tupleLookupRecords.length > 0) {
            Record tupleLookupRecord = tupleLookupRecords[0];
            Class<? extends Item> classForMdmCode = tupleProducer.getClassForMdmCode(tupleProperties.getCode());
            Item position = getInstanceOfItem(classForMdmCode);
            if (position == null) {
                return;
            }
            ItemProducer positionProducer = ItemProducer.parse(position);
            FieldId[] fieldIds = tupleLookupRecord.getFields();
            for (FieldId fieldId : fieldIds) {
                MdmValue fieldValue = tupleLookupRecord.getFieldValue(fieldId);
                TableSchema lookupMetadata = tupleLookupRecord.getMetadata();
                FieldProperties lookupFieldProperties = lookupMetadata.getField(fieldId);
                String code = lookupFieldProperties.getCode();
                if (lookupFieldProperties.isLookup()) {
                    Record[] records = tupleLookupRecord.findLookupRecords(lookupFieldProperties.getId());
                    Item lookupPosition = getInstanceOfItem(positionProducer.getClassForMdmCode(lookupFieldProperties.getCode()));
                    if (lookupPosition == null) {
                        continue;
                    }
                    if (records.length > 0) {
                        populateInnerLookup(ItemProducer.parse(lookupPosition), lookupFieldProperties, records);
                    }
                    positionProducer.setValue(code, lookupPosition);
                } else {
                    positionProducer.setValue(code, mapper.mapMdmToBeanType(fieldValue));
                }
            }
            tupleProducer.setValue(tupleProperties.getCode(), position);
        }
    }

    @Override
    public T getBeanByInternalId(int internalId, String[] supportingTables, int resultDefinitionMode) {
        String table = ItemProducer.getMdmTableFromAnnotation(typeParameterClass);

        try {
            TableId[] sTables = new TableId[supportingTables.length];
            int i = 0;
            for (String tbl : supportingTables) {
                sTables[i++] = baseDAO.getSchema().getTableId(tbl);
            }

            TableId tableId = baseDAO.getSchema().getTableId(table);
            RecordEx record = baseDAO.getRecordExById(tableId, new RecordId(internalId), sTables, resultDefinitionMode);
            return recordToGenericPosition(record, supportingTables);
        } catch (ConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T getBeanByInternalId(int internalId, String[] supportingTables) {
        return getBeanByInternalId(internalId, supportingTables, BaseMdmDAO.CREATE_RESULTDEF_MODE_ALLFIELDS);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getBeanByPK(String idFieldCode, Object idFieldValue, String[] supportingTables) {
        String table = ItemProducer.getMdmTableFromAnnotation(typeParameterClass);

        try {
            TableId[] sTables = new TableId[supportingTables.length];
            int i = 0;
            for (String tbl : supportingTables) {
                sTables[i++] = baseDAO.getSchema().getTableId(tbl);
            }

            Hashtable pkValueMap = new Hashtable();
            FieldProperties idFieldProperties = baseDAO.getSchema().getField(table, idFieldCode);
            pkValueMap.put(idFieldProperties, idFieldValue);
            RecordEx record = baseDAO.getCheckoutVersionOfRecordByIdValue(table, pkValueMap, sTables);
            return recordToGenericPosition(record, supportingTables);
        } catch (ConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(T t) {
        Assert.notNull(t.getOriginalRecord(), "Original record cannot be null");

        try {
            baseDAO.deleteRecord(t.getOriginalRecord());
        } catch (ConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void saveBean(T t) {
        Record originalRecord = t.getOriginalRecord();
        if (originalRecord == null) {
            // we have to get Record by internal id field
            try {
                String table = ItemProducer.getMdmTableFromAnnotation(typeParameterClass);
                TableId tableId = baseDAO.getSchema().getTableId(table);
                originalRecord = baseDAO.getRecordExById(tableId, new RecordId(t.getId()), null, BaseMdmDAO.CREATE_RESULTDEF_MODE_ALLFIELDS);

            } catch (ConnectionException e) {
                throw new RuntimeException(e);
            }
        }
        ItemProducer itemProducer = ItemProducer.parse(t);
        itemProducer.setOriginalRecord(originalRecord);

        Map<String, Object> simpleFieldValues = itemProducer.getSimpleFieldValues();
        String rootTableName = itemProducer.getTableName();

        // save simple fields
        for (String mdmCode : simpleFieldValues.keySet()) {
            FieldProperties fieldProperties = baseDAO.getField(rootTableName, mdmCode);
            Object value = simpleFieldValues.get(mdmCode);
            MdmValue mdmValue = mapper.mapBeanTypeToMdm(value);
            try {
                originalRecord.setFieldValue(fieldProperties.getId(), mdmValue);
            } catch (MdmValueTypeException e) {
                throw new RuntimeException(String.format("Cannot set value (%s) to field (%s) of the record ", mdmValue, mdmCode), e);
            }
        }
        foundAndSetLookupRecord(originalRecord, itemProducer, false);

        try {
            baseDAO.updateRecord(originalRecord);
        } catch (ConnectionException e) {
            throw new RuntimeException("Cannot save record", e);
        }

    }

    private void foundAndSetLookupRecord(Record originalRecord, ItemProducer itemProducer, boolean useCache) {
        String rootTableName = itemProducer.getTableName();
        Map<String, Map<String, Object>> lookupFieldValues = itemProducer.getLookupFieldValues();
        // save lookup fields
        // find new Lookup Record and add it to original Record
        for (String tableName : lookupFieldValues.keySet()) {
            Map<String, Object> simpleValuesOfLookup = lookupFieldValues.get(tableName);

            if (useCache) {
                // We know that lookups values are stored in LinkedHashMap
                // That meas it support ORDER and we can use it in constructing cache key
                String cacheKey = constructLookupCacheKey(tableName, simpleValuesOfLookup);
                Record lookupRecord = this.lookupCache.get(cacheKey);
                if (lookupRecord != null) {
                    FieldProperties fieldProperties = baseDAO.getField(rootTableName, itemProducer.getMdmCodeOfLookupByTablename(tableName));
                    try {
                        originalRecord.setFieldValue(fieldProperties.getId(), new LookupValue(lookupRecord.getId()));
                        continue;
                    } catch (MdmValueTypeException e) {
                        throw new RuntimeException(String.format("Cannot set Lookup record to record. Lookup table=%s", tableName), e);
                    }
                }
            }

            // perform search for lookup
            Hashtable data = new Hashtable();
            for (String mdmCodeOfLookup : simpleValuesOfLookup.keySet()) {
                FieldProperties fieldProperties = baseDAO.getField(tableName, mdmCodeOfLookup);
                Object value = simpleValuesOfLookup.get(mdmCodeOfLookup);
                if (value != null) data.put(fieldProperties, value);
            }
            try {
                if (data.size() == 0) {
                    continue;
                }
                RecordResultSet recordsByFieldValue = baseDAO.getRecordsByFieldValue(tableName, data, false);
                if (recordsByFieldValue.getRecords().length > 1) {
                    throw new RuntimeException(String.format("Found to many lookup records in table=%s by fields=%s", tableName, data));
                } else if (recordsByFieldValue.getRecords().length == 0) {
                    // not found lookup
                    continue;
                }
                Record lookupRecord = recordsByFieldValue.getRecords()[0];
                FieldProperties fieldProperties = baseDAO.getField(rootTableName, itemProducer.getMdmCodeOfLookupByTablename(tableName));
                originalRecord.setFieldValue(fieldProperties.getId(), new LookupValue(lookupRecord.getId()));
                if (useCache) {
                    String cacheKey = constructLookupCacheKey(tableName, simpleValuesOfLookup);
                    this.lookupCache.put(cacheKey, lookupRecord);
                }
            } catch (ConnectionException e) {
                throw new RuntimeException(String.format("Cannot find Lookup record in table=%s by fields=%s", tableName, data), e);
            } catch (MdmValueTypeException e) {
                throw new RuntimeException(String.format("Cannot set Lookup record to record. Lookup table=%s", tableName), e);
            }
        }
    }

    private String constructLookupCacheKey(String tableName, Map<String, Object> simpleValuesOfLookup) {
        return tableName + simpleValuesOfLookup;
    }

    public int create(List<T> items) {
        int created = 0;
        try {
            for (T item : items) {
                create(item, true);
                created++;
            }
        } finally {
            // empty cache

        }
        return created;
    }

    public void create(T t) {
        create(t, false);
    }


    public void create(T t, boolean useCache) {
        ItemProducer itemProducer = ItemProducer.parse(t);
        String tableName = itemProducer.getTableName();
        Record emptyRecord = RecordFactory.createEmptyRecord(baseDAO.getSchema().getTableId(tableName));
        Map<String, Object> simpleFieldValues = itemProducer.getSimpleFieldValues(true);

        for (String key : simpleFieldValues.keySet()) {
            FieldProperties field = baseDAO.getField(tableName, key);
            Object value = simpleFieldValues.get(key);
            try {
                emptyRecord.setFieldValue(field.getId(), mapper.mapBeanTypeToMdm(value));
            } catch (MdmValueTypeException e) {
                throw new RuntimeException(String.format("Cannot map bean value to mdm type value = %s", value), e);
            }

        }
        // lookup fields
        foundAndSetLookupRecord(emptyRecord, itemProducer, useCache);
        try {
            Record parentRecord = null;
            // два сопсоба указанаия парента через поле parendId и через иерархию
            // второй способ будет работать быстрее при массовой вставке, т.к. он предполагает что Record у рута уже получен
            if (itemProducer.hasParent()) {
                String keyCode = itemProducer.getParentKeyCode();
                Object value = itemProducer.getParentValue();

                if (value != null) {
                    RecordResultSet recordResultSet = baseDAO.getRecordsByFieldValue(tableName, keyCode, value);
                    if (recordResultSet.getRecords().length > 1) {
                        throw new RuntimeException(String.format("Found to many records in table=%s by keyCode=%s value=%s", tableName, keyCode, value));
                    }
                    parentRecord = recordResultSet.getRecord(0);
                }

            } else if (t instanceof HierarchyItem) {
                HierarchyItem hi = (HierarchyItem) t;
                Item parent = hi.getParent();
                if (parent != null) {
                    parentRecord = parent.getOriginalRecord();
                    if (parentRecord == null) {
                        throw new RuntimeException("Original record of parent cannot be null");
                    }
                }

            }

            Record record = baseDAO.createRecord(emptyRecord, parentRecord != null ? parentRecord.getId() : null);
            itemProducer.setOriginalRecord(record);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create record", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> searchBeansByFilter(Map<String, Object> searchData, String[] supportingTables) {
        String table = ItemProducer.getMdmTableFromAnnotation(typeParameterClass);

        // needed for old code
        Hashtable data = new Hashtable();
        for (String key : searchData.keySet()) {
            FieldProperties field = baseDAO.getField(table, key);
            data.put(field, searchData.get(key));
        }

        return searchRecordsByParameters(supportingTables, data);
    }

    private List<T> searchRecordsByParameters(String[] supportingTables, Map<FieldProperties, Object> data) {
        TableId[] sTables = new TableId[supportingTables.length];
        int i = 0;
        for (String tbl : supportingTables) {
            sTables[i++] = baseDAO.getSchema().getTableId(tbl);
        }

        List<T> result = new ArrayList<T>();
        try {
            RecordResultSet recordsByFieldValue = baseDAO.getRecordsByMapValue(data, sTables, false);
            int count = recordsByFieldValue.getCount();
            while (--count >= 0) {
                RecordEx record = (RecordEx) recordsByFieldValue.getRecord(count);
                T t = recordToGenericPosition(record, supportingTables);
                result.add(t);
            }
        } catch (ConnectionException e) {
            throw new RuntimeException(String.format("Cannot perform search by data=%s", data), e);
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> searchBeanByPattern(T pattern, String[] supportingTables) {
        ItemProducer itemProducer = ItemProducer.parse(pattern);
        String rootTableName = itemProducer.getTableName();

        Map<String, Object> simpleFieldValues = itemProducer.getSimpleFieldValues(true);
        Map<FieldProperties, Object> searchData = new HashMap<FieldProperties, Object>();
        for (String key : simpleFieldValues.keySet()) {
            FieldProperties field = baseDAO.getField(rootTableName, key);
            searchData.put(field, simpleFieldValues.get(key));
        }

        Map<String, Map<String, Object>> lookupFieldValues = itemProducer.getLookupFieldValues(true);

        // add all lookup values to search params
        for (String tableName : lookupFieldValues.keySet()) {
            Map<String, Object> simpleValuesOfLookup = lookupFieldValues.get(tableName);

            // perform search for lookup
            Hashtable lookupData = new Hashtable();
            for (String mdmCodeOfLookup : simpleValuesOfLookup.keySet()) {
                FieldProperties fieldProperties = baseDAO.getField(tableName, mdmCodeOfLookup);
                lookupData.put(fieldProperties, simpleValuesOfLookup.get(mdmCodeOfLookup));
            }
            try {
                RecordResultSet recordsByFieldValue = baseDAO.getRecordsByFieldValue(tableName, lookupData, false);
                if (recordsByFieldValue.getRecords().length > 1) {
                    throw new RuntimeException(String.format("Found to many lookup records in table=%s by fields=%s", tableName, lookupData));
                }
                Record lookupRecord = recordsByFieldValue.getRecords()[0];
                FieldProperties fieldProperties = baseDAO.getField(rootTableName, itemProducer.getMdmCodeOfLookupByTablename(tableName));
                searchData.put(fieldProperties, new LookupValue(lookupRecord.getId()));
            } catch (ConnectionException e) {
                throw new RuntimeException(String.format("Cannot find Lookup record in table=%s by fields=%s", tableName, lookupData), e);
            }
        }

        return searchRecordsByParameters(supportingTables, searchData);
    }

    private Item getInstanceOfItem(Class<? extends Item> type) {
        try {
            return type.newInstance();
        } catch (Exception e) {
            return null;
        }
    }

}

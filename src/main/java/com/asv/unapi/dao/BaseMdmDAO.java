package com.asv.unapi.dao;

import com.asv.config.Constants;
import com.asv.tx.SchemaHolder;
import com.asv.tx.UserSessionManager;
import com.sap.mdm.commands.CommandException;
import com.sap.mdm.data.*;
import com.sap.mdm.data.commands.*;
import com.sap.mdm.data.permanentid.commands.GetPermanentIdFromRecordIdCommand;
import com.sap.mdm.extension.data.RecordEx;
import com.sap.mdm.extension.data.commands.RetrieveLimitedRecordsExCommand;
import com.sap.mdm.extension.data.commands.RetrieveRecordsByIdExCommand;
import com.sap.mdm.ids.AttributeId;
import com.sap.mdm.ids.FieldId;
import com.sap.mdm.ids.RecordId;
import com.sap.mdm.ids.TableId;
import com.sap.mdm.net.ConnectionException;
import com.sap.mdm.schema.*;
import com.sap.mdm.schema.fields.LookupFieldProperties;
import com.sap.mdm.search.*;
import com.sap.mdm.session.SessionException;
import com.sap.mdm.session.UserSessionContext;
import com.sap.mdm.valuetypes.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This is old MDM Java API base logic class. It has been written since old MDM 5.5 thus some methods must be
 * refactored.
 *
 * @author Sergey Aleksandrov
 * @since 10.03.2009
 */
public class BaseMdmDAO {

    public static final int NOT_SPECIFIED = -1;
    public static final int CREATE_RESULTDEF_MODE_ALLFIELDS = 0;
    public static final int CREATE_RESULTDEF_MODE_DISPLAYFIELDS = 2;

    public BaseMdmDAO() {
    }

    public RepositorySchema getSchema() {
        return SchemaHolder.getInstance().getSchema();
    }

    public UserSessionContext getUserSessionContext() {
        return UserSessionManager.getUserSessionContext();
    }

    public String getTableName(TableId tableId) {
        return getSchema().getTable(tableId).getCode();
    }

    public TableId getTableId(String tableName) {
        return getSchema().getTable(tableName).getId();
    }

    public boolean isTableOfMainType(TableId tableId) {
        return getSchema().getTable(tableId).getType() == TableProperties.MAIN;
    }

    public String getTableName(Record record) {
        return getSchema().getTable(record.getTable()).getCode();
    }

    public FieldProperties getField(String tableName, String fieldName) {
        return getSchema().getField(tableName, fieldName);
    }

    private FieldId[] getAllSearchFields(TableId tableId) {
        FieldProperties[] fields = getSchema().getFields(tableId);
        FieldId[] fieldIds = new FieldId[fields.length];
        for (int i = 0; i < fields.length; i++) {
            fieldIds[i] = fields[i].getId();
        }
        return fieldIds;
    }

    public ResultDefinition createResultDefinition(TableId tableId, int mode) {
        ResultDefinition resDefinition = new ResultDefinition(tableId);

        FieldId[] fieldIds;
        switch (mode) {
            case CREATE_RESULTDEF_MODE_DISPLAYFIELDS:
                fieldIds = getSchema().getTable(tableId).getDisplayFieldIds();
                break;
            default:
                fieldIds = getAllSearchFields(tableId);
                break;
        }

        resDefinition.setSelectFields(fieldIds);
        resDefinition.setLoadAttributes(true);

        return resDefinition;
    }

    public RecordId checkoutRecord(Record record) throws ConnectionException, CommandException {
        if (getSchema().getTable(record.getTable()).getType() != TableProperties.MAIN) {
            throw new RuntimeException("This method can be used only for main tables");
        }

        CheckoutRecordsCommand command = new CheckoutRecordsCommand(getUserSessionContext());
        command.setTableId(record.getTable());
        command.addRecordId(record.getId());
        command.setExclusive(false);
        command.execute();

        return command.getCheckOutRecordIds()[0];
    }

    public void joinCheckoutRecord(Record record) throws ConnectionException {
        if (!isTableOfMainType(record.getTable())) {
            throw new RuntimeException("This method can be used only for main tables");
        }

        JoinCheckoutRecordsCommand command = new JoinCheckoutRecordsCommand(getUserSessionContext());
        command.setTableId(record.getTable());
        command.addRecordId(record.getId());
        try {
            command.execute();
        } catch (CommandException e) {
            throw new RuntimeException(e);
        }
    }

    public RecordId checkinRecord(Record record) throws ConnectionException {
        if (getSchema().getTable(record.getTable()).getType() != TableProperties.MAIN) {
            throw new RuntimeException("This method can be used only for main tables");
        }

        CheckinRecordsCommand command = new CheckinRecordsCommand(getUserSessionContext());
        command.setTableId(record.getTable());
        command.addRecordId(record.getId());

        try {
            command.execute();
            return command.getSucceededRecords()[0];
        } catch (CommandException e) {
            throw new RuntimeException(e);
        }
    }

    public void rollbackRecord(Record record) throws ConnectionException {
        if (getSchema().getTable(record.getTable()).getType() != TableProperties.MAIN) {
            throw new RuntimeException("This method can be used only for main tables");
        }

        RollbackRecordsCommand command = new RollbackRecordsCommand(getUserSessionContext());
        command.setTableId(record.getTable());
        command.addRecordId(record.getId());

        try {
            command.execute();
        } catch (CommandException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteRecord(Record record) throws ConnectionException {
        DeleteRecordsCommand command = new DeleteRecordsCommand(getUserSessionContext());
        command.setTable(record.getTable());
        command.addRecord(record.getId());

        try {
            command.execute();
        } catch (CommandException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Optimized performace for multiply records
     *
     * @param table
     * @param recordIds
     * @throws ConnectionException
     */
    public void deleteRecords(TableId table, RecordId[] recordIds) throws ConnectionException {
        if (recordIds == null || recordIds.length == 0)
            return;
        DeleteRecordsCommand command = new DeleteRecordsCommand(getUserSessionContext());
        command.setTable(table);
        command.addRecords(recordIds);

        try {
            command.execute();
        } catch (CommandException e) {
            throw new RuntimeException(e);
        }
    }

    public Record getLookupRecordFromTuple(TupleValue tv, String fieldName) throws ConnectionException {
        // Find out lookup-table name from record
        FieldProperties fieldProperties = tv.getMetadata().getField(fieldName);
        LookupFieldProperties lookupFieldProperties = (LookupFieldProperties) fieldProperties;
        String lookupTableName = getTableName(lookupFieldProperties.getLookupTableId());

        if (!fieldProperties.isLookup()) {
            throw new RuntimeException("Field '" + fieldName + "' of tuple '"
                    + tv.getMetadata().getTupleDefinition().getCode() + "' is not lookup!");
        }

        MdmValue value = tv.getFieldValue(fieldProperties.getId());

        LookupValue lookupValue = null;
        if (!value.equals(new NullValue())) {
            lookupValue = (LookupValue) value;
        }

        if (lookupValue == null) {
            return null;
        }
        int internalID = lookupValue.getLookupId().getIdValue();
        return getRecordByInternalId(lookupTableName, internalID);
    }

    public MdmValue getLookupRecordValue(Record record, String recordFieldName, String lookupTableField)
            throws ConnectionException {
        Record lookupRecord = getLookupRecord(record, recordFieldName);
        if (lookupRecord == null)
            return new NullValue();
        return MdmTypesUtil.getFieldMdmValue(lookupRecord, lookupTableField);
    }

    public Record getLookupRecord(Record record, String recordFieldName) throws ConnectionException {
        // Find out lookup-table name from record
        FieldProperties fieldProperties = record.getMetadata().getField(recordFieldName);
        LookupFieldProperties lookupFieldProperties = (LookupFieldProperties) fieldProperties;
        String lookupTableName = getTableName(lookupFieldProperties.getLookupTableId());

        if (!fieldProperties.isLookup()) {
            throw new RuntimeException("Field '" + recordFieldName + "' of table '"
                    + record.getMetadata().getTable().getCode() + "' is not lookup!");
        }

        MdmValue value = MdmTypesUtil.getFieldMdmValue(record, recordFieldName);

        LookupValue lookupValue = null;
        if (!value.equals(new NullValue())) {
            lookupValue = (LookupValue) value;
        }

        if (lookupValue == null) {
            return null;
        }
        int internalID = lookupValue.getLookupId().getIdValue();
        return getRecordByInternalId(lookupTableName, internalID);
    }

    public String[] getFieldDisplayValue(Record record, String fieldName) throws ConnectionException {
        MdmValue mdmValue = MdmTypesUtil.getFieldMdmValue(record, fieldName);
        FieldProperties fieldProperties = record.getMetadata().getField(fieldName);
        String strValue = "";
        if (mdmValue.getType() != MdmValue.Type.NULL) {
            if (fieldProperties.isMultiValued()) {
                MultiTupleValue tupleMultiTupleValue = (MultiTupleValue) mdmValue;
                String[] res = new String[tupleMultiTupleValue.getValuesCount()];
                for (int i = 0; i < tupleMultiTupleValue.getValuesCount(); i++) {
                    TupleValue tupleValue = (TupleValue) tupleMultiTupleValue.getValue(i);
                    res[i] = tupleValue.getDisplayValue();
                }

                return res;
            }

            if (fieldProperties.isTuple()) {
                TupleValue tupleValue = (TupleValue) mdmValue;
                strValue = tupleValue.getDisplayValue();
            }

            if (fieldProperties.isLookup()) {
                Record lookupRecord = getLookupRecord(record, fieldName);
                strValue = lookupRecord.getDisplayValue();
            } else if (mdmValue.getType() == MdmValue.Type.DATE_TIME) {
                // We have to convert retrieved from MDM date to local zone time
                DateFormat dateFormat = new SimpleDateFormat();
                dateFormat.setTimeZone(TimeZone.getDefault());
                strValue = dateFormat.format(((DateTimeValue) mdmValue).getDate());
            } else {
                strValue = mdmValue.toString();
            }

        }
        return new String[]{strValue};
    }

    public String getFieldStringValue(Record record, String fieldName) throws ConnectionException {
        // String tableName = getSchema().getTable(record.getTable()).getCode();
        MdmValue mdmValue = MdmTypesUtil.getFieldMdmValue(record, fieldName);
        FieldProperties fieldProperties = record.getMetadata().getField(fieldName);
        String strValue = "";
        if (mdmValue.getType() != MdmValue.Type.NULL) {
            if (fieldProperties.isLookup()) {
                Record lookupRecord = getLookupRecord(record, fieldName);
                strValue = getRecordAsString(lookupRecord);
            } else if (mdmValue.getType() == MdmValue.Type.DATE_TIME) {
                // We have to convert retrieved from MDM date to local zone time
                DateFormat dateFormat = new SimpleDateFormat();
                dateFormat.setTimeZone(TimeZone.getDefault());
                strValue = dateFormat.format(((DateTimeValue) mdmValue).getDate());
            } else {
                strValue = mdmValue.toString();
            }
        }
        return strValue;
    }


    public String getRecordAsString(Record record) {
        return getRecordAsString(record, true);
    }

    public String getRecordAsString(Record record, boolean onlyDisplayFields) {
        StringBuffer buffer = new StringBuffer();

        FieldProperties[] fields;
        if (onlyDisplayFields) {
            RepositorySchema schema = getSchema();

            FieldId[] fieldIds = schema.getTable(record.getTable()).getDisplayFieldIds();
            fields = new FieldProperties[fieldIds.length];
            for (int i = 0; i < fieldIds.length; i++) {
                fields[i] = schema.getField(record.getTable(), fieldIds[i]);
            }
        } else {
            fields = getSchema().getFields(record.getTable());
        }

        buffer.append("[");
        for (int i = 0; i < fields.length; i++) {
            try {
                FieldProperties field = fields[i];
                buffer.append("{");
                buffer.append(field.getCode()).append(" = ");
                buffer.append(getFieldStringValue(record, field.getCode()));
                buffer.append("}");
                if (i != (fields.length - 1)) {
                    buffer.append(Constants.Xml.XML_DF_DELIMITER);
                }
            } catch (ConnectionException e) {
                throw new RuntimeException(e);
            }
        }
        buffer.append("]");
        return buffer.toString();
    }

    public int getRecordInternalId(String tableName, int id) throws ConnectionException {
        return getRecordInternalId(tableName, Constants.Mdm.REPOSITORY_IDENTY_FIELD_NAME, id);
    }

    public int getRecordInternalId(String tableName, String idFieldName, int recordId) throws ConnectionException {
        Record record = getRecordById(tableName, idFieldName, recordId);
        return record.getId().getIdValue();
    }

    public Record getRecordByInternalId(String tableName, int recordId) throws ConnectionException {
        return getRecordById(tableName, new RecordId(recordId));
    }

    public Record getRecordById(String tableName, RecordId recordId) throws ConnectionException {
        return getRecordById(getTableId(tableName), recordId);
    }

    protected Record getRecordById(TableId tableId, RecordId recordId) throws ConnectionException {
        RecordResultSet resultSet = getRecordsByIds(tableId, new RecordId[]{recordId});
        if (resultSet != null && resultSet.getCount() > 0) {
            return resultSet.getRecord(0);
        } else {
            return null;
        }
    }

    protected RecordResultSet getRecordsByIds(TableId tableId, RecordId[] ids) throws ConnectionException {
        RetrieveRecordsByIdCommand command = new RetrieveRecordsByIdCommand(getUserSessionContext());
        command.setIds(ids);
        ResultDefinition resultDefinition = createResultDefinition(tableId, CREATE_RESULTDEF_MODE_ALLFIELDS);
        command.setResultDefinition(resultDefinition);

        try {
            command.execute();
        } catch (CommandException e) {
            return null;
            // throw new RuntimeException(e);
        }

        RecordResultSet resultSet = command.getRecords();
        if (resultSet.getCount() == ids.length) {
            return resultSet;
        } else {
            return null;
        }
    }

    public Integer getRecordId(Record record, String idFieldName) {
        MdmValue value = MdmTypesUtil.getFieldMdmValue(record, idFieldName);
        if (value.isNull()) {
            return null;
        } else {
            return new Integer(((IntegerValue) value).getInt());
        }
    }

    public Record getRecordById(String tableName, String idFieldName, int recordId) throws ConnectionException {
        return getRecordById(getTableId(tableName), idFieldName, recordId);
    }

    protected Record getRecordById(TableId tableId, String idFieldName, int recordId) throws ConnectionException {
        RecordResultSet resultSet = getRecordsByIds(tableId, idFieldName, new int[]{recordId});
        // Its work around т.к. метод setCheckoutTypeSearch не работает
        // необходимо пропустить записи в чекауте и вернуть ORIGINAL
        if (resultSet != null && resultSet.getCount() > 1) {
            Record[] records = resultSet.getRecords();
            for (Record record : records) {
                if (record.getCheckoutStatus() == Record.CheckoutStatus.ORIGINAL) {
                    return record;
                }
            }
        }

        if (resultSet != null && resultSet.getCount() > 0) {
            return resultSet.getRecord(0);
        } else {
            return null;
        }
    }

    protected RecordResultSet getRecordsByIds(TableId tableId, String idFieldName, int[] recordIds)
            throws ConnectionException {
        return getRecordsByIds(getSchema().getTable(tableId).getCode(), idFieldName, recordIds);
    }

    protected RecordResultSet getRecordsByIds(String tableName, String idFieldName, int[] recordIds)
            throws ConnectionException {
        Hashtable<FieldProperties, Object> data = new Hashtable<FieldProperties, Object>();
        for (int recordId : recordIds) {
            FieldProperties field = getSchema().getField(tableName, idFieldName);
            Object value = recordId;
            data.put(field, value);
        }

        return getRecordsByFieldValue(data, true);
    }

    public RecordResultSet getRecordsByFieldValue(String tableName, String fieldName, Object fieldValue)
            throws ConnectionException {
        Hashtable<FieldProperties, Object> data = new Hashtable<FieldProperties, Object>();
        FieldProperties field = getSchema().getField(tableName, fieldName);
        data.put(field, fieldValue);
        return getRecordsByFieldValue(data, true);
    }

    /**
     * Returns all the records from the specified table
     */
    public RecordResultSet getRecords(String tableName) {
        TableId tableId = getSchema().getTable(tableName).getId();

        RetrieveLimitedRecordsCommand command;
        try {
            command = new RetrieveLimitedRecordsCommand(getUserSessionContext());
            command.setSearch(new Search(tableId));
            command.setResultDefinition(createResultDefinition(tableId, CREATE_RESULTDEF_MODE_ALLFIELDS));
            command.execute();
        } catch (SessionException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            return null;
        } catch (CommandException e) {
            throw new RuntimeException(e);
        } catch (ConnectionException e) {
            throw new RuntimeException(e);
        }
        RecordResultSet resultSet = command.getRecords();
        if (resultSet.getCount() < 1) {
            return null;
        }
        return resultSet;
    }

    public RecordResultSet getRecordsByFieldValue(Hashtable data, boolean isOrSearch) throws ConnectionException {
        Search search = null;

        for (Object o : data.keySet()) {
            FieldProperties field = (FieldProperties) o;
            Object value = data.get(field);

            SearchConstraint constraint;
            switch (field.getType()) {
                case FieldProperties.AUTOID_FIELD:
                case FieldProperties.INTEGER_FIELD:
                    constraint = new NumericSearchConstraint((Integer) value, NumericSearchConstraint.EQUALS);
                    break;
                case FieldProperties.BOOLEAN_FIELD:
                    constraint = new BooleanSearchConstraint((Boolean) value);
                    break;
                case FieldProperties.CURRENCY_FIELD:
                case FieldProperties.REAL4_FIELD:
                    constraint = new NumericSearchConstraint((Double) value, NumericSearchConstraint.EQUALS);
                    break;
                case FieldProperties.LARGE_TEXT_FIELD:
                case FieldProperties.FIXED_WIDTH_TEXT_FIELD:
                case FieldProperties.TEXT_FIELD:
                case FieldProperties.NAME_FIELD:
                case FieldProperties.NORMALIZED_TEXT_FIELD:
                    constraint = new TextSearchConstraint(value + "", TextSearchConstraint.EQUALS);
                    break;
                case FieldProperties.HIER_LOOKUP_FIELD:
                case FieldProperties.FLAT_LOOKUP_FIELD:
                    int recordId = (Integer) value;
                    LookupFieldProperties lookupField = (LookupFieldProperties) field;
                    int recordInternalId = getRecordInternalId(getTableName(lookupField.getLookupTableId()), recordId); // Converting
                    MdmValue mdmValue = new LookupValue(new RecordId(recordInternalId));
                    constraint = new PickListSearchConstraint(new MdmValue[]{mdmValue});
                    break;
                case FieldProperties.TAXONOMY_LOOKUP_FIELD:
                    String taxonomyTableName = getTableName(((LookupFieldProperties) field).getLookupTableId());
                    RecordResultSet resultSet = getRecordsByFieldValue(taxonomyTableName,
                            Constants.Mdm.TAXONOMY_LOOKUP_ID_FIELD_NAME, String.valueOf(value));

                    Record taxonomyRecord;
                    if (resultSet == null || resultSet.getCount() <= 0) {
                        throw new RuntimeException("Unable to find a record in taxonomy table (" + taxonomyTableName
                                + ") by specified ID field value: " + value);
                    } else if (resultSet.getCount() > 1) {
                        throw new RuntimeException("There are more than one record in taxonomy taxonomy table ("
                                + taxonomyTableName + ") was found by specified identy field value '" + value
                                + "' (is this field indeed ID?)");
                    } else {
                        taxonomyRecord = resultSet.getRecord(0);
                    }
                    mdmValue = new LookupValue(taxonomyRecord.getId());
                    constraint = new PickListSearchConstraint(new MdmValue[]{mdmValue});
                    break;
                default:
                    throw new RuntimeException("Unsupported type of field: " + field.getTypeName());
            }

            if (search == null) {
                search = new Search(field.getTableId());
            }
            search.addSearchItem(new FieldSearchDimension(field.getId()), constraint);
        }

        if (isOrSearch) {
            search.setComparisonOperator(Search.OR_OPERATOR);
        } else {
            search.setComparisonOperator(Search.AND_OPERATOR);
        }

        ResultDefinition resDefinition = createResultDefinition(search.getSearchTableId(),
                CREATE_RESULTDEF_MODE_ALLFIELDS);

        RetrieveLimitedRecordsExCommand command = new RetrieveLimitedRecordsExCommand(getUserSessionContext());
        // NEW we don't need ckeckout records
        search.setCheckoutSearchType(Search.CheckOutSearchType.ORIGINAL);

        command.setSearch(search);
        command.setResultDefinition(resDefinition);
        try {
            command.execute();
        } catch (IllegalArgumentException e) {
            return null;
        } catch (CommandException e) {
            throw new RuntimeException(e);
        }
        RecordResultSet resultSet = command.getRecords();
        if (resultSet.getCount() < 1) {
            return null;
        }

        return resultSet;
    }

    public RecordResultSet getRecordsByMapValue(Map<FieldProperties, Object> data, TableId[] supportingTables, boolean isOrSearch) throws ConnectionException {
        Search search = null;

        for (Object o : data.keySet()) {
            FieldProperties field = (FieldProperties) o;
            Object value = data.get(field);

            SearchConstraint constraint;
            switch (field.getType()) {
                case FieldProperties.AUTOID_FIELD:
                case FieldProperties.INTEGER_FIELD:
                    constraint = new NumericSearchConstraint((Integer) value, NumericSearchConstraint.EQUALS);
                    break;
                case FieldProperties.BOOLEAN_FIELD:
                    constraint = new BooleanSearchConstraint((Boolean) value);
                    break;
                case FieldProperties.CURRENCY_FIELD:
                case FieldProperties.REAL4_FIELD:
                    constraint = new NumericSearchConstraint((Double) value, NumericSearchConstraint.EQUALS);
                    break;
                case FieldProperties.LARGE_TEXT_FIELD:
                case FieldProperties.FIXED_WIDTH_TEXT_FIELD:
                case FieldProperties.TEXT_FIELD:
                case FieldProperties.NAME_FIELD:
                case FieldProperties.NORMALIZED_TEXT_FIELD:
                    constraint = new TextSearchConstraint(value + "", TextSearchConstraint.EQUALS);
                    break;
                case FieldProperties.HIER_LOOKUP_FIELD:
                case FieldProperties.FLAT_LOOKUP_FIELD:
                case FieldProperties.TAXONOMY_LOOKUP_FIELD:
                    MdmValue mdmValue = (MdmValue) value;
                    constraint = new PickListSearchConstraint(new MdmValue[]{mdmValue});
                    break;
                default:
                    throw new RuntimeException("Unsupported type of field: " + field.getTypeName());
            }

            if (search == null) {
                search = new Search(field.getTableId());
            }
            search.addSearchItem(new FieldSearchDimension(field.getId()), constraint);
        }

        if (isOrSearch) {
            search.setComparisonOperator(Search.OR_OPERATOR);
        } else {
            search.setComparisonOperator(Search.AND_OPERATOR);
        }

        ResultDefinition[] supportingResultDefinitions = null;
        if (supportingTables != null && supportingTables.length > 0) {
            supportingResultDefinitions = new ResultDefinition[supportingTables.length];
            for (int i = 0; i < supportingTables.length; i++) {
                ResultDefinition rd = createResultDefinition(supportingTables[i], CREATE_RESULTDEF_MODE_ALLFIELDS);
                supportingResultDefinitions[i] = rd;
            }
        }

        ResultDefinition resDefinition = createResultDefinition(search.getSearchTableId(), CREATE_RESULTDEF_MODE_ALLFIELDS);

        RetrieveLimitedRecordsExCommand command = new RetrieveLimitedRecordsExCommand(getUserSessionContext());
        // NEW we don't need ckeckout records
        search.setCheckoutSearchType(Search.CheckOutSearchType.ORIGINAL);

        command.setSearch(search);
        command.setResultDefinition(resDefinition);
        if (supportingResultDefinitions != null && supportingResultDefinitions.length > 0) {
            command.setSupportingResultDefinitions(supportingResultDefinitions);
        }

        try {
            command.execute();
        } catch (IllegalArgumentException e) {
            return null;
        } catch (CommandException e) {
            throw new RuntimeException(e);
        }
        RecordResultSet resultSet = command.getRecords();
        if (resultSet.getCount() < 1) {
            return null;
        }

        return resultSet;
    }


    public RecordResultSet getRecordsByFieldValue(String tableName, Hashtable data, boolean isOrSearch)
            throws ConnectionException {
        Search search = new Search(getTableId(tableName));

        mapSearchFieldToMdmTypes(data, search);
        if (isOrSearch) {
            search.setComparisonOperator(Search.OR_OPERATOR);
        } else {
            search.setComparisonOperator(Search.AND_OPERATOR);
        }

        ResultDefinition resDefinition = createResultDefinition(search.getSearchTableId(), CREATE_RESULTDEF_MODE_ALLFIELDS);

        RetrieveLimitedRecordsExCommand command = new RetrieveLimitedRecordsExCommand(getUserSessionContext());
        command.setSearch(search);
        command.setResultDefinition(resDefinition);

        try {
            command.execute();
        } catch (IllegalArgumentException e) {
            return null;
        } catch (CommandException e) {
            throw new RuntimeException(e);
        }
        RecordResultSet resultSet = command.getRecords();
        if (resultSet.getCount() < 1) {
            return null;
        }

        return resultSet;
    }

    public RecordEx getCheckoutVersionOfRecordByIdValue(String tableName, Hashtable data, TableId[] supportingTables)
            throws ConnectionException {
        Search search = new Search(getTableId(tableName));
        mapSearchFieldToMdmTypes(data, search);
        search.setComparisonOperator(Search.AND_OPERATOR);
        search.setCheckoutSearchType(Search.CheckOutSearchType.ALL);


        ResultDefinition resDefinition = createResultDefinition(search.getSearchTableId(), CREATE_RESULTDEF_MODE_ALLFIELDS);

        ResultDefinition[] supportingResultDefinitions = null;
        if (supportingTables != null && supportingTables.length > 0) {
            supportingResultDefinitions = new ResultDefinition[supportingTables.length];
            for (int i = 0; i < supportingTables.length; i++) {
                ResultDefinition rd = createResultDefinition(supportingTables[i], CREATE_RESULTDEF_MODE_ALLFIELDS);
                supportingResultDefinitions[i] = rd;
            }
        }

        RetrieveLimitedRecordsExCommand command = new RetrieveLimitedRecordsExCommand(getUserSessionContext());
        command.setSearch(search);
        command.setResultDefinition(resDefinition);
        if (supportingResultDefinitions != null && supportingResultDefinitions.length > 0) {
            command.setSupportingResultDefinitions(supportingResultDefinitions);
        }
        try {
            command.execute();
        } catch (IllegalArgumentException e) {
            return null;
        } catch (CommandException e) {
            throw new RuntimeException(e);
        }
        RecordResultSet resultSet = command.getRecords();
        if (resultSet.getCount() < 1) {
            return null;
        }
        for (int i = 0; i < resultSet.getRecords().length; i++) {
            RecordEx record = (RecordEx) resultSet.getRecords()[i];
            boolean canWorkWithRecordInCheckouStatus = record.getCheckoutStatus() == Record.CheckoutStatus.MEMBER || record.getCheckoutStatus() == Record.CheckoutStatus.OWNER;
            if (record.getCheckoutStatus() == Record.CheckoutStatus.NONE || canWorkWithRecordInCheckouStatus) {
                return record;
            } else if (record.getCheckoutStatus() == Record.CheckoutStatus.NON_MEMBER) {
                joinCheckoutRecord(record);
                return record;
            }
        }

        throw new RuntimeException(String.format("Cannot find record by values %s in table=%s", data, tableName));
    }

    private void mapSearchFieldToMdmTypes(Hashtable data, Search search) throws ConnectionException {
        for (Object o : data.keySet()) {
            FieldProperties field = (FieldProperties) o;
            Object value = data.get(field);

            FieldProperties tupleFieldProperties = null;
            if (value instanceof TupleContainer) {
                TupleContainer tupleContainer = (TupleContainer) value;
                value = tupleContainer.getValue();
                tupleFieldProperties = tupleContainer.getTupleProperties();
            }

            SearchConstraint constraint;
            switch (field.getType()) {
                case FieldProperties.AUTOID_FIELD:
                case FieldProperties.INTEGER_FIELD:
                    constraint = new NumericSearchConstraint((Integer) value, NumericSearchConstraint.EQUALS);
                    break;
                case FieldProperties.BOOLEAN_FIELD:
                    constraint = new BooleanSearchConstraint((Boolean) value);
                    break;
                case FieldProperties.CURRENCY_FIELD:
                case FieldProperties.REAL4_FIELD:
                    constraint = new NumericSearchConstraint((Double) value, NumericSearchConstraint.EQUALS);
                    break;
                case FieldProperties.LARGE_TEXT_FIELD:
                case FieldProperties.FIXED_WIDTH_TEXT_FIELD:
                case FieldProperties.TEXT_FIELD:
                case FieldProperties.NAME_FIELD:
                case FieldProperties.NORMALIZED_TEXT_FIELD:
                    constraint = new TextSearchConstraint(value + "", TextSearchConstraint.EQUALS);
                    break;
                case FieldProperties.HIER_LOOKUP_FIELD:
                case FieldProperties.FLAT_LOOKUP_FIELD:
                case FieldProperties.TAXONOMY_LOOKUP_FIELD:
                    MdmValue mdmValue;
                    if (value instanceof LookupValue) {
                        mdmValue = (MdmValue) value;
                        constraint = new PickListSearchConstraint(new MdmValue[]{mdmValue});
                    } else if (value instanceof MdmValue[]) {
                        constraint = new PickListSearchConstraint((MdmValue[]) value);
                    } else if (value instanceof String) {
                        String taxonomyTableName = getTableName(((LookupFieldProperties) field).getLookupTableId());
                        RecordResultSet resultSet = getRecordsByFieldValue(taxonomyTableName,
                                Constants.Mdm.TAXONOMY_LOOKUP_ID_FIELD_NAME, (String) value);

                        Record taxonomyRecord;
                        if (resultSet == null || resultSet.getCount() <= 0) {
                            throw new RuntimeException("Unable to find a record in table (" + taxonomyTableName
                                    + ") by specified ID field value: " + value);
                        } else if (resultSet.getCount() > 1) {
                            throw new RuntimeException("There are more than one record in table (" + taxonomyTableName
                                    + ") was found by specified identy field value '" + value
                                    + "' (is this field indeed ID?)");
                        } else {
                            taxonomyRecord = resultSet.getRecord(0);
                        }
                        mdmValue = new LookupValue(taxonomyRecord.getId());
                        constraint = new PickListSearchConstraint(new MdmValue[]{mdmValue});
                    } else {
                        throw new RuntimeException("Error in getRecordsByFieldValue");
                    }
                    break;
                default:
                    throw new RuntimeException("Unsupported type of field: " + field.getTypeName());
            }
            FieldSearchDimension fieldSearchDimension;

            if (tupleFieldProperties != null) {
                fieldSearchDimension = new FieldSearchDimension(new FieldId[]{tupleFieldProperties.getId(),
                        field.getId()});
            } else {
                fieldSearchDimension = new FieldSearchDimension(field.getId());
            }
            search.addSearchItem(fieldSearchDimension, constraint);
        }
    }

    public AttributeProperties[] getRecordFieldAttributes(Record record, String fieldName) throws ConnectionException {
        LookupFieldProperties field = (LookupFieldProperties) record.getMetadata().getField(fieldName);
        MdmValue fieldValue = MdmTypesUtil.getFieldMdmValue(record, fieldName);

        SearchConstraint constraint = new PickListSearchConstraint(new MdmValue[]{fieldValue});
        FieldSearchDimension searchDimension = new FieldSearchDimension(field.getId());

        Search search = new Search(field.getTableId());
        search.addSearchItem(searchDimension, constraint);

        RetrieveLimitedAttributesCommand command2 = new RetrieveLimitedAttributesCommand(getUserSessionContext());
        command2.setSearch(search);
        command2.setTaxonomyFieldId(field.getId());

        try {
            command2.execute();
        } catch (CommandException e) {
            throw new RuntimeException(e);
        }

        return command2.getAttributes();
    }

    public Record createRecord(Record record, RecordId parentRecordId) throws CommandException, ConnectionException {
        CreateRecordCommand command = new CreateRecordCommand(getUserSessionContext());
        command.setRecord(record);

        if (parentRecordId != null) {
            command.setParentRecordId(parentRecordId);
        }
        command.execute();
        return command.getRecord();
    }

    public void updateRecord(Record record) throws ConnectionException {
        ModifyRecordCommand command = new ModifyRecordCommand(getUserSessionContext());

        command.setCommitOnWarning(true);
        command.setModifyAnyway(true);
        command.setRecord(record);

        try {
            command.execute();
        } catch (CommandException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] getFields(TupleValue r) {
        FieldId[] fields = r.getFields();
        TupleDefinitionSchema schema = r.getMetadata();

        ArrayList<String> res = new ArrayList<String>();
        for (int i = 0; i < fields.length; i++) {
            MdmValue value = r.getFieldValue(fields[i]);
            String code = schema == null ? "??" : schema.getFieldCode(fields[i]);
            if (value instanceof NullValue)
                continue;
            res.add(code + "(" + value.getClass().getSimpleName() + ") = " + toString(value));
        }
        String[] res2 = res.toArray(new String[0]);
        Arrays.sort(res2);
        return res2;
    }

    private String toString(MdmValue value) {
        if (value instanceof TupleValue) {
            String[] res = getFields((TupleValue) value);
            StringBuilder sb = new StringBuilder();
            sb.append("{ ");
            for (int j = 0; j < res.length; j++) {
                if (j > 0)
                    sb.append(",");
                sb.append(res[j]);
            }
            sb.append(" }");
            return sb.toString();
        }
        if (value instanceof MultiTupleValue) {
            MdmValue[] mv = ((MultiTupleValue) value).getValues();
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < mv.length; j++) {
                if (j > 0)
                    sb.append(",");
                sb.append(toString(mv[j]));
            }
            return sb.toString();
        }
        return value.toString();
    }

    public Record merge(Record srcRecord, Record newRecord) throws ConnectionException {
        MergeRecordsCommand mergeRecordsCommand = new MergeRecordsCommand(getUserSessionContext());
        mergeRecordsCommand.setTableId(getTableId(MdmTypesUtil.getTableName(srcRecord)));
        // mergeRecordsCommand.setMergeRecordValues(newRecord);
        // mergeRecordsCommand.setRecordIds(new RecordId[]{srcRecord.getId()});
        mergeRecordsCommand.setMergeRecordValues(srcRecord);
        mergeRecordsCommand.setRecordIds(new RecordId[]{newRecord.getId()});

        try {
            mergeRecordsCommand.execute();
        } catch (CommandException e) {
            throw new RuntimeException(e);
        }
        return mergeRecordsCommand.getMergeRecordValues();
    }

    public RecordId duplicateRecord(Record record) throws ConnectionException {
        DuplicateRecordCommand command = new DuplicateRecordCommand(getUserSessionContext());
        command.setTableId(getTableId(MdmTypesUtil.getTableName(record)));
        command.setRecordId(record.getId());

        try {
            command.execute();
        } catch (CommandException e) {
            throw new RuntimeException(e);
        }
        return command.getNewRecordId();
    }

    public Record[] executeSelect(String tableName, Search search, FieldId[] fieldIds, SortDefinition sd)
            throws ConnectionException, CommandException {
        return executeSelect(tableName, search, fieldIds, sd, NOT_SPECIFIED, NOT_SPECIFIED);
    }

    public Record[] executeSelect(String tableName, Search search, FieldId[] fieldIds, SortDefinition sd, int pageSize,
                                  int pageIndex) throws ConnectionException, CommandException {
        RetrieveLimitedRecordsCommand command = new RetrieveLimitedRecordsCommand(getUserSessionContext());
        if (search != null)
            command.setSearch(search);

        TableId tableId = getTableId(tableName);

        ResultDefinition resultDefinition = new ResultDefinition(tableId);
        if (fieldIds != null) {
            resultDefinition.setSelectFields(fieldIds);
        } else {
            resultDefinition.setSelectFields(getAllSearchFields(tableId));
        }
        if (sd != null) {
            resultDefinition.setFieldSortingOrder(sd);
        }

        command.setResultDefinition(resultDefinition);

        if (pageSize != NOT_SPECIFIED)
            command.setPageSize(pageSize);
        if (pageIndex != NOT_SPECIFIED)
            command.setPageIndex(pageIndex);

        command.execute();

        RecordResultSet resultSet = command.getRecords();
        if (resultSet == null) {
            throw new NullPointerException("[MDM] Result Set is NULL");
        }
        return resultSet.getRecords();
    }

    public HierNode executeSelectHier(String tableName, Search search, FieldId[] fieldIds, SortDefinition sd,
                                      RecordId rootNode) throws ConnectionException, CommandException {
        RetrieveLimitedHierTreeCommand command = new RetrieveLimitedHierTreeCommand(getUserSessionContext());
        if (search != null)
            command.setSearch(search);
        ResultDefinition resultDefinition = new ResultDefinition(getTableId(tableName));
        if (fieldIds != null)
            resultDefinition.setSelectFields(fieldIds);
        if (sd != null)
            resultDefinition.setFieldSortingOrder(sd);
        if (rootNode != null)
            command.setRootNode(rootNode);
        command.setResultDefinition(resultDefinition);
        command.execute();
        HierNode resultSet = command.getTree();
        if (resultSet == null) {
            // throw new NullPointerException("[MDM] Result Set is NULL");
            return null;
        }
        return resultSet;
    }

    public RecordEx getRecordExById(TableId tableId, RecordId recordId, TableId[] supportingTables, int mode)
            throws ConnectionException {

        // create supporting result definitions
        ResultDefinition[] rds = null;
        if (supportingTables != null && supportingTables.length > 0) {
            rds = new ResultDefinition[supportingTables.length];
            for (int i = 0; i < supportingTables.length; i++) {
                ResultDefinition rd = createResultDefinition(supportingTables[i], mode);
                rds[i] = rd;
            }
        }

        RecordResultSet resultSet = getRecordsExByIds(tableId, new RecordId[]{recordId}, rds);
        if (resultSet != null && resultSet.getCount() > 0) {
            return (RecordEx) resultSet.getRecord(0);
        } else {
            return null;
        }
    }

    protected RecordResultSet getRecordsExByIds(TableId tableId, RecordId[] ids,
                                                ResultDefinition[] supportingResultDefinitions) throws ConnectionException {

        RetrieveRecordsByIdExCommand command = new RetrieveRecordsByIdExCommand(getUserSessionContext());
        command.setIds(ids);

        ResultDefinition resultDefinition = createResultDefinition(tableId, CREATE_RESULTDEF_MODE_ALLFIELDS);
        command.setResultDefinition(resultDefinition);

        if (supportingResultDefinitions != null && supportingResultDefinitions.length > 0)
            command.setSupportingResultDefinitions(supportingResultDefinitions);

        try {
            command.execute();
        } catch (CommandException e) {
            return null;
        }

        RecordResultSet resultSet = command.getRecords();
        if (resultSet.getCount() == ids.length) {
            return resultSet;
        } else {
            return null;
        }
    }

    public void copyRecord(Record sourceRecord, Record destRecord, String[] excludeFieldsCodes)
            throws ConnectionException {
        copyRecord(sourceRecord, destRecord, excludeFieldsCodes, true);
    }

    public void copyRecord(Record sourceRecord, Record destRecord, String[] excludeFieldsCodes, boolean updateRecord)
            throws ConnectionException {

        if (sourceRecord == null) {
            throw new NullPointerException("Source record does not exist");
        }

        if (destRecord.getCheckoutStatus() != Record.CheckoutStatus.NONE
                && destRecord.getCheckoutStatus() != Record.CheckoutStatus.UNDEFINED) {
            joinCheckoutRecord(destRecord);
        }

        final List<String> codesList;
        if (excludeFieldsCodes != null) {
            codesList = Arrays.asList(excludeFieldsCodes);
        } else {
            codesList = new LinkedList<String>();
        }

        final TableId sourceTableId = getTableId(getTableName(sourceRecord));

        // gets the source fields to be copied
        List<FieldProperties> sourceFieldList = new LinkedList<FieldProperties>();
        FieldId[] sourceFields = sourceRecord.getFields();
        for (FieldId fieldId : sourceFields) {
            FieldProperties fp = getSchema().getField(sourceTableId, fieldId);
            if (!codesList.contains(fp.getCode())) {
                sourceFieldList.add(fp);
            }
        }

        // copy processing
        try {
            for (FieldProperties sourceFP : sourceFieldList) {
                FieldProperties destFP = findField(destRecord, sourceFP);
                if (destFP != null && destFP.isEditable() && !destFP.isCalculated()) {
                    MdmValue sourceValue = sourceRecord.getFieldValue(sourceFP.getId());
                    if (sourceValue.isNull()) {
                        if (FieldProperties.NAME_FIELD == sourceFP.getType()
                                || FieldProperties.MEASUREMENT_FIELD == sourceFP.getType()
                                || FieldProperties.QUALIFIED_FLAT_LOOKUP_FIELD == sourceFP.getType())
                            continue;
                        destRecord.setFieldValue(destFP.getId(), NullValue.NULL);
                    } else {
                        // Taxonomy
                        if (sourceFP.isTaxonomyLookup()) {
                            destRecord.setFieldValue(destFP.getId(), sourceValue);
                            List<AttributeId> sourceAttrsList = Arrays.asList(sourceRecord.getAttributes(sourceFP
                                    .getId()));
                            for (AttributeId attributeId : sourceAttrsList) {
                                MdmValue attrValue = sourceRecord.getAttributeValue(sourceFP.getId(), attributeId);
                                destRecord.setAttributeValue(destFP.getId(), attributeId, attrValue);
                            }

                        } else if (sourceFP.isTuple()) {
                            if (sourceFP.isMultiValued()) {
                                // clear the values
                                MdmValue destValue = destRecord.getFieldValue(destFP.getId());
                                MultiTupleValue destMtv = null;
                                if (destValue.isNull()) {
                                    destMtv = new MultiTupleValue();
                                } else {
                                    destMtv = (MultiTupleValue) destValue;
                                    MdmValue[] dValues = destMtv.getValues();
                                    for (MdmValue dValue : dValues) {
                                        destMtv.removeTupleValue((TupleValue) dValue);
                                    }
                                }

                                // create copies of multi-tuple values
                                MdmValue[] sourceTupleValues = ((MultiTupleValue) sourceValue).getValues();

                                for (MdmValue v : sourceTupleValues) {
                                    TupleValue tv = (TupleValue) v;
                                    TupleValue destTv = createNewCopyOfTupleValue(tv);
                                    destMtv.addValue(destTv);
                                }

                                destRecord.setFieldValue(destFP.getId(), destMtv);
                            } else {
                                TupleValue sourceTv = (TupleValue) sourceValue;
                                TupleValue destTv = createNewCopyOfTupleValue(sourceTv);
                                destRecord.setFieldValue(destFP.getId(), destTv);
                            }
                        } else
                            destRecord.setFieldValue(destFP.getId(), sourceValue);
                    }
                }
            }

            if (updateRecord)
                updateRecord(destRecord);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (MdmValueTypeException e) {
            throw new RuntimeException(e);
        }
    }

    protected TupleValue createNewCopyOfTupleValue(TupleValue source) {

        TupleValue tv = MdmValueFactory.createTupleValue();

        FieldId[] tupleFieldIds = source.getFields();
        for (FieldId tupleFieldId : tupleFieldIds) {
            MdmValue tupleValue = source.getFieldValue(tupleFieldId);
            if (!tupleValue.isNull()) {
                FieldProperties tfp = source.getMetadata().getField(tupleFieldId);
                if (tfp.isTuple()) {
                    if (!tfp.isMultiValued()) {
                        TupleValue innerTupleValue = createNewCopyOfTupleValue((TupleValue) tupleValue);
                        tv.setFieldValue(tupleFieldId, innerTupleValue);
                    } else {
                        MultiTupleValue mtv = (MultiTupleValue) tupleValue;
                        MdmValue[] tupleValues = mtv.getValues();
                        MultiTupleValue destInnerMtv = new MultiTupleValue();
                        for (MdmValue v : tupleValues) {
                            TupleValue destInnerTupleValue = createNewCopyOfTupleValue((TupleValue) v);
                            destInnerMtv.newTupleValue(destInnerTupleValue);
                        }
                        tv.setFieldValue(tupleFieldId, destInnerMtv);
                    }
                } else if (tfp != null && tfp.isEditable() && !tfp.isCalculated()) {
                    tv.setFieldValue(tupleFieldId, source.getFieldValue(tupleFieldId));
                }
            } else {
                tv.setFieldValue(tupleFieldId, new NullValue());
            }
        }

        return tv;
    }

    protected FieldProperties findField(Record destRec, FieldProperties sourceFP) {
        final String destTableName = getSchema().getTable(destRec.getTable()).getCode();
        final FieldProperties[] destFPs = getSchema().getFields(destTableName);

        for (FieldProperties destFP : destFPs)
            if (destFP.getCode().equals(sourceFP.getCode()) && destFP.getType() == sourceFP.getType()
                    && destFP.getTypeName().equals(sourceFP.getTypeName()))
                return destFP;
        return null;
    }

    public Integer getRecordPermanentId(Record record) {
        GetPermanentIdFromRecordIdCommand command;
        try {
            command = new GetPermanentIdFromRecordIdCommand(getUserSessionContext());
        } catch (SessionException e) {
            throw new RuntimeException(e);
        } catch (ConnectionException e) {
            throw new RuntimeException(e);
        }
        command.setTableId(record.getTable());
        command.setRecordIds(new RecordId[]{record.getId()});

        try {
            command.execute();
        } catch (CommandException e) {
            throw new RuntimeException(e);
        }

        int[] permIds = command.getPermanentIds();
        if (permIds.length > 0) {
            return permIds[0];
        } else {
            return null;
        }
    }

    public static class TupleContainer {

        private FieldProperties tupleProperties;
        private Object value;

        public TupleContainer(FieldProperties tupleProperties, Object value) {
            this.tupleProperties = tupleProperties;
            this.value = value;
        }

        public FieldProperties getTupleProperties() {
            return tupleProperties;
        }

        public Object getValue() {
            return value;
        }
    }

}

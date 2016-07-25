package com.asv.unapi.service;

import com.asv.unapi.service.model.Item;

import java.util.List;
import java.util.Map;

/**
 * Base service for all bean to mdm manipulations
 *
 * @author Sergey Aleksandrov
 * @since 31.03.2016.
 */
public interface UniversalRepoService<T extends Item> {

    T getBeanByInternalId(int internalId, String[] supportingTables, int resultDefinitionMode);

    T getBeanByInternalId(int internalId, String[] supportingTables);

    /**
     * Search bean by primary key. Searches records in checkout and not checkout status.
     * If record is checked out then the method returns checked out version of record.
     * If not method returns original version of record.
     *
     * @param idFieldCode
     * @param idFieldValue
     * @param supportingTables
     * @return
     */
    T getBeanByPK(String idFieldCode, Object idFieldValue, String[] supportingTables);

    /**
     * Performs search by simple (not lookup) fields
     *
     * @param data             Mdmcodes and values to search
     * @param supportingTables Needed for constucting bean by record. Used for populating bean inner classes.
     * @return found mdm records mapped to bean
     */
    List<T> searchBeansByFilter(Map<String, Object> data, String[] supportingTables);

    /**
     * Performs search by bean patterns. Only simple fields and single lookup of first level are supported
     *
     * @param pattern
     * @param supportingTables
     * @return found mdm record mapped to bean
     */
    List<T> searchBeanByPattern(T pattern, String[] supportingTables);

    /**
     * Method stores only first level of fields of the type T. Inner type are not supported.
     * Currently it supports only simple type of fields
     * like ({@link Integer}, {@link String}, {@link Boolean} and so on) and types that maps to single MDM type Lookup.
     * Tuples currently don't supported.
     * Collections type are not supported
     *
     * @param t bean class to save
     */
    void saveBean(T t);

    /**
     * Create MDM Record based on bean.
     * Currently only simple values are supported
     *
     * @param t bean to create
     */
    void create(T t);

    /**
     * Delete MDM record based on bean
     *
     * @param t bean to delete
     */
    void delete(T t);

}

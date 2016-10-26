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
     *  Search bean by primary key. Searches records in checkout and not checkout status.
     *  If record is checked out then the method returns checked out version of record.
     *  If not method returns original version of record.
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
     * @param data Mdmcodes and values to search
     * @param supportingTables Needed for constucting bean by record. Used for populating bean inner classes.
     * @return
     */
    List<T> searchBeansByFilter(Map<String, Object> data, String[] supportingTables);

    /**
     * Performs search by bean patterns. Only simple fields and single lookup of first level are supported
     *
     *
     * @param pattern
     * @param supportingTables
     * @return
     */
    List<T> searchBeanByPattern(T pattern, String[] supportingTables);

    /**
     *
     * Method stores only first level of fields of the type T. Inner type are not supported.
     * Currently it supports only simple type od fields
     * like ({@link Integer}, {@link String}, {@link Boolean} and so on) and types that maps to single MDM type Lookup.
     * Tuples currently don't supported.
     * This method performs search request to MDM for each Lookup field.
     * Collections type are not supported
     * If you don't want update field use flag updatable = false in field annotation
     *
     * @param t bean class to save
     */
    void saveBean(T t);

    /**
     * Create MDM Record based on bean.
     * Currently it supports only simple type od fields
     * like ({@link Integer}, {@link String}, {@link Boolean} and so on) and types that maps to single MDM type Lookup.
     * This method performs search request to MDM for each Lookup field.
     *
     * @param t bean to create
     */
    void create(T t);

    /**
     * Creation method for mass operation.
     * Currently it supports only simple type od fields
     * like ({@link Integer}, {@link String}, {@link Boolean} and so on) and types that maps to single MDM type Lookup.
     * For performance reason it uses cache for Lookup values.
     * It performs search request for Lookup Value only once and later use cache to retrieve the same value.
     * This method is not thread safe.
     *
     *
     * @param items
     * @return
     */
    int create(List<T> items);


    /**
     * Delete MDM record based on bean
     *
     * @param t bean to delete
     */
    void delete(T t);

}

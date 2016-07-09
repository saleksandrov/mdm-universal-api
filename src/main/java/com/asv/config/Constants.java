package com.asv.config;

/**
 * @author Sergey Aleksandrov
 * @since 05.07.2008
 */
public abstract class Constants {

    public static final String REGION_CODE = "Russian [RU]";

    public interface Mdm {
        String REPOSITORY_IDENTY_FIELD_NAME = "ID";
        String TAXONOMY_LOOKUP_ID_FIELD_NAME = "Code";
    }

    public interface Xml {
        // Fields delimiter for MDMRecord-to-XML generation tool
        public static final String XML_DF_DELIMITER = ", ";
    }

}
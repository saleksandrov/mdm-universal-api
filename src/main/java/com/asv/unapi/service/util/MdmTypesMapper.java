package com.asv.unapi.service.util;

import com.sap.mdm.valuetypes.MdmValue;

/**
 * @author alexandrov
 * @since 16.06.2016
 */
public interface MdmTypesMapper {

    Object mapMdmToBeanType(MdmValue value);

    MdmValue mapBeanTypeToMdm(Object value);

}

package com.asv.unapi.service;

import com.asv.unapi.service.util.MdmTypesMapper;
import com.asv.unapi.service.util.MdmTypesMapperImpl;
import com.asv.unapi.dao.BaseMdmDAO;
import com.asv.unapi.service.model.Item;

/**
 * @author Sergey Aleksandrov
 * @since 31.03.2016
 */
public class UniversalFactory {

    private static UniversalFactory ourInstance = new UniversalFactory();

    private static final MdmTypesMapper mapper = new MdmTypesMapperImpl();
    private static final BaseMdmDAO dao = new BaseMdmDAO();

    public static UniversalFactory getInstance() {
        return ourInstance;
    }

    private UniversalFactory() {
    }

    public <T extends Item> UniversalRepoService<T> getService(Class<T> itemClass) {
        return new UniversalRepoServiceImpl<T>(itemClass, dao, mapper);
    }


}

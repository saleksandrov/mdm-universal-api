package com.asv.unapi;

import com.asv.example.model.Service;
import com.asv.example.model.UOM;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author alexandrov
 * @since 05.09.2016
 */
public class CreationTest extends BaseMdmTest {

    @Test
    public void testSearchByParams() {
        Service srv1 = new Service();
        srv1.setName("Test service " + System.currentTimeMillis());
        srv1.setFullName("Test service " + System.currentTimeMillis());
        srv1.setUom(new UOM("ЕР"));

        Service srv2 = new Service();
        srv2.setName("Test service 2 " + System.currentTimeMillis());
        srv2.setFullName("Test service 2" + System.currentTimeMillis());
        srv2.setUom(new UOM("ЕР"));

        List<Service> items = new ArrayList<Service>();
        items.add(srv1);
        items.add(srv2);

        srvService.create(items);

        List<Service> foundService1 = srvService.searchBeanByPattern(srv1, new String[]{"MeasureUnits"});
        List<Service> foundService2 = srvService.searchBeanByPattern(srv2, new String[]{"MeasureUnits"});

        assertTrue(foundService1.size() == 1);
        assertTrue(foundService1.get(0) != null);
        assertTrue(foundService1.get(0).getUom() != null);

        assertTrue(foundService2.size() == 1);
        assertTrue(foundService2.get(0) != null);
        assertTrue(foundService2.get(0).getUom() != null);

        srvService.delete(foundService1.get(0));
        srvService.delete(foundService2.get(0));
    }
}

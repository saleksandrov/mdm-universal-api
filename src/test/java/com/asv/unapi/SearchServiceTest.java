package com.asv.unapi;

import com.asv.example.model.Service;
import com.asv.example.model.UOM;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test depends on MDM repository structure. By default all test are ignored.
 *
 * @author alexandrov
 * @since 28.06.2016
 */
@Ignore
public class SearchServiceTest extends BaseMdmTest {

    @Test
    public void testSearchByParams() {
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("Role", "asu_nsi_srv_curator_economy_dept");
        List<Service> srvList = srvService.searchBeansByFilter(data, new String[]{});
        assertTrue(srvList.size() > 0);
    }

    @Test
    public void testSearchByUom() {
        Service srv = new Service();
        UOM uom = new UOM();
        String symbol = "ЕР";
        uom.setSymbol(symbol);
        srv.setUom(uom);
        String role = "asu_nsi_srv_curator_economy_dept";
        srv.setRole(role);

        List<Service> srvList = srvService.searchBeanByPattern(srv, new String[]{"MeasureUnits"});
        assertTrue(srvList.size() > 0);

        for (Service srv1 : srvList) {
            assertTrue(srv1.getUom().getSymbol().equals(symbol));
            assertTrue(srv1.getRole().equals(role));
        }
    }
}

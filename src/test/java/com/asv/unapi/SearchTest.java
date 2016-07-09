package com.asv.unapi;

import com.asv.config.Constants;
import com.asv.tx.UserSessionManager;
import com.asv.tx.destination.LocalDestination;
import com.asv.example.model.Service;
import com.asv.example.model.UOM;
import com.asv.unapi.service.UniversalFactory;
import com.asv.unapi.service.UniversalRepoService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author alexandrov
 * @since 28.06.2016
 */
public class SearchTest {

    UniversalRepoService<Service> srvService;

    @Before
    public void setUpConnection() {
        UserSessionManager.initUserSession(new LocalDestination("host", "REPO", Constants.REGION_CODE, "user"), "pass");
        UniversalFactory factory = UniversalFactory.getInstance();
        srvService = factory.getService(Service.class);
    }

    @After
    public void testSearch() {
         UserSessionManager.closeSession();
    }

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

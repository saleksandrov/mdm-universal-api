package com.asv.unapi;

import com.asv.example.model.OKVED2;
import com.asv.example.model.Service;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author alexandrov
 * @since 25.03.2016
 */
public class UniversalServiceTest extends SearchTest{

    @Test
    public void testGetById() {
        String gid = "7000002";
        Service srv = srvService.getBeanByPK("GID", gid, new String[]{ "MeasureUnits", "OKEI", "OKVED2", "WorkAndServicesClassifier"});

        assertTrue(srv != null);
        assertTrue(srv.getGID().equals(gid));
        assertTrue(srv.getUom().getSymbol() != null);
    }

    @Test
    public void testModify() {
        String gid = "7000002";
        String[] supportingTables = {"MeasureUnits", "OKEI", "OKVED2", "WorkAndServicesClassifier"};

        //68.31
        String newPositionCode = "01.12";

        Service srv = srvService.getBeanByPK("GID", gid, supportingTables);
        String oldName = srv.getName();
        String newName = oldName + " NEW";
        srv.setName(newName);

        OKVED2 okved2 = new OKVED2();
        okved2.setPositionCode(newPositionCode);
        srv.setOkved2(okved2);

        srvService.saveBean(srv);
        srv = srvService.getBeanByPK("GID", gid, supportingTables);

        assertTrue(srv.getName().equals(newName));
        assertTrue(srv.getOkved2() != null);
        assertTrue(srv.getOkved2().getPositionCode().equals(newPositionCode));

    }

}

package com.asv.unapi;

import com.asv.config.Constants;
import com.asv.example.model.Service;
import com.asv.tx.UserSessionManager;
import com.asv.tx.destination.LocalDestination;
import com.asv.unapi.service.UniversalFactory;
import com.asv.unapi.service.UniversalRepoService;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

/**
 *  Base test class that hold used resources.
 *
 * @author alexandrov
 * @since 11.07.2016
 */
public abstract class BaseMdmTest {

    UniversalRepoService<Service> srvService;

    @Before
    public void setUpConnection() {
        UserSessionManager.initUserSession(new LocalDestination("host", "REPO", Constants.REGION_CODE, "user"), "pass");
        UniversalFactory factory = UniversalFactory.getInstance();
        srvService = factory.getService(Service.class);
    }

    @After
    public void closeResources() {
         UserSessionManager.closeSession();
    }
}

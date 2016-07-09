package com.asv.tx;

import com.asv.tx.destination.Destination;
import com.sap.mdm.session.SessionManager;
import com.sap.mdm.session.SessionTypes;
import com.sap.mdm.session.UserSessionContext;
import com.asv.tx.destination.MdmDestination;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: alexandrov
 * Date: 11.02.2009
 */
public class UserSessionManager {

    private static final Logger log = Logger.getLogger(UserSessionManager.class.getName());

    private static final String GENERIC_PASSWORD = "sso";

    /**
     * Creates new MDM Session
     *
     * @param destinationName name of registered MDM destination
     * @param userName        mdm user login name
     */
    public static void initUserSession(String destinationName, String userName) {
        Destination destination = new MdmDestination(destinationName);
        UserSessionContext usc = destination.getUserSessionContext(userName);
        // SSO authentication no password
        SessionManager.getInstance().createSession(usc, SessionTypes.USER_SESSION_TYPE, GENERIC_PASSWORD);
        bindUserSession(usc);
    }

    /**
     * Creates transaction context based on existing UserSession
     *
     * @param usc
     */
    public static void bindUserSession(UserSessionContext usc) {
        TransactionContext txContext = new TransactionContext(usc);
        TransactionResourceManager.bindResource(txContext);
    }

    /**
     * Method for local test
     *
     * @param destination local destination
     * @param password    mdm user password
     */
    public static void initUserSession(Destination destination, String password) {
        UserSessionContext usc = destination.getUserSessionContext(null);
        SessionManager.getInstance().createSession(usc, SessionTypes.USER_SESSION_TYPE, password);
        TransactionContext txContext = new TransactionContext(usc);
        TransactionResourceManager.bindResource(txContext);
    }

    public static UserSessionContext getUserSessionContext() {
        TransactionContext context = (TransactionContext) TransactionResourceManager.getResource();
        if (context == null) return null;
        return context.getUsc();
    }

    public static void closeSession() {
        try {
            UserSessionContext context = getUserSessionContext();
            // Context could be null when this method is invoked from life cycle method wdExit()
            if (context != null) {
                SessionManager.getInstance().destroySessions(context);
                TransactionResourceManager.unbindResource();
            }
        } catch (Throwable e) {
            // We catch all exceptions because this method could be invoked more than once.
            // This behavior occurs because there is no place in WebDynpro where finalizing logic
            // could be placed. In this case this method will be invoked in request processing phase methods
            // and also in life cycle methods
            log.log(Level.SEVERE, "[FATAL] Cannot close transaction", e);
        }
    }


}

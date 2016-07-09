package com.asv.tx;

import com.sap.mdm.session.UserSessionContext;

/**
 * User: alexandrov
 * Date: 11.02.2009
 */
public class TransactionContext {

    private UserSessionContext usc;

    TransactionContext(UserSessionContext usc) {
        this.usc = usc;
    }

    UserSessionContext getUsc() {
        return usc;
    }

}

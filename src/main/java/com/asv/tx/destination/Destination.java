package com.asv.tx.destination;

import com.sap.mdm.session.UserSessionContext;

/**
 * @author alexandrov
 * @since 03.03.2011
 */
public interface Destination {

    UserSessionContext getUserSessionContext(String userName);

}

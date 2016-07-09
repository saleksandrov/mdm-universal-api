package com.asv.tx.destination;

import com.sap.mdm.session.UserSessionContext;

/**
 * @author Sergey Aleksandrov
 * @since 18.09.2009
 */
public class LocalDestination implements Destination {

    private String serverName;
    private String repositoryName;
    private String regionName;
    private String user;

    public LocalDestination(String serverName, String repositoryName, String regionName, String user) {
        this.serverName = serverName;
        this.repositoryName = repositoryName;
        this.regionName = regionName;
        this.user = user;
    }

    public UserSessionContext getUserSessionContext(String userName) {
        return new UserSessionContext(serverName, repositoryName, regionName, user);
    }

}

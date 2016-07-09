package com.asv.tx.destination;

import com.sap.mdm.session.UserSessionContext;
import com.sap.security.core.server.destinations.api.DestinationService;
import com.sap.security.core.server.destinations.api.DestinationServiceLocator;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Sergey Aleksandrov
 * @since 18.09.2009
 */
public class MdmDestination implements Destination {

    private static final String SERVER_NAME_PROP = "serverName";
    private static final String REPOSITORY_NAME_PROP = "repositoryName";
    private static final String DESTINATION_TYPE = "MDM";

    // destination cache instance for performance reason
    private static Map<String, MdmDestination> destinationCache = new HashMap<String, MdmDestination>();

    private String serverName;
    private String repositoryName;

    public MdmDestination(String destinationName) {
        if (destinationCache.get(destinationName) == null) {
            synchronized (MdmDestination.class) {
                readDestination(destinationName);
                destinationCache.put(destinationName, this);
            }
        } else {
            this.serverName = destinationCache.get(destinationName).getServerName();
            this.repositoryName = destinationCache.get(destinationName).getRepositoryName();
        }
    }

    public String getServerName() {
        return serverName;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    private void readDestination(String destinationName) {
        try {
            DestinationService dstService = DestinationServiceLocator.getInstance();

            if (dstService != null) {
                // This is an ugly workaround.
                // I did't find a jar file with implementation (DynamicDestinationImpl)
                Object d = dstService.getDestination(DESTINATION_TYPE, destinationName);

                Method getPropertyMethod = d.getClass().getMethod("getSimpleProperty", String.class);
                Object serverNamePropertyObj = getPropertyMethod.invoke(d, SERVER_NAME_PROP);
                Object repositoryNamePropertyObj = getPropertyMethod.invoke(d, REPOSITORY_NAME_PROP);

                String serverName = (String) serverNamePropertyObj.getClass().getMethod("getValue").invoke(serverNamePropertyObj);
                String repositoryName = (String) repositoryNamePropertyObj.getClass().getMethod("getValue").invoke(repositoryNamePropertyObj);

                this.serverName = serverName;
                this.repositoryName = repositoryName;
            }

        } catch (Exception e) {
            throw new RuntimeException("Cannot read destination " + destinationName + " " + e.getMessage());
        }
    }

    public UserSessionContext getUserSessionContext(String userName) {
        String serverName = getServerName();
        String repositoryName = getRepositoryName();
        UserSessionContext userSessionContext = new UserSessionContext(serverName, repositoryName, userName);
        userSessionContext.setTrustedConnection(true);
        return userSessionContext;
    }

}

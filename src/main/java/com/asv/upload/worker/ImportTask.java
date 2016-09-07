package com.asv.upload.worker;

import com.asv.unapi.service.model.Item;
import com.sap.mdm.session.UserSessionContext;

import java.util.List;

/**
 * @author alexandrov
 * @since 07.09.2016
 */
public interface ImportTask {

    void setEmailForNotification(String email);

    void setUserSessionContext(UserSessionContext usc);

    WorkerReport perform(List<Item> items);

}

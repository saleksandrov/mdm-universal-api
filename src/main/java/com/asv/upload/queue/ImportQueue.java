package com.asv.upload.queue;

import com.asv.upload.queue.ImportQueueImpl.ImportCommand;

/**
 * @author alexandrov
 * @since 07.09.2016
 */
public interface ImportQueue {

    void addWorkItem(ImportCommand command);

}

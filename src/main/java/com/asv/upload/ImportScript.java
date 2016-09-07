package com.asv.upload;

import com.asv.unapi.service.util.Assert;
import com.asv.upload.parser.ParserAction;
import com.asv.upload.queue.ImportQueue;
import com.asv.upload.queue.ImportQueueImpl;
import com.asv.upload.worker.ImportTask;
import com.asv.upload.worker.WorkerReport;
import com.sap.mdm.session.UserSessionContext;

import java.io.IOException;
import java.io.InputStream;

/**
 * Facade to import functionality. Supports 2 modes of import - synchronous/asynchronous.
 * In asynchronous mode all import tasks performs in background sequential.
 * In order to optimize server resources only one import task can be active.
 *
 * Example:
 * <code>
 *    ImportScript.table(Table.NAME_OF_TABLE)
 *                    .sendNotificationTo("noemail")
 *                    .forFile(fileInputStream)
 *                    .session(UserSessionManager.getUserSessionContext())
 *                    .startSynchronous();
 *
 * </code>
 *
 *
 * @author alexandrov
 * @since 07.09.2016
 */
public class ImportScript {

    private static final ImportQueue importQueue = new ImportQueueImpl();

    private InputStream inputStream;
    private String email;

    private ParserAction parserAction;
    private ImportTask importTask;
    private UserSessionContext usc;

    private ImportScript(Table table ) {
        Assert.notNull(table, "Source table cannot be null");
        this.parserAction = table.parserAction;
        this.importTask = table.importTask;
    }

    // only for test
    private ImportScript(ParserAction parserAction, ImportTask importTask) {
        this.parserAction = parserAction;
        this.importTask = importTask;
    }

    public static ImportScript table(Table table) {
        return new ImportScript(table);
    }

    public static ImportScript create(ParserAction parserAction, ImportTask importTask) {
        return new ImportScript(parserAction, importTask);
    }

    public ImportScript forFile(InputStream inputStream) {
        this.inputStream = inputStream;
        return this;
    }

    public ImportScript session(UserSessionContext usc) {
        this.usc = usc;
        return this;
    }

    public ImportScript sendNotificationTo(String email) {
        this.email = email;
        return this;
    }

    public WorkerReport startSynchronous() throws IOException {
        Assert.notNull(inputStream, "Source file cannot ne null");
        Assert.notNull(usc, "Mdm UserSessionContext  cannot ne null");
        this.importTask.setUserSessionContext(usc);
        return this.importTask.perform(this.parserAction.parse(inputStream));
    }

    public void startAsynchronous() throws IOException {
        Assert.notNull(inputStream, "Source file cannot ne null");
        Assert.notNull(email, "Email cannot be null");
        Assert.notNull(usc, "Mdm UserSessionContext  cannot ne null");
        this.importTask.setUserSessionContext(usc);
        this.importTask.setEmailForNotification(email);
        ImportQueueImpl.ImportCommand ic = new ImportQueueImpl.ImportCommand();
        ic.setImportTask(importTask);
        ic.setItemsToImport(this.parserAction.parse(inputStream));
        importQueue.addWorkItem(ic);
    }

}

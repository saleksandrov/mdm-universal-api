package com.asv.upload.queue;

import com.asv.unapi.service.model.Item;
import com.asv.upload.exception.EmptyImportListException;
import com.asv.upload.exception.TooManyImportTasksException;
import com.asv.upload.worker.ImportTask;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author alexandrov
 * @since 07.09.2016
 */
public class ImportQueueImpl implements ImportQueue {

    private static final String THREAD_NAME =  "Import.Task";
    private static final int QUEUE_CAPACITY = 10;

    private final BlockingQueue<ImportCommand> queue = new ArrayBlockingQueue<ImportCommand>(QUEUE_CAPACITY);

    private Thread workThread;

    public static class ImportCommand {

        List<Item> itemsToImport;
        ImportTask importTask;

        public void setItemsToImport(List<Item> itemsToImport) {
            this.itemsToImport = itemsToImport;
        }

        public void setImportTask(ImportTask importTask) {
            this.importTask = importTask;
        }

        boolean isEmpty() {
            return (itemsToImport == null || itemsToImport.size() == 0 || importTask == null);

        }

        void perform() {
            importTask.perform(itemsToImport);
        }
    }


    public void addWorkItem(ImportCommand command) {
        if (command.isEmpty()) {
            throw new EmptyImportListException("Empty import data list");
        }
        if (!queue.offer(command)) {
            throw new TooManyImportTasksException("Too many import tasks");
        }
        if (workThread == null) {
            workThread = createWorkThread();
            workThread.start();
        }  else if (workThread.isInterrupted() || workThread.getState() == Thread.State.TERMINATED) {
            workThread = createWorkThread();
        }
    }

    private Thread createWorkThread() {

        Runnable task = new Runnable() {

            @Override
            public void run() {
                try {
                    doWork();
                } catch (InterruptedException e) {
                    // OK
                }
            }

            private void doWork() throws InterruptedException {
                ImportCommand importCommand;
                while ((importCommand = queue.take()) != null) {
                    importCommand.perform();
                }
            }

        };
        return new Thread(task, THREAD_NAME);
    }

}

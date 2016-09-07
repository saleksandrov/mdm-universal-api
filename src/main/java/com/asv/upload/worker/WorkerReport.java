package com.asv.upload.worker;

/**
 * @author alexandrov
 * @since 07.09.2016
 */
public class WorkerReport {

    private int count;
    private String name;

    public WorkerReport(String name) {
        this.name = name;
    }

    void incrementTotalCount() {
        this.count++;
    }

    public int getTotalCount() {
        return count;
    }

    public String getName() {
        return name;
    }

    public void setCount(int count) {
        this.count = count;
    }

}

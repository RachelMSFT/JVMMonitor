package com.aac.jvmmonitor.entity;

/**
 * 线程数据对象
 */
public class ThreadDTO extends AbstractDTO {

    private long threadCount;
    private long startedThreadCount;
    private long daemonThreadCount;
    private long peakThreadCount;

    public long getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(long threadCount) {
        this.threadCount = threadCount;
    }

    public long getStartedThreadCount() {
        return startedThreadCount;
    }

    public void setStartedThreadCount(long startedThreadCount) {
        this.startedThreadCount = startedThreadCount;
    }

    public long getDaemonThreadCount() {
        return daemonThreadCount;
    }

    public void setDaemonThreadCount(long daemonThreadCount) {
        this.daemonThreadCount = daemonThreadCount;
    }

    public long getPeakThreadCount() {
        return peakThreadCount;
    }

    public void setPeakThreadCount(long peakThreadCount) {
        this.peakThreadCount = peakThreadCount;
    }
}

package com.aac.jvmmonitor.entity;

/**
 * 内存数据对象
 */
public class MemoryDTO extends AbstractDTO{

    private String memoryType;

    private long init;

    private long max;

    private long used;

    private long commited;

    public String getMemoryType() {
        return memoryType;
    }

    public void setMemoryType(String memoryType) {
        this.memoryType = memoryType;
    }

    public long getInit() {
        return init;
    }

    public void setInit(long init) {
        this.init = init;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public long getUsed() {
        return used;
    }

    public void setUsed(long used) {
        this.used = used;
    }

    public long getCommited() {
        return commited;
    }

    public void setCommited(long commited) {
        this.commited = commited;
    }
}

package com.aac.jvmmonitor.entity;

/**
 * 垃圾回收数据对象
 */
public class GarbageCollectDTO extends AbstractDTO {

    private String gcType;
    private long collectionCount;
    private long collectionTime;

    public long getCollectionCount() {
        return collectionCount;
    }

    public void setCollectionCount(long collectionCount) {
        this.collectionCount = collectionCount;
    }

    public long getCollectionTime() {
        return collectionTime;
    }

    public void setCollectionTime(long collectionTime) {
        this.collectionTime = collectionTime;
    }

    public String getGcType() {
        return gcType;
    }

    public void setGcType(String gcType) {
        this.gcType = gcType;
    }
}

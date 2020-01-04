package com.aac.jvmmonitor.entity;

/**
 * 编译器数据对象
 */
public class CompilerDTO extends AbstractDTO {

    private long totalCompilationTime;

    public long getTotalCompilationTime() {
        return totalCompilationTime;
    }

    public void setTotalCompilationTime(long totalCompilationTime) {
        this.totalCompilationTime = totalCompilationTime;
    }
}

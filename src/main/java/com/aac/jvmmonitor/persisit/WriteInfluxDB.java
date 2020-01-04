package com.aac.jvmmonitor.persisit;

import com.aac.jvmmonitor.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 写数据至InfluxDB
 */
@Service
public class WriteInfluxDB {

    private static final Logger logger = LoggerFactory.getLogger(WriteInfluxDB.class);

    @Value("${spring.influx.url}")
    private String dbUrl;

    @Value("${spring.influx.dbname}")
    private String dbName;

    @Value("${spring.system.isDebug}")
    private boolean isDebug;

    /**
     * Write influxDB
     * 此处有两种设计思路，1是传入一个AbstractDTO对象，通过instance of来判断其类型，通过if else来完成不同情况的处理
     * 2是传入一个AbstractDTO对象和一个类型枚举，而后通过强制类型转换将AbstractDTO转换为对应类型，通过switch case方式实现
     * 综合来说switch case性能略高，但方式1可以少传一个枚举类型的参数，且instanceof更简洁。
     * @param dto AbstractDataTransferObject
     */
    public void doWrite(AbstractDTO dto){
        String targetUrl = dbUrl + "/write?db="+dbName;
        String str = "";
        RestTemplate template = new RestTemplate();
        if(dto instanceof MemoryDTO){
            MemoryDTO memoryDTO = (MemoryDTO)dto;
            str = "memory,";
            str = str + "host="+memoryDTO.getHost()+",";
            str = str + "type="+memoryDTO.getMemoryType().replaceAll(" ","")+" ";
            str = str + "init="+memoryDTO.getInit()+",";
            str = str + "max="+memoryDTO.getMax()+",";
            str = str + "used="+memoryDTO.getUsed()+",";
            str = str + "comitted="+memoryDTO.getCommited();
        }else if (dto instanceof ThreadDTO){
            ThreadDTO threadDTO = (ThreadDTO)dto;
            str = "thread,";
            str = str + "host="+threadDTO.getHost()+" ";
            str = str + "thread_count="+threadDTO.getThreadCount()+",";
            str = str + "daemon_thread_count="+threadDTO.getDaemonThreadCount()+",";
            str = str + "started_thread_count="+threadDTO.getStartedThreadCount()+",";
            str = str + "peak_thread_count="+threadDTO.getPeakThreadCount();
        }else if (dto instanceof ClassLoadDTO){
            ClassLoadDTO classLoadDTO = (ClassLoadDTO)dto;
            str = "classload,";
            str = str + "host="+classLoadDTO.getHost()+" ";
            str = str + "total_loaded="+classLoadDTO.getTotalLoadedClassCount()+",";
            str = str + "loaded="+classLoadDTO.getLoadedClassCount()+",";
            str = str + "unloaded="+classLoadDTO.getUnloadedClassCount();
        }else if (dto instanceof CompilerDTO){
            CompilerDTO compilerDTO = (CompilerDTO)dto;
            str = "complier,";
            str = str + "host="+compilerDTO.getHost()+" ";
            str = str + "total_compilation_time="+compilerDTO.getTotalCompilationTime();
        }else if (dto instanceof GarbageCollectDTO){
            GarbageCollectDTO gcDto = (GarbageCollectDTO)dto;
            str = "gc,";
            str = str + "host="+gcDto.getHost()+",type="+gcDto.getGcType()+" ";
            str = str + "gc_count="+gcDto.getCollectionCount()+",";
            str = str + "gc_time="+gcDto.getCollectionTime();
        }
        if(isDebug){
            logger.debug(str);
        }
        if (!str.equals("")){
            template.postForObject(targetUrl,str,Object.class);
        }
    }
}

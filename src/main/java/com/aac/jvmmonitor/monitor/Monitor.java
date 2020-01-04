package com.aac.jvmmonitor.monitor;

import com.aac.jvmmonitor.entity.*;
import com.aac.jvmmonitor.persisit.WriteInfluxDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.*;
import java.util.Arrays;
import java.util.Set;

@Service
public class Monitor implements SchedulingConfigurer {

    private static Logger logger = LoggerFactory.getLogger(Monitor.class);

    @Value("${spring.system.isDebug}")
    private boolean isDebug;

    @Value("${monitor.server.host}")
    private String host;

    @Value("${monitor.scan.period}")
    private String period;

    private final WriteInfluxDB writeInfluxDB;

    public Monitor(WriteInfluxDB writeInfluxDB) {
        this.writeInfluxDB = writeInfluxDB;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.addTriggerTask(() -> {
            String[] hostList = host.split(";");
            for (String str : hostList) {
                String host = str.split(":")[0];
                int port = Integer.parseInt(str.split(":")[1]);
                try {
                    this.monitorJmx(host, port);
                } catch (IOException e) {
                    logger.error(Arrays.toString(e.getStackTrace()));
                }
            }
        },triggerContext -> {
            CronTrigger trigger = new CronTrigger(period);
            return trigger.nextExecutionTime(triggerContext);
        });
    }

    private void monitorJmx(String host,int port) throws IOException {

        JMXServiceURL jmxServiceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"+host+":"+port+"/jmxrmi");
        JMXConnector connector = JMXConnectorFactory.connect(jmxServiceURL);
        if(isDebug){
            logger.debug("Try to connect to remote Java Virtual Machine：{}",jmxServiceURL.getURLPath());
        }
        MBeanServerConnection mbsc = connector.getMBeanServerConnection();
        if(isDebug){
            logger.debug("Remote Java Virtual Machine Connected");
        }
        memoryMonitor(mbsc,host);
        threadMonitor(mbsc,host);
        loadClassMonitor(mbsc,host);
        compilerMonitor(mbsc,host);
        memoryPoolMonitor(mbsc,host);
        garbageCollectorMonitor(mbsc,host);
        runtimeMonitor(mbsc);
        operatingSystemMonitor(mbsc);
        connector.close();
    }

    /**
     * 内存监控
     * @param mbsc MBean连接对象
     * @param host 主机
     * @throws IOException IO异常
     */
    private void memoryMonitor(MBeanServerConnection mbsc,String host) throws IOException {

        MemoryMXBean memoryMXBean = ManagementFactory.newPlatformMXBeanProxy(mbsc,ManagementFactory.MEMORY_MXBEAN_NAME,MemoryMXBean.class);
        /*
         * 堆内存监控
         */
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryDTO heapMemoryDTO = new MemoryDTO();
        heapMemoryDTO.setInit(heapMemoryUsage.getInit());
        heapMemoryDTO.setMax(heapMemoryUsage.getMax());
        heapMemoryDTO.setUsed(heapMemoryUsage.getUsed());
        heapMemoryDTO.setCommited(heapMemoryUsage.getCommitted());
        heapMemoryDTO.setMemoryType("HEAP");
        heapMemoryDTO.setHost(host);
        writeInfluxDB.doWrite(heapMemoryDTO);

        /*
         * 非堆内存监控
         */
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
        MemoryDTO nonHeapMemoryDTO = new MemoryDTO();
        nonHeapMemoryDTO.setInit(nonHeapMemoryUsage.getInit());
        nonHeapMemoryDTO.setMax(nonHeapMemoryUsage.getMax());
        nonHeapMemoryDTO.setUsed(nonHeapMemoryUsage.getUsed());
        nonHeapMemoryDTO.setCommited(nonHeapMemoryUsage.getCommitted());
        nonHeapMemoryDTO.setMemoryType("NON_HEAP");
        nonHeapMemoryDTO.setHost(host);

        writeInfluxDB.doWrite(nonHeapMemoryDTO);
    }

    /**
     * 线程监控
     * @param mbsc MBean连接对象
     * @param host 主机
     * @throws IOException IO异常
     */
    private void threadMonitor(MBeanServerConnection mbsc,String host) throws IOException {
        ThreadMXBean threadMXBean = ManagementFactory.newPlatformMXBeanProxy(mbsc,ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class);
        ThreadDTO threadDTO = new ThreadDTO();
        threadDTO.setHost(host);
        threadDTO.setStartedThreadCount(threadMXBean.getTotalStartedThreadCount());
        threadDTO.setThreadCount(threadMXBean.getThreadCount());
        threadDTO.setDaemonThreadCount(threadMXBean.getDaemonThreadCount());
        threadDTO.setPeakThreadCount(threadMXBean.getPeakThreadCount());
        writeInfluxDB.doWrite(threadDTO);
    }

    /**
     * 类加载监控
     * @param mbsc MBean连接对象
     * @param host 主机
     * @throws IOException IO异常
     */
    private void loadClassMonitor(MBeanServerConnection mbsc,String host) throws IOException {
        ClassLoadingMXBean classLoadingMXBean = ManagementFactory.newPlatformMXBeanProxy(mbsc,ManagementFactory.CLASS_LOADING_MXBEAN_NAME,ClassLoadingMXBean.class);
        ClassLoadDTO classLoadDTO = new ClassLoadDTO();
        classLoadDTO.setHost(host);
        classLoadDTO.setTotalLoadedClassCount(classLoadingMXBean.getTotalLoadedClassCount());
        classLoadDTO.setLoadedClassCount(classLoadingMXBean.getLoadedClassCount());
        classLoadDTO.setUnloadedClassCount(classLoadingMXBean.getUnloadedClassCount());
        writeInfluxDB.doWrite(classLoadDTO);

    }

    /**
     * 编译器监控
     * @param mbsc MBean连接对象
     * @param host 主机
     * @throws IOException IO异常
     */
    private void compilerMonitor(MBeanServerConnection mbsc,String host) throws IOException {
        CompilationMXBean compilationMXBean = ManagementFactory.newPlatformMXBeanProxy(mbsc,ManagementFactory.COMPILATION_MXBEAN_NAME,CompilationMXBean.class);
        CompilerDTO compilerDTO = new CompilerDTO();
        compilerDTO.setHost(host);
        compilerDTO.setTotalCompilationTime(compilationMXBean.getTotalCompilationTime());
        writeInfluxDB.doWrite(compilerDTO);
    }

    /**
     * 内存池监控
     * @param mbsc MBean连接对象
     * @param host 主机
     * @throws IOException IO异常
     */
    private void memoryPoolMonitor(MBeanServerConnection mbsc,String host) throws IOException {
        ObjectName poolObjectName = null;
        try {
            poolObjectName = new ObjectName(ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE+",*");
        }catch (MalformedObjectNameException e) {
            assert (false);
        }
        Set memoryBeans = mbsc.queryNames(poolObjectName,null);
        if(memoryBeans != null){
            for (Object memoryBean : memoryBeans) {
                ObjectName objectName = (ObjectName) memoryBean;
                MemoryPoolMXBean poolMXBean = ManagementFactory.newPlatformMXBeanProxy(mbsc, objectName.getCanonicalName(), MemoryPoolMXBean.class);

                /*
                 * 当前占用
                 */
                MemoryDTO usedMemoryDTO = new MemoryDTO();
                usedMemoryDTO.setHost(host);
                usedMemoryDTO.setMemoryType(poolMXBean.getName());
                usedMemoryDTO.setInit(poolMXBean.getUsage().getInit());
                usedMemoryDTO.setMax(poolMXBean.getUsage().getMax());
                usedMemoryDTO.setUsed(poolMXBean.getUsage().getUsed());
                usedMemoryDTO.setCommited(poolMXBean.getUsage().getCommitted());

                writeInfluxDB.doWrite(usedMemoryDTO);

                /*
                 * 峰值占用
                 */
                MemoryDTO peakUseMemoryDTO = new MemoryDTO();
                peakUseMemoryDTO.setHost(host);
                peakUseMemoryDTO.setMemoryType(poolMXBean.getName());
                peakUseMemoryDTO.setInit(poolMXBean.getPeakUsage().getInit());
                peakUseMemoryDTO.setMax(poolMXBean.getPeakUsage().getMax());
                peakUseMemoryDTO.setUsed(poolMXBean.getPeakUsage().getUsed());
                peakUseMemoryDTO.setCommited(poolMXBean.getPeakUsage().getCommitted());

                writeInfluxDB.doWrite(peakUseMemoryDTO);
            }
        }
    }

    /**
     * 垃圾回收监控
     * @param mbsc MBean连接对象
     * @param host 主机
     * @throws IOException IO异常
     */
    private void garbageCollectorMonitor(MBeanServerConnection mbsc,String host) throws IOException {
        ObjectName gcObjectName = null;
        try {
            gcObjectName = new ObjectName(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE+",*");
        }catch (MalformedObjectNameException e) {
            assert (false);
        }
        Set gcBeans = mbsc.queryNames(gcObjectName,null);
        if(gcBeans != null){
            for (Object gcBean : gcBeans) {
                ObjectName objectName = (ObjectName) gcBean;
                GarbageCollectorMXBean gcMXBean = ManagementFactory.newPlatformMXBeanProxy(mbsc, objectName.getCanonicalName(), GarbageCollectorMXBean.class);
                GarbageCollectDTO garbageCollectDTO = new GarbageCollectDTO();
                garbageCollectDTO.setHost(host);
                garbageCollectDTO.setGcType(gcMXBean.getName().replaceAll(" ",""));
                garbageCollectDTO.setCollectionCount(gcMXBean.getCollectionCount());
                garbageCollectDTO.setCollectionTime(gcMXBean.getCollectionTime());

                writeInfluxDB.doWrite(garbageCollectDTO);
            }
        }
    }

    private void runtimeMonitor(MBeanServerConnection mbsc) throws IOException {
        RuntimeMXBean runtimeMXBean = ManagementFactory.newPlatformMXBeanProxy(mbsc,ManagementFactory.RUNTIME_MXBEAN_NAME,RuntimeMXBean.class);
        /*
         * 运行时详情
         */
        logger.info(" ");
        logger.info("运行时详情：");
        logger.info("JVM名称：{}",runtimeMXBean.getVmName());
        logger.info("JVM版本：{}",runtimeMXBean.getVmVersion());
    }

    private void operatingSystemMonitor(MBeanServerConnection mbsc) throws IOException {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.newPlatformMXBeanProxy(mbsc,ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME,OperatingSystemMXBean.class);
        /*
         * 宿主机详情
         */
        double systemLoadAverage = operatingSystemMXBean.getSystemLoadAverage();
        double availableProcessors = operatingSystemMXBean.getAvailableProcessors();
        String osVersion = operatingSystemMXBean.getVersion();
        logger.info(" ");
        logger.info("系统详情：");
        logger.info("系统平均负载：{}",systemLoadAverage);
        logger.info("可用处理器数量：{}",availableProcessors);
        logger.info("系统内核版本：{}",osVersion);
    }
}

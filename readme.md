# 关于这个项目

起源是由于模具系统宕机频率非常高，为对其JVM内存进行分析而开发的工具，后来发现不局限于模具系统，任何Tomcat，甚至任何Java应用程序都可以通过该程序对其内存进行持续监控。

---
程序核心是采用了javax.management包中的一系列远程管理API来实现数据抓取，通过influxDB来完成数据存储，通过Grafana来完成数据展示

---

待监控服务需要开启JMX远程管理，通过java启动参数完成开启：

```   
-Dcom.sun.management.jmxremote 
-Dcom.sun.management.jmxremote.port=6670
-Dcom.sun.management.jmxremote.ssl=false 
-Dcom.sun.management.jmxremote.authenticate=false
```
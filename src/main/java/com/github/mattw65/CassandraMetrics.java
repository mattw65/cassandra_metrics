package com.github.mattw65;

import java.util.HashMap;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.json.JSONObject;

public class CassandraMetrics {
    public static JSONObject getMetrics(String node)  throws Exception {
        JMXServiceURL jmxUrl = new JMXServiceURL(String.format("service:jmx:rmi:///jndi/rmi://%s:7199/jmxrmi", node));
        JMXConnector jmxc = JMXConnectorFactory.connect(jmxUrl);
        MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
        ObjectName osBean = new ObjectName("java.lang:type=OperatingSystem");
        ObjectName memoryBean = new ObjectName("java.lang:type=Memory");
        ObjectName readLatencyBean = new ObjectName("org.apache.cassandra.metrics:type=ClientRequest,scope=Read,name=Latency");
        ObjectName writeLatencyBean = new ObjectName("org.apache.cassandra.metrics:type=ClientRequest,scope=Write,name=Latency");
        ObjectName compactionsBean = new ObjectName("org.apache.cassandra.metrics:type=Compaction,name=PendingTasks");
        ObjectName connectionsBean = new ObjectName("org.apache.cassandra.metrics:type=Client,name=connectedNativeClients");
        ObjectName storageServiceMBeanName = new ObjectName("org.apache.cassandra.metrics:type=Storage,name=Load");
        
        HashMap<String, Object> metrics = new HashMap<String, Object>();
        metrics.put("CpuUsage", mbsc.getAttribute(osBean, "ProcessCpuLoad"));
        metrics.put("MemoryUsage(mb)", (long) ((CompositeData) mbsc.getAttribute(memoryBean, "HeapMemoryUsage")).get("committed") / (1024*1024));
        metrics.put("PendingCompactions", (int) mbsc.getAttribute(compactionsBean, "Value"));
        metrics.put("Connections", (int) mbsc.getAttribute(connectionsBean, "Value"));
        metrics.put("StorageUsed(MB)", (long) mbsc.getAttribute(storageServiceMBeanName, "Count") / (1024*1024));

        Double readLatency = (Double) mbsc.getAttribute(readLatencyBean, "Mean");
        if (!readLatency.isNaN()) metrics.put("ReadLatency(ms)", readLatency);
        Double writeLatency = (Double) mbsc.getAttribute(writeLatencyBean, "Mean");
        if (!writeLatency.isNaN()) metrics.put("WriteLatency(ms)", writeLatency);
        
        jmxc.close();

        return new JSONObject(metrics);
    }
}

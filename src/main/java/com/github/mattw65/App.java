package com.github.mattw65;

import java.util.HashMap;
import org.json.JSONObject;

public final class App {
    public static void main(String[] args) {
        
        // Add nodes here
        String[] nodes = new String[] {};

        HashMap<String, JSONObject> metrics = new HashMap<String, JSONObject>();
        for (String node: nodes) {
            try {
                JSONObject nodeMetrics = CassandraMetrics.getMetrics(node);
                metrics.put(node, nodeMetrics);
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        System.out.println((new JSONObject(metrics)).toString(4));
    }
}

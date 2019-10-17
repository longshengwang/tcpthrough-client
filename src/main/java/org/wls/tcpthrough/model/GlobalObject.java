package org.wls.tcpthrough.model;

import org.wls.tcpthrough.data.DataClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wls on 2019/10/16.
 */
public class GlobalObject {
    public static Map<String, DataClient> dataClientMap = new HashMap<>();

}

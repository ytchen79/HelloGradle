package com.cyt.utils.entity;

import com.cyt.utils.Assert;
import com.cyt.utils.json.JSONUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataMap {

    private Map<String, List<Map<String, String>>> dataMapListMap;

    public DataMap() {
        dataMapListMap = new LinkedHashMap<>();
    }

    public void parseJsonDataMap(String dataStr) {
        Assert.isTrue(JSONUtils.isValidateJson(dataStr), "ILLEGAL JSON STRING");
        dataMapListMap = JSONUtils.jsonToObject(dataStr, Map.class);
    }

    public List<Map<String, String>> getDataMapList(String name) {
        return dataMapListMap.get(name);
    }

    public Map<String, String> getDataMap(String name) {
        List<Map<String, String>> dataMapList = getDataMapList(name);
        return (dataMapList == null || dataMapList.size() == 0) ? null : dataMapList.get(0);
    }

    public void addDataMap(String name, Map<String, String> dataMap) {
        List<Map<String, String>> dataMapList = getDataMapList(name);
        if (dataMapList == null) {
            dataMapList = new ArrayList<>();
            dataMapListMap.put(name, dataMapList);
        }
        dataMapList.add(dataMap);
    }

    public void addDataMapList(String name, List<Map<String, String>> dataMapList) {
        for (Map<String, String> dataMap : dataMapList) {
            addDataMap(name, dataMap);
        }
    }


}

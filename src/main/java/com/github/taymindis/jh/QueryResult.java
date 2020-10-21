package com.github.taymindis.jh;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class QueryResult  {

    private final Map<String, List<Object>> vMap;
    private int index = -1;
    private final int size;

    public QueryResult(Map<String, List<Object>> vMap, int totalSize) {
        if(vMap == null || totalSize <= 0) {
            throw new NullPointerException("Query Result Cannot be null or total size cannot be 0 or negative value");
        }
        this.vMap = vMap;
        this.size = totalSize;
    }

    public Object get(String colName) {
        return vMap.get(colName).get(index);
    }

    /**
     * @return first column value
     */
    public Object get() {
        List<Object> objects = vMap.values().iterator().next();
        if(objects != null && objects.size() > 0) {
            return objects.get(index);
        }
        return null;
    }

    /**
     * @return all the columns name
     */
    public Set<String> getAllColumns() {
        return vMap.keySet();
    }

    public boolean next() {
        return ++index < this.size;
    }

}

package rs117.hd.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModelCache extends LinkedHashMap<Integer, ModelData> {
    private final int capacity;

    public ModelCache(int capacity) {
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }

    public ModelData get(int key) {
        return super.get(key);
    }

    public void put(int key, ModelData value) {
        super.put(key, value);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, ModelData> eldest) {
        return this.size() > this.capacity;
    }
}

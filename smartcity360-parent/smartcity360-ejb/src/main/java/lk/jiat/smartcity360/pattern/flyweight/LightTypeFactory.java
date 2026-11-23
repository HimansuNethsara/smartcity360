package lk.jiat.smartcity360.pattern.flyweight;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public class LightTypeFactory {

    private final ConcurrentHashMap<Integer, LightType> pool = new ConcurrentHashMap<>();
    private final LongAdder requests = new LongAdder();
    private final LongAdder created = new LongAdder();

    public LightType get(LightTypeSpec spec) {
        requests.increment();
        return pool.computeIfAbsent(spec.id(), id -> {
            created.increment();
            return new LightType(spec);
        });
    }

    public void evict(int typeId) {
        pool.remove(typeId);
    }

    public void clear() {
        pool.clear();
        requests.reset();
        created.reset();
    }

    public int poolSize() { return pool.size(); }
    public long getRequests() { return requests.sum(); }
    public long getCreated() { return created.sum(); }
    public long getHits() { return requests.sum() - created.sum(); }

    public List<LightType> pooledTypes() {
        return new ArrayList<>(pool.values());
    }
}

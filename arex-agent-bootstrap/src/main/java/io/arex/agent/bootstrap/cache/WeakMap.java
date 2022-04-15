package io.arex.agent.bootstrap.cache;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.*;

public class WeakMap<K, V> extends ReferenceQueue<K> {
    public static WeakMap<Object, Object> DEFAULT = new WeakMap<>();

    final ConcurrentMap<WeakReferenceKey<K>, V> target;

    protected WeakMap() {
        this(new ConcurrentHashMap<>());
    }

    protected WeakMap(ConcurrentMap<WeakReferenceKey<K>, V> target) {
        this.target = target;
    }

    public V get(K key) {
        check();
        return target.get(new WeakReferenceKey<>(key, this));
    }

    public boolean containsKey(K key) {
        check();
        return target.containsKey(key);
    }

    public V put(K key, V value) {
        check();
        return target.put(new WeakReferenceKey<>(key, this), value);
    }

    public V remove(K key) {
        check();
        return target.remove(key);
    }

    public void clear() {
        target.clear();
    }

    public void check() {
        Reference<?> reference;
        while ((reference = poll()) != null) {
            target.remove(reference);
        }
    }

    public int size() {
        check();
        return target.size();
    }

    static final class WeakReferenceKey<K> extends WeakReference<K> {
        private final int hashCode;

        WeakReferenceKey(K key, ReferenceQueue<? super K> queue) {
            super(key, queue);

            hashCode = System.identityHashCode(key);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof WeakMap.WeakReferenceKey<?>) {
                return ((WeakReferenceKey<?>) other).get() == get();
            } else {
                return other.equals(this);
            }
        }
    }
}


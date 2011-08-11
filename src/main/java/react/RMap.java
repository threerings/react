//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Provides a reactive model of a map. Note that {@link #put} and other default mechanisms for
 * updating the map <em>will not</em> trigger a notification if the updated value is equal to the
 * value already in the map. Use {@link #putForce} to force a notification. Similarly, {@link
 * #remove} will only generate a notification if a mapping for the specified key existed.
 */
public class RMap<K,V> extends Reactor<RMap.Listener<K,V>>
    implements Map<K,V>
{
    /** An interface for publishing map events to listeners. */
    public static abstract class Listener<K,V> extends Reactor.RListener
    {
        /**
         * Notifies listener of an added or updated mapping. This method will call the
         * old-value-forgetting version ({@link #onPut(K,V)}) by default.
         */
        public void onPut (K key, V value, V oldValue) {
            onPut(key, value);
        }

        /**
         * Notifies listener of an added or updated mapping.
         */
        public void onPut (K key, V value) {
            // noop
        }

        /**
         * Notifies listener of a removed mapping. This method will call the old-value-forgetting
         * version ({@link #onRemove(K)}) by default.
         */
        public void onRemove (K key, V oldValue) {
            onRemove(key);
        }

        /**
         * Notifies listener of a removed mapping.
         */
        public void onRemove (K key) {
            // noop
        }
    }

    /**
     * Creates a distributed map with the supplied underlying map implementation.
     */
    public static <K,V> RMap<K,V> create (Map<K,V> impl) {
        return new RMap<K,V>(impl);
    }

    /**
     * Creates a distributed map with the supplied underlying map implementation.
     */
    public RMap (Map<K,V> impl) {
        _impl = impl;
    }

    /**
     * Connects the supplied listener to this map, such that it will be notified on puts and
     * removes.
     * @return a connection instance which can be used to cancel the connection.
     */
    public Connection listen (Listener<? super K, ? super V> listener) {
        // alas, Java does not support higher kinded types; this cast is safe
        @SuppressWarnings("unchecked") Listener<K,V> casted = (Listener<K,V>)listener;
        return addConnection(casted);
    }

    /**
     * Updates the mapping with the supplied key and value, and notifies registered listeners
     * regardless of whether the new value is equal to the old value.
     * @return the previous value mapped to the supplied key, or null.
     */
    public V putForce (K key, V value) {
        checkMutate();
        V ovalue = _impl.put(key, value);
        emitPut(key, value, ovalue);
        return ovalue;
    }

    /**
     * Removes the mapping associated with the supplied key, and notifies registered listeners
     * regardless of whether a previous mapping existed or not.
     * @return the previous value mapped to the supplied key, or null.
     */
    public V removeForce (K key) {
        checkMutate();
        V ovalue = _impl.remove(key);
        emitRemove(key, ovalue);
        return ovalue;
    }

    /**
     * Returns a value that models whether the specified key is contained in this map. The value
     * will report a change when a mapping for the specified key is added or removed. Note: this
     * view only works on maps that <em>do not</em> contain mappings to {@code null}.
     */
    public MappedValueView<Boolean> containsKeyView (final K key) {
        if (key == null) throw new NullPointerException("Must supply non-null 'key'.");
        final MappedValue<Boolean> model = new MappedValue<Boolean>() {
            public Boolean get () {
                return containsKey(key);
            }
        };
        model.setConnection(listen(new Listener<K,V>() {
            @Override public void onPut (K pkey, V value, V ovalue) {
                if (key.equals(pkey)) {
                    if (ovalue == null) model.notifyChange(true, false);
                }
            }
            @Override public void onRemove (K pkey, V ovalue) {
                if (key.equals(pkey)) {
                    model.notifyChange(false, true);
                }
            }
        }));
        return model;
    }

    /**
     * Returns a value that models the mapping of the specified key in this map. The value will
     * report a change when the mapping for the specified key is changed or removed.
     */
    public MappedValueView<V> getView (final K key) {
        if (key == null) throw new NullPointerException("Must supply non-null 'key'.");
        final MappedValue<V> model = new MappedValue<V>() {
            public V get () {
                return RMap.this.get(key);
            }
        };
        model.setConnection(listen(new Listener<K,V>() {
            @Override public void onPut (K pkey, V value, V ovalue) {
                if (key.equals(pkey)) {
                    model.notifyChange(value, ovalue);
                }
            }
            @Override public void onRemove (K pkey, V ovalue) {
                if (key.equals(pkey)) {
                    model.notifyChange(null, ovalue);
                }
            }
        }));
        return model;
    }

    // from interface Map<K,V>
    public int size () {
        return _impl.size();
    }

    // from interface Map<K,V>
    public boolean isEmpty () {
        return _impl.isEmpty();
    }

    // from interface Map<K,V>
    public boolean containsKey (Object key) {
        return _impl.containsKey(key);
    }

    // from interface Map<K,V>
    public boolean containsValue (Object value) {
        return _impl.containsValue(value);
    }

    // from interface Map<K,V>
    public V get (Object key) {
        return _impl.get(key);
    }

    // from interface Map<K,V>
    public V put (K key, V value) {
        checkMutate();
        V ovalue = _impl.put(key, value);
        if (!areEqual(value, ovalue)) {
            emitPut(key, value, ovalue);
        }
        return ovalue;
    }

    // from interface Map<K,V>
    public V remove (Object rawKey) {
        checkMutate();

        // avoid generating an event if no mapping exists for the supplied key
        if (!_impl.containsKey(rawKey)) {
            return null;
        }

        @SuppressWarnings("unchecked") K key = (K)rawKey;
        V ovalue = _impl.remove(key);
        emitRemove(key, ovalue);

        return ovalue;
    }

    // from interface Map<K,V>
    public void putAll (Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    // from interface Map<K,V>
    public void clear () {
        checkMutate();
        // generate removed events for our keys (do so on a copy of our set so that we can clear
        // our underlying map before any of the published events are processed)
        Set<Map.Entry<K,V>> entries = new HashSet<Map.Entry<K,V>>(_impl.entrySet());
        _impl.clear();
        for (Map.Entry<K,V> entry : entries) {
            emitRemove(entry.getKey(), entry.getValue());
        }
    }

    // from interface Map<K,V>
    public Set<K> keySet () {
        final Set<K> iset = _impl.keySet();
        return new AbstractSet<K>() {
            public Iterator<K> iterator () {
                final Iterator<K> iiter = iset.iterator();
                return new Iterator<K>() {
                    public boolean hasNext () {
                        return iiter.hasNext();
                    }
                    public K next () {
                        return (_current = iiter.next());
                    }
                    public void remove () {
                        if (_current == null) throw new IllegalStateException();
                        checkMutate();
                        V ovalue = RMap.this.get(_current);
                        iiter.remove();
                        emitRemove(_current, ovalue);
                        _current = null;
                    }
                    protected K _current;
                };
            }
            public int size () {
                return RMap.this.size();
            }
            public boolean remove (Object o) {
                checkMutate();
                V ovalue = RMap.this.get(o);
                boolean modified = iset.remove(o);
                if (modified) {
                    @SuppressWarnings("unchecked") K key = (K)o;
                    emitRemove(key, ovalue);
                }
                return modified;
            }
            public void clear () {
                RMap.this.clear();
            }
        };
    }

    // from interface Map<K,V>
    public Collection<V> values () {
        final Collection<Map.Entry<K,V>> iset = _impl.entrySet();
        return new AbstractCollection<V>() {
            public Iterator<V> iterator () {
                final Iterator<Map.Entry<K,V>> iiter = iset.iterator();
                return new Iterator<V>() {
                    public boolean hasNext () {
                        return iiter.hasNext();
                    }
                    public V next () {
                        return (_current = iiter.next()).getValue();
                    }
                    public void remove () {
                        if (_current == null) throw new IllegalStateException();
                        checkMutate();
                        iiter.remove();
                        emitRemove(_current.getKey(), _current.getValue());
                        _current = null;
                    }
                    protected Map.Entry<K,V> _current;
                };
            }
            public int size () {
                return RMap.this.size();
            }
            public boolean contains (Object o) {
                return RMap.this.containsValue(o);
            }
            public void clear () {
                RMap.this.clear();
            }
        };
    }

    // from interface Map<K,V>
    public Set<Map.Entry<K,V>> entrySet () {
        final Set<Map.Entry<K,V>> iset = _impl.entrySet();
        return new AbstractSet<Map.Entry<K,V>>() {
            public Iterator<Map.Entry<K,V>> iterator () {
                final Iterator<Map.Entry<K,V>> iiter = iset.iterator();
                return new Iterator<Map.Entry<K,V>>() {
                    public boolean hasNext () {
                        return iiter.hasNext();
                    }
                    public Map.Entry<K,V> next () {
                        _current = iiter.next();
                        return new Map.Entry<K,V>() {
                            public K getKey () {
                                return _current.getKey();
                            }
                            public V getValue () {
                                return _current.getValue();
                            }
                            public V setValue (V value) {
                                checkMutate();
                                V ovalue = _current.setValue(value);
                                if (!areEqual(value, ovalue)) {
                                    emitPut(_current.getKey(), value, ovalue);
                                }
                                return ovalue;
                            }
                            // it's safe to pass these through because Map.Entry's
                            // implementations operate solely on getKey/getValue
                            public boolean equals (Object o) {
                                return _current.equals(o);
                            }
                            public int hashCode () {
                                return _current.hashCode();
                            }
                        };
                    }
                    public void remove () {
                        if (_current == null) throw new IllegalStateException();
                        checkMutate();
                        iiter.remove();
                        emitRemove(_current.getKey(), _current.getValue());
                        _current = null;
                    }
                    protected Map.Entry<K,V> _current;
                };
            }
            public boolean contains (Object o) {
                return iset.contains(o);
            }
            public boolean remove (Object o) {
                checkMutate();
                boolean modified = iset.remove(o);
                if (modified) {
                    @SuppressWarnings("unchecked") Map.Entry<K,V> entry = (Map.Entry<K,V>)o;
                    emitRemove(entry.getKey(), entry.getValue());
                }
                return modified;
            }
            public int size () {
                return RMap.this.size();
            }
            public void clear () {
                RMap.this.clear();
            }
        };
    }

    protected void emitPut (K key, V value, V oldValue) {
        notifyPut(key, value, oldValue);
    }

    protected void notifyPut (K key, V value, V oldValue) {
        Cons<RMap.Listener<K,V>> lners = prepareNotify();
        try {
            for (Cons<RMap.Listener<K,V>> cons = lners; cons != null; cons = cons.next) {
                cons.listener.onPut(key, value, oldValue);
                if (cons.oneShot) cons.disconnect();
            }
        } finally {
            finishNotify(lners);
        }
    }

    protected void emitRemove (K key, V oldValue) {
        notifyRemove(key, oldValue);
    }

    protected void notifyRemove (K key, V oldValue) {
        Cons<RMap.Listener<K,V>> lners = prepareNotify();
        try {
            for (Cons<RMap.Listener<K,V>> cons = lners; cons != null; cons = cons.next) {
                cons.listener.onRemove(key, oldValue);
                if (cons.oneShot) cons.disconnect();
            }
        } finally {
            finishNotify(lners);
        }
    }

    /** Contains our underlying mappings. */
    protected Map<K, V> _impl;
}

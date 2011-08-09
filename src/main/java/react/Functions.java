//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import java.util.Map;

/**
 * Various {@link Function} related utility methods.
 */
public class Functions
{
    /** Implements boolean not. */
    public static Function<Boolean, Boolean> NOT = new Function<Boolean, Boolean>() {
        public Boolean apply (Boolean value) {
            return !value;
        }
    };

    /** A function that applies {@link String#valueOf} to its argument. */
    public static Function<Object, String> TO_STRING = new Function<Object, String>() {
        public String apply (Object value) {
            return String.valueOf(value);
        }
    };

    /**
     * Returns a function which performs a map lookup with a default value. The function created by
     * this method returns defaultValue for all inputs that do not belong to the map's key set.
     */
    public static <K, V> Function<K, V> forMap (final Map<K, ? extends V> map, final V defaultValue)
    {
        return new Function<K, V>() {
            public V apply (K key) {
                V value = map.get(key);
                return (value != null || map.containsKey(key)) ? value : defaultValue;
            }
        };
    }
}

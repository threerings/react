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

    /** A function that returns true for null values and false for non-null values. */
    public static Function<Object, Boolean> IS_NULL = new Function<Object, Boolean>() {
        public Boolean apply (Object value) {
            return (value == null);
        }
    };

    /** A function that returns true for non-null values and false for null values. */
    public static Function<Object, Boolean> NON_NULL = new Function<Object, Boolean>() {
        public Boolean apply (Object value) {
            return (value != null);
        }
    };

    /** A function that returns the float value of a number. */
    public static Function<Number,Float> FLOAT_VALUE = new Function<Number,Float>() {
        public Float apply (Number value) {
            return value.floatValue();
        }
    };

    /** A function that returns the int value of a number. */
    public static Function<Number,Integer> INT_VALUE = new Function<Number,Integer>() {
        public Integer apply (Number value) {
            return value.intValue();
        }
    };

    /**
     * Returns a function that always returns the supplied constant value.
     */
    public static <E> Function<Object,E> constant (final E constant) {
        return new Function<Object,E>() {
            public E apply (Object value) {
                return constant;
            }
        };
    }

    /**
     * Returns a function that computes whether a value is greater than {@code target}.
     */
    public static Function<Integer,Boolean> greaterThan (final int target) {
        return new Function<Integer,Boolean>() {
            public Boolean apply (Integer value) {
                return value > target;
            }
        };
    }

    /**
     * Returns a function that computes whether a value is greater than or equal to {@code value}.
     */
    public static Function<Integer,Boolean> greaterThanEqual (final int target) {
        return new Function<Integer,Boolean>() {
            public Boolean apply (Integer value) {
                return value >= target;
            }
        };
    }

    /**
     * Returns a function that computes whether a value is less than {@code target}.
     */
    public static Function<Integer,Boolean> lessThan (final int target) {
        return new Function<Integer,Boolean>() {
            public Boolean apply (Integer value) {
                return value < target;
            }
        };
    }

    /**
     * Returns a function that computes whether a value is less than or equal to {@code target}.
     */
    public static Function<Integer,Boolean> lessThanEqual (final int target) {
        return new Function<Integer,Boolean>() {
            public Boolean apply (Integer value) {
                return value <= target;
            }
        };
    }

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

    /**
     * Returns a function which returns its argument as a string with {@code prefix} prepended.
     */
    public static <T> Function<T,String> prefix (final String prefix) {
        return new Function<T,String>() {
            public String apply (T value) {
                return prefix + value;
            }
        };
    }

    /**
     * Returns a function which returns its argument as a string with {@code suffix} appended.
     */
    public static <T> Function<T,String> suffix (final String suffix) {
        return new Function<T,String>() {
            public String apply (T value) {
                return value + suffix;
            }
        };
    }

    /**
     * Returns the identity function for type {@code T}.
     */
    public static <T> Function<T, T> identity () {
        @SuppressWarnings("unchecked") Function<T, T> ident = (Function<T, T>)IDENT;
        return ident;
    }

    protected static final Function<Object, Object> IDENT = new Function<Object, Object>() {
        public Object apply (Object value) {
            return value;
        }
    };
}

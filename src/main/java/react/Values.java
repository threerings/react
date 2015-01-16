//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides utility methods for {@link Value}s.
 */
public class Values
{
    /**
     * Creates a boolean value that is toggled every time the supplied signal fires.
     *
     * @param signal the signal that will trigger the toggling.
     * @param initial the initial value of the to be toggled value.
     */
    public static ValueView<Boolean> toggler (final SignalView<?> signal, final boolean initial) {
        return new MappedValue<Boolean>() {
            @Override public Boolean get () {
                return _current;
            }
            @Override protected Connection connect () {
                return signal.connect(new UnitSlot() {
                    @Override public void onEmit () {
                        boolean old = _current;
                        notifyChange(_current = !old, old);
                    }
                });
            }
            protected boolean _current = initial;
        };
    }

    /**
     * Returns a value which is the logical NOT of the supplied value.
     */
    public static ValueView<Boolean> not (ValueView<Boolean> value) {
        return value.map(Functions.NOT);
    }

    /**
     * Returns a value which is the logical AND of the supplied values.
     */
    @SuppressWarnings("unchecked") // TODO: use new varargs suppression in JDK 1.7
    public static ValueView<Boolean> and (ValueView<Boolean> one, ValueView<Boolean> two) {
        return and(Arrays.asList(one, two));
    }

    /**
     * Returns a value which is the logical AND of the supplied values.
     */
    public static ValueView<Boolean> and (ValueView<Boolean>... values) {
        return and(Arrays.asList(values));
    }

    /**
     * Returns a value which is the logical AND of the supplied values.
     */
    public static ValueView<Boolean> and (final Iterable<? extends ValueView<Boolean>> values) {
        return aggValue(values, COMPUTE_AND);
    }

    /**
     * Returns a value which is the logical OR of the supplied values.
     */
    @SuppressWarnings("unchecked") // TODO: use new varargs suppression in JDK 1.7
    public static ValueView<Boolean> or (ValueView<Boolean> one, ValueView<Boolean> two) {
        return or(Arrays.asList(one, two));
    }

    /**
     * Returns a value which is the logical OR of the supplied values.
     */
    public static ValueView<Boolean> or (ValueView<Boolean>... values) {
        return or(Arrays.asList(values));
    }

    /**
     * Returns a value which is the logical OR of the supplied values.
     */
    public static ValueView<Boolean> or (final Iterable<? extends ValueView<Boolean>> values) {
        return aggValue(values, COMPUTE_OR);
    }

    /**
     * Returns a view of the supplied signal as a value. It will contain the value {@code initial}
     * until the signal fires, at which time the value will be updated with the emitted value.
     */
    public static <T> ValueView<T> asValue (final SignalView<T> signal, final T initial) {
        return new MappedValue<T>() {
            @Override public T get () {
                return _value;
            }
            @Override protected T updateLocal (T value) {
                T ovalue = _value;
                _value = value;
                return ovalue;
            }
            @Override protected Connection connect () {
                return signal.connect(new Slot<T>() {
                    public void onEmit (T value) {
                        updateAndNotifyIf(value);
                    }
                });
            }
            protected T _value = initial;
        };
    }

    protected static final ValueView<Boolean> aggValue (
        final Iterable<? extends ValueView<Boolean>> values,
        final Function<Iterable<? extends ValueView<Boolean>>,Boolean> aggOp) {

        return new MappedValue<Boolean>() {
            @Override public Boolean get () {
                return aggOp.apply(values);
            }

            @Override protected Connection connect () {
                final List<Connection> conns = new ArrayList<Connection>();
                for (ValueView<Boolean> value : values) {
                    conns.add(value.connect(_trigger));
                }
                return new Connection() {
                    public void close () {
                        for (Connection conn : conns) conn.close();
                    }
                    public Connection once () {
                        for (Connection conn : conns) conn.once();
                        return this;
                    }
                    public Connection atPrio (int priority) {
                        for (Connection conn : conns) conn.atPrio(priority);
                        return this;
                    }
                    public Connection holdWeakly () {
                        for (Connection conn : conns) conn.holdWeakly();
                        return this;
                    }
                };
            }

            protected final UnitSlot _trigger = new UnitSlot() {
                public void onEmit () {
                    boolean ovalue = _current;
                    _current = aggOp.apply(values);
                    notifyChange(_current, ovalue);
                }
                protected boolean _current = aggOp.apply(values);
            };
        };
    }

    protected static final Function<Iterable<? extends ValueView<Boolean>>,Boolean> COMPUTE_AND =
        new Function<Iterable<? extends ValueView<Boolean>>,Boolean>() {
            public Boolean apply (Iterable<? extends ValueView<Boolean>> values) {
                for (ValueView<Boolean> value : values) {
                    if (!value.get()) return false;
                }
                return true;
            }
        };

    protected static final Function<Iterable<? extends ValueView<Boolean>>,Boolean> COMPUTE_OR =
        new Function<Iterable<? extends ValueView<Boolean>>,Boolean>() {
            public Boolean apply (Iterable<? extends ValueView<Boolean>> values) {
                for (ValueView<Boolean> value : values) {
                    if (value.get()) return true;
                }
                return false;
            }
        };

    private Values () {} // no constructski
}

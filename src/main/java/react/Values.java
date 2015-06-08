//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Provides utility methods for {@link Value}s.
 */
public class Values
{
    /** Used by {@link #join(ValueView,ValueView)}. */
    public static class T2<A,B> {
        public final A a;
        public final B b;
        public T2 (A a, B b) {
            this.a = a;
            this.b = b;
        }

        @Override public String toString () {
            return "T2(" + a + ", " + b + ")";
        }
        @Override public int hashCode () {
            return Objects.hashCode(a) ^ Objects.hashCode(b);
        }
        @Override public boolean equals (Object other) {
            if (!(other instanceof T2<?,?>)) return false;
            T2<?,?> ot = (T2<?,?>)other;
            return Objects.equals(a, ot.a) && Objects.equals(b, ot.b);
        }
    }

    /** Used by {@link #join(ValueView,ValueView,ValueView)}. */
    public static class T3<A,B,C> {
        public final A a;
        public final B b;
        public final C c;
        public T3 (A a, B b, C c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        @Override public String toString () {
            return "T3(" + a + ", " + b + ", " + c + ")";
        }
        @Override public int hashCode () {
            return Objects.hashCode(a) ^ Objects.hashCode(b);
        }
        @Override public boolean equals (Object other) {
            if (!(other instanceof T3<?,?,?>)) return false;
            T3<?,?,?> ot = (T3<?,?,?>)other;
            return Objects.equals(a, ot.a) && Objects.equals(b, ot.b) && Objects.equals(c, ot.c);
        }
    }

    /**
     * Returns a reactive value which is triggered when either of {@code a, b} emits an event. The
     * mapped value will retain connections to {@code a+b} only while it itself has connections.
     */
    public static <A,B> ValueView<T2<A,B>> join (final ValueView<A> a, final ValueView<B> b) {
        return new MappedValue<T2<A,B>>() {
            @Override public T2<A,B> get () {
                return _current;
            }
            @Override protected Connection connect () {
                return Connection.join(a.connect(_trigger), b.connect(_trigger));
            }
            protected final UnitSlot _trigger = new UnitSlot() {
                public void onEmit () {
                    T2<A,B> ovalue = _current;
                    _current = new T2<A,B>(a.get(), b.get());
                    notifyChange(_current, ovalue);
                }
            };
            protected T2<A,B> _current = new T2<A,B>(a.get(), b.get());
        };
    }

    /**
     * Returns a reactive value which is triggered when either of {@code a, b, c} emits an event.
     * The mapped value will retain connections to {@code a+b+c} only while it itself has
     * connections.
     */
    public static <A,B,C> ValueView<T3<A,B,C>> join (final ValueView<A> a, final ValueView<B> b,
                                                     final ValueView<C> c) {
        return new MappedValue<T3<A,B,C>>() {
            @Override public T3<A,B,C> get () {
                return _current;
            }
            @Override protected Connection connect () {
                return Connection.join(
                    a.connect(_trigger), b.connect(_trigger), c.connect(_trigger));
            }
            protected final UnitSlot _trigger = new UnitSlot() {
                public void onEmit () {
                    T3<A,B,C> ovalue = _current;
                    _current = new T3<A,B,C>(a.get(), b.get(), c.get());
                    notifyChange(_current, ovalue);
                }
            };
            protected T3<A,B,C> _current = new T3<A,B,C>(a.get(), b.get(), c.get());
        };
    }

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
    public static ValueView<Boolean> and (ValueView<Boolean> one, ValueView<Boolean> two) {
        return and(Arrays.asList(one, two));
    }

    /**
     * Returns a value which is the logical AND of the supplied values.
     */
    @SafeVarargs @SuppressWarnings("varargs")
    public static ValueView<Boolean> and (ValueView<Boolean>... values) {
        return and(Arrays.asList(values));
    }

    /**
     * Returns a value which is the logical AND of the supplied values.
     */
    public static ValueView<Boolean> and (final Collection<? extends ValueView<Boolean>> values) {
        return aggValue(values, COMPUTE_AND);
    }

    /**
     * Returns a value which is the logical OR of the supplied values.
     */
    public static ValueView<Boolean> or (ValueView<Boolean> one, ValueView<Boolean> two) {
        return or(Arrays.asList(one, two));
    }

    /**
     * Returns a value which is the logical OR of the supplied values.
     */
    @SafeVarargs @SuppressWarnings("varargs")
    public static ValueView<Boolean> or (ValueView<Boolean>... values) {
        return or(Arrays.asList(values));
    }

    /**
     * Returns a value which is the logical OR of the supplied values.
     */
    public static ValueView<Boolean> or (final Collection<? extends ValueView<Boolean>> values) {
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
        final Collection<? extends ValueView<Boolean>> values,
        final Function<Iterable<? extends ValueView<Boolean>>,Boolean> aggOp) {

        return new MappedValue<Boolean>() {
            @Override public Boolean get () {
                return aggOp.apply(values);
            }

            @Override protected Connection connect () {
                Connection[] conns = new Connection[values.size()];
                Iterator<? extends ValueView<Boolean>> iter = values.iterator();
                for (int ii = 0; ii < conns.length; ii++) conns[ii] = iter.next().connect(_trigger);
                return Connection.join(conns);
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

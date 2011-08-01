//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package com.threerings.react;

import java.util.Set;

/**
 * Handles the machinery of connecting listeners to a value and notifying them, without exposing a
 * public interface for updating the value. This can be used by libraries which wish to provide
 * observable values, but must manage the distribution of value updates themselves (so that they
 * may send them over the network, for example).
 */
public class AbstractValue<T> implements ValueView<T>
{
    @Override public T get () {
        return _value;
    }

    @Override public Connection listen (Listener<T> listener) {
        Cons cons = new Cons(listener);
        if (isDispatching()) {
            cons.next = _toAdd;
            _toAdd = cons;
        } else {
            _listeners = Cons.insert(_listeners, cons);
        }
        return cons;
    }

    @Override public SignalView<T> asSignal () {
        return new SignalView<T>() {
            public Connection connect (final Slot<T> slot) {
                return listen(new Listener<T>() {
                    public void onChange (T value) {
                        slot.onEmit(value);
                    }
                    public int priority () {
                        return slot.priority();
                    }
                });
            }
        };
    }

    @Override public int hashCode () {
        return (_value == null) ? 0 : _value.hashCode();
    }

    @Override public boolean equals (Object other) {
        if (other == null) return false;
        if (other.getClass() != getClass()) return false;
        @SuppressWarnings("unchecked") T ovalue = ((AbstractValue<T>)other).get();
        return (_value == ovalue || (ovalue != null && ovalue.equals(_value)));
    }

    @Override public String toString () {
        return getClass().getSimpleName() + "(" + _value + ")";
    }

    /**
     * Updates the value contained in this instance iff said value is not equal to the value
     * already contained in this instance, as defined by {@link Object#equals} (accounting for
     * nulls).
     */
    protected T updateAndNotifyIf (T value) {
        if (value == _value || (value != null && value.equals(_value))) return value;
        return updateAndNotify(value);
    }

    /**
     * Updates the value contained in this instance and notifies registered listeners.
     * @return the previously contained value.
     */
    protected T updateAndNotify (T value) {
        // first update our value
        T ovalue = _value;
        _value = value;

        // next note that we're dispatching
        Cons lners = _listeners;
        _listeners = (Cons)DISPATCHING;

        // now dispatch to all existing listeners
        Cons cons = lners;
        while (cons != null) {
            cons.listener.onChange(value, ovalue);
            if (cons.oneShot) cons.disconnect();
            cons = cons.next;
        }

        // note that we're no longer dispatching
        _listeners = lners;

        // now remove listeners any queued for removing and add any queued for adding
        if (_toRemove != null) {
            _listeners = Cons.removeAll(_listeners, _toRemove);
            _toRemove = null;
        }
        if (_toAdd != null) {
            _listeners = Cons.insertAll(_listeners, _toAdd);
            _toAdd = null;
        }

        // TODO: notify listeners
        return ovalue;
    }

    protected final boolean isDispatching () {
        return _listeners == DISPATCHING;
    }

    protected class Cons extends AbstractConnection<Cons> {
        public final Listener<T> listener;

        public Cons (Listener<T> listener) {
            this.listener = listener;
        }

        @Override public void disconnect () {
            if (isDispatching()) {
                _toRemove = Cons.queueRemove(_toRemove, this);
            } else {
                _listeners = Cons.remove(_listeners, this);
            }
        }

        @Override public int priority () {
            return listener.priority();
        }
    }

    protected Cons _listeners;
    protected Cons _toAdd;
    protected Set<Cons> _toRemove;

    protected T _value;

    protected static final Object DISPATCHING = new AbstractValue<Void>().new Cons(null);
}

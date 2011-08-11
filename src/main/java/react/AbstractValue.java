//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import java.util.Set;

/**
 * Handles the machinery of connecting listeners to a value and notifying them, without exposing a
 * public interface for updating the value. This can be used by libraries which wish to provide
 * observable values, but must manage the maintenance and distribution of value updates themselves
 * (so that they may send them over the network, for example).
 */
public abstract class AbstractValue<T> extends Reactor<ValueView.Listener<T>>
    implements ValueView<T>
{
    /**
     * Creates a value that maps this value via a function. Every time this value is updated the
     * mapped value will be updated, regardless of whether or not the mapped value differs.
     */
    public <M> ValueView<M> map (final Function<? super T, M> func) {
        final AbstractValue<T> outer = this;
        final AbstractValue<M> mapped = new AbstractValue<M>() {
            @Override public M get () {
                return func.apply(outer.get());
            }
            // we don't track internal state
            @Override protected void updateLocal (M value) {}
        };
        listen(new Listener<T>() {
            @Override public void onChange (T value) {
                mapped.updateAndNotify(func.apply(value));
            }
        });
        return mapped;
    }

    @Override public Connection listen (Listener<? super T> listener) {
        // alas, Java does not support higher kinded types; this cast is safe
        @SuppressWarnings("unchecked") Listener<T> casted = (Listener<T>)listener;
        return addConnection(casted);
    }

    @Override public Connection connect (final Slot<? super T> slot) {
        return listen(new Listener<T>() {
            public void onChange (T value) {
                slot.onEmit(value);
            }
            public int priority () {
                return slot.priority();
            }
        });
    }

    @Override public Connection connectNotify (Slot<? super T> slot) {
        Connection c = connect(slot);
        slot.onEmit(get());
        return c;
    }

    @Override public Connection listenNotify (Listener<? super T> listener) {
        Connection c = listen(listener);
        listener.onChange(get(), null);
        return c;
    }

    @Override public int hashCode () {
        T value = get();
        return (value == null) ? 0 : value.hashCode();
    }

    @Override public boolean equals (Object other) {
        if (other == null) return false;
        if (other.getClass() != getClass()) return false;
        T value = get();
        @SuppressWarnings("unchecked") T ovalue = ((AbstractValue<T>)other).get();
        return (value == ovalue || (ovalue != null && ovalue.equals(value)));
    }

    @Override public String toString () {
        return getClass().getSimpleName() + "(" + get() + ")";
    }

    /**
     * Updates the value contained in this instance iff said value is not equal to the value
     * already contained in this instance, as defined by {@link Object#equals} (accounting for
     * nulls).
     */
    protected T updateAndNotifyIf (T value) {
        T ovalue = get();
        if (value == ovalue || (value != null && value.equals(ovalue))) return value;
        return updateAndNotify(value);
    }

    /**
     * Updates the value contained in this instance and notifies registered listeners.
     * @return the previously contained value.
     */
    protected T updateAndNotify (T value) {
        return updateAndNotify(value, get());
    }

    /**
     * Updates the value contained in this instance and notifies registered listeners.
     * @return the previously contained value.
     */
    protected T updateAndNotify (T value, T ovalue) {
        checkMutate();
        updateLocal(value);
        notifyChange(value, ovalue);
        return ovalue;
    }

    /**
     * Notifies our listeners of a value change.
     */
    protected void notifyChange (T value, T ovalue) {
        Cons<ValueView.Listener<T>> lners = prepareNotify();
        try {
            for (Cons<ValueView.Listener<T>> cons = lners; cons != null; cons = cons.next) {
                cons.listener.onChange(value, ovalue);
                if (cons.oneShot) cons.disconnect();
            }
        } finally {
            finishNotify(lners);
        }
    }

    protected abstract void updateLocal (T value);
}

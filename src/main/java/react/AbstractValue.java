//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * Handles the machinery of connecting listeners to a value and notifying them, without exposing a
 * public interface for updating the value. This can be used by libraries which wish to provide
 * observable values, but must manage the maintenance and distribution of value updates themselves
 * (so that they may send them over the network, for example).
 */
public abstract class AbstractValue<T> extends Reactor<ValueView.Listener<T>>
    implements ValueView<T>
{
    @Override public <M> ValueView<M> map (final Function<? super T, M> func) {
        final AbstractValue<T> outer = this;
        return new MappedValue<M>() {
            @Override public M get () {
                return func.apply(outer.get());
            }
            @Override protected Connection connect () {
                return outer.connect(new Listener<T>() {
                    @Override public void onChange (T value, T ovalue) {
                        notifyChange(func.apply(value), func.apply(ovalue));
                    }
                });
            }
        };
    }

    @Override public Connection connect (Listener<? super T> listener) {
        // alas, Java does not support higher kinded types; this cast is safe
        @SuppressWarnings("unchecked") Listener<T> casted = (Listener<T>)listener;
        return addConnection(casted);
    }

    @Override public Connection connectWeak (Listener<? super T> listener) {
        @SuppressWarnings("unchecked") Listener<T> casted = (Listener<T>)listener;
        return addConnectionWeak(casted);
    }

    @Override public Connection connectNotify (Listener<? super T> listener) {
        // connect before calling emit; if the listener changes the value in the body of onEmit, it
        // will expect to be notified of that change; however if onEmit throws a runtime exception,
        // we need to take care of disconnecting the listener because the returned connection
        // instance will never reach the caller
        Connection conn = connect(listener);
        try {
            listener.onChange(get(), null);
            return conn;
        } catch (RuntimeException re) {
            conn.disconnect();
            throw re;
        } catch (Error e) {
            conn.disconnect();
            throw e;
        }
    }

    @Override public void disconnect (Listener<? super T> listener) {
        // alas, Java does not support higher kinded types; this cast is safe
        @SuppressWarnings("unchecked") Listener<T> casted = (Listener<T>)listener;
        removeConnection(casted);
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
        return areEqual(value, ovalue);
    }

    @Override public String toString () {
        String cname = getClass().getName();
        return cname.substring(cname.lastIndexOf(".")+1) + "(" + get() + ")";
    }

    @Override Listener<T> placeholderListener () {
        @SuppressWarnings("unchecked") Listener<T> p = (Listener<T>)Slots.NOOP;
        return p;
    }

    /**
     * Updates the value contained in this instance and notifies registered listeners iff said
     * value is not equal to the value already contained in this instance (per {@link #areEqual}).
     */
    protected T updateAndNotifyIf (T value) {
        return updateAndNotify(value, false);
    }

    /**
     * Updates the value contained in this instance and notifies registered listeners.
     * @return the previously contained value.
     */
    protected T updateAndNotify (T value) {
        return updateAndNotify(value, true);
    }

    /**
     * Updates the value contained in this instance and notifies registered listeners.
     * @param force if true, the listeners will always be notified, if false the will be notified
     * only if the new value is not equal to the old value (per {@link #areEqual}).
     * @return the previously contained value.
     */
    protected T updateAndNotify (T value, boolean force) {
        checkMutate();
        T ovalue = updateLocal(value);
        if (force || !areEqual(value, ovalue)) {
            emitChange(value, ovalue);
        }
        return ovalue;
    }

    /**
     * Emits a change notification. Default implementation immediately notifies listeners.
     */
    protected void emitChange (T value, T ovalue) {
        notifyChange(value, ovalue);
    }

    /**
     * Notifies our listeners of a value change.
     */
    protected void notifyChange (T value, T ovalue) {
        Cons<Listener<T>> lners = prepareNotify();
        MultiFailureException error = null;
        try {
            for (Cons<Listener<T>> cons = lners; cons != null; cons = cons.next) {
                try {
                    cons.listener().onChange(value, ovalue);
                } catch (Throwable t) {
                    if (error == null) error = new MultiFailureException();
                    error.addFailure(t);
                }
                if (cons.oneShot) cons.disconnect();
            }
        } finally {
            finishNotify(lners);
        }
        if (error != null) error.trigger();
    }

    /**
     * Updates our locally stored value. Default implementation throws unsupported operation.
     * @return the previously stored value.
     */
    protected T updateLocal (T value) {
        throw new UnsupportedOperationException();
    }
}

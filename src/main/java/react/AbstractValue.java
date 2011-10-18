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
    @Override public AsSignalView<T> asSignal () {
        return new AsSignalView<T>() {
            @Override public <M> MappedSignalView<M> map (final Function<? super T, M> func) {
                final MappedSignal<M> mapped = new MappedSignal<M>();
                mapped.setConnection(AbstractValue.this.connect(new Listener<T>() {
                    @Override public void onChange (T value, T ovalue) {
                        mapped.notifyEmit(func.apply(value));
                    }
                }));
                return mapped;
            }

            @Override public Connection connect (final Slot<? super T> slot) {
                return addWrappedListener(slot, new Listener<T>() {
                    public void onChange (T value) {
                        slot.onEmit(value);
                    }
                    public int priority () {
                        return slot.priority();
                    }
                });
            }

            @Override public Connection connectNotify (Slot<? super T> slot) {
                // connect before calling emit; if the slot changes the value in the body of
                // onEmit, it will expect to be notified of that change; however if onEmit throws a
                // runtime exception, we need to take care of disconnecting the slot because the
                // returned connection instance will never reach the caller
                Connection conn = connect(slot);
                try {
                    slot.onEmit(get());
                    return conn;
                } catch (RuntimeException re) {
                    conn.disconnect();
                    throw re;
                } catch (Error e) {
                    conn.disconnect();
                    throw e;
                }
            }

            @Override public void disconnect (Slot<? super T> slot) {
                // alas, Java does not support higher kinded types; this cast is safe
                @SuppressWarnings("unchecked") Slot<T> casted = (Slot<T>)slot;
                removeConnection(casted);
            }
        };
    }

    @Override public <M> MappedValueView<M> map (final Function<? super T, M> func) {
        final AbstractValue<T> outer = this;
        final MappedValue<M> mapped = new MappedValue<M>() {
            @Override public M get () {
                return func.apply(outer.get());
            }
        };
        mapped.setConnection(connect(new Listener<T>() {
            @Override public void onChange (T value, T ovalue) {
                mapped.notifyChange(func.apply(value), func.apply(ovalue));
            }
        }));
        return mapped;
    }

    @Override public Connection connect (Listener<? super T> listener) {
        // alas, Java does not support higher kinded types; this cast is safe
        @SuppressWarnings("unchecked") Listener<T> casted = (Listener<T>)listener;
        return addConnection(casted);
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

    /**
     * Updates the value contained in this instance and notifies registered listeners iff said
     * value is not equal to the value already contained in this instance (per {@link #areEqual}).
     */
    protected T updateAndNotifyIf (T value) {
        return updateAndNotify(value, get(), false);
    }

    /**
     * Updates the value contained in this instance and notifies registered listeners.
     * @return the previously contained value.
     */
    protected T updateAndNotify (T value) {
        return updateAndNotify(value, get(), true);
    }

    /**
     * Updates the value contained in this instance and notifies registered listeners.
     * @param force if true, the listeners will always be notified, if false the will be notified
     * only if the new value is not equal to the old value (per {@link #areEqual}).
     * @return the previously contained value.
     */
    protected T updateAndNotify (T value, T ovalue, boolean force) {
        checkMutate();
        updateLocal(value);
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
        Cons<ValueView.Listener<T>> lners = prepareNotify();
        MultiFailureException error = null;
        try {
            for (Cons<ValueView.Listener<T>> cons = lners; cons != null; cons = cons.next) {
                try {
                    cons.receiver.onChange(value, ovalue);
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

//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * A view of a {@link Value}, to which listeners may be added, but which one cannot update. This
 * can be used in combination with {@link AbstractValue} to provide {@link Value} semantics to an
 * entity which dispatches value changes in a custom manner (like over the network). Value
 * consumers should require only a view on a value, rather than a concrete value.
 */
public interface ValueView<T> extends SignalView<T>
{
    /**
     * Used to observe changes to a value. One must override only one of the {@link #onChange}
     * methods, depending on how much information is desired.
     */
    abstract class Listener<T> extends Reactor.RListener {
        /**
         * Called when the value to which this listener is bound has changed. This method will call
         * the old-value-forgetting version ({@link #onChange(T)}) by default.
         */
        public void onChange (T value, T oldValue) {
            onChange(value);
        }

        /**
         * Called when the value to which this listener is bound has changed, unless the less
         * forgetful {@link #onChange(T,T)} has been overridden. This method will call the
         * all-values-forgetting version ({@link #onChange()}) by default.
         */
        public void onChange (T value) {
            onChange();
        }

        /**
         * Called when the value to which this listener is bound has changed, unless one of the
         * less forgetful methods has been overridden.
         */
        public void onChange () {
            // noop
        }
    }

    /**
     * Returns the current value.
     */
    T get ();

    /**
     * Connects the supplied listener to this value, such that it will be notified when this value
     * changes.
     * @return a connection instance which can be used to cancel the connection.
     */
    Connection listen (Listener<? super T> listener);

    /**
     * Connects this signal to the supplied value, such that when the value changes, the slot will
     * be notified. Also immediately notifies the slot of the current value.
     * @return a connection instance which can be used to cancel the connection.
     */
    Connection connectNotify (Slot<? super T> slot);

    /**
     * Connects the supplied listener to this value, such that it will be notified when this value
     * changes. Also immediately notifies the listener of the current value. Note that the previous
     * value supplied with this notification will be null.
     * @return a connection instance which can be used to cancel the connection.
     */
    Connection listenNotify (Listener<? super T> listener);
}

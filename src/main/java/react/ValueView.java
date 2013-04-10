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
public interface ValueView<T>
{
    /**
     * Used to observe changes to a value. One must override only one of the {@link #onChange}
     * methods, depending on how much information is desired.
     */
    abstract class Listener<T> extends Reactor.RListener {
        /**
         * Called when the value to which this listener is bound has changed.
         */
        public abstract void onChange (T value, T oldValue);
    }

    /**
     * Returns the current value.
     */
    T get ();

    /**
     * Creates a value that maps this value via a function. When this value changes, the mapped
     * listeners will be notified, regardless of whether the new and old mapped values differ. The
     * mapped value will retain a connection to this value for as long as it has connections of its
     * own.
     */
    <M> ValueView<M> map (final Function<? super T, M> func);

    /**
     * Connects the supplied listener to this value, such that it will be notified when this value
     * changes. The listener is held by a strong reference, so it's held in memory by virtue of
     * being connected.
     * @return a connection instance which can be used to cancel the connection.
     */
    Connection connect (Listener<? super T> listener);

    /**
     * Connects the supplied listener to this value, such that it will be notified when this value
     * changes. The listener is only held by a weak reference, so it only remains in memory and
     * connected as long as it's referenced elsewhere.
     *
     * <p><em>NOTE:</em> weak references are not supported in JavaScript. When using this library
     * in GWT, this will result in a strong connection.</p>
     *
     * @return a connection instance which can be used to cancel the connection.
     */
    Connection connectWeak (Listener<? super T> listener);

    /**
     * Connects the supplied listener to this value, such that it will be notified when this value
     * changes. Also immediately notifies the listener of the current value. Note that the previous
     * value supplied with this notification will be null. If the notification triggers an
     * unchecked exception, the slot will automatically be disconnected and the caller need not
     * worry about cleaning up after itself.
     * @return a connection instance which can be used to cancel the connection.
     */
    Connection connectNotify (Listener<? super T> listener);

    /**
     * Disconnects the supplied listener from this value if it's connected. If the listener has been
     * connected multiple times, all connections are cancelled.
     */
    void disconnect (Listener<? super T> listener);
}

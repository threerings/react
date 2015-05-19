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
    /** Used to observe changes to a value. */
    interface Listener<T> extends Reactor.RListener {
        /** Called when the value to which this listener is bound has changed. */
        void onChange (T value, T oldValue);
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

    // these methods exist only to let javac know that it can synthesize a SignalView.Listener
    // instance from a single argument lambda; otherwise they are unnecessary because
    // SignalView.Listener is a subtype of ValueView.Listener

    /**
     * Connects the supplied listener to this value, such that it will be notified when this value
     * changes. The listener is held by a strong reference, so it's held in memory by virtue of
     * being connected.
     * @return a connection instance which can be used to cancel the connection.
     */
    Connection connect (SignalView.Listener<? super T> listener);

    /**
     * Connects the supplied listener to this value, such that it will be notified when this value
     * changes. Also immediately notifies the listener of the current value. If the notification
     * triggers an unchecked exception, the slot will automatically be disconnected and the caller
     * need not worry about cleaning up after itself.
     * @return a connection instance which can be used to cancel the connection.
     */
    Connection connectNotify (SignalView.Listener<? super T> listener);

    // these methods exist to help javac disambiguate between the above two methods, yay
    // TODO: when we drop support for java 1.7, we can remove these methods

    /**
     * Connects the supplied listener to this value, such that it will be notified when this value
     * changes. The listener is held by a strong reference, so it's held in memory by virtue of
     * being connected.
     * @return a connection instance which can be used to cancel the connection.
     */
    Connection connect (Slot<? super T> listener);

    /**
     * Connects the supplied listener to this value, such that it will be notified when this value
     * changes. Also immediately notifies the listener of the current value. If the notification
     * triggers an unchecked exception, the slot will automatically be disconnected and the caller
     * need not worry about cleaning up after itself.
     * @return a connection instance which can be used to cancel the connection.
     */
    Connection connectNotify (Slot<? super T> listener);
}

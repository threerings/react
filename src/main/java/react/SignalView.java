//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * A view of a {@link Signal}, on which slots may listen, but to which one cannot emit events. This
 * is generally used to provide signal-like views of changing entities. See {@link AbstractValue}
 * for an example.
 */
public interface SignalView<T>
{
    /**
     * Creates a signal that maps this signal via a function. When this signal emits a value, the
     * mapped signal will emit that value as transformed by the supplied function. The mapped value
     * will retain a connection to this signal for as long as it has connections of its own.
     */
    <M> SignalView<M> map (final Function<? super T, M> func);

    /**
     * Connects this signal to the supplied slot, such that when an event is emitted from this
     * signal, the slot will be notified.
     *
     * @return a connection instance which can be used to cancel the connection.
     */
    Connection connect (Slot<? super T> slot);

    /**
     * Connects this signal to the supplied slot, such that when an event is emitted from this
     * signal, the slot will be notified. The slot is only held by a weak reference, so it only
     * remains in memory and connected as long as it's referenced elsewhere.
     *
     * <p><em>NOTE:</em> weak references are not supported in JavaScript. When using this library
     * in GWT, this will result in a strong connection.</p>
     *
     * @return a connection instance which can be used to cancel the connection.
     */
    Connection connectWeak (Slot<? super T> slot);

    /**
     * Disconnects the supplied slot from this signal if connect was called with it. If the slot has
     * been connected multiple times, all connections are cancelled.
     */
    void disconnect (Slot<? super T> slot);
}

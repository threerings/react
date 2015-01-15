//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * Provides a mechanism to cancel a slot or listener registration, or to perform post-registration
 * adjustment like making the registration single-shot.
 */
public interface Connection
{
    /**
     * Disconnects this registration. Subsequent events will not be dispatched to the associated
     * slot or listener.
     */
    void disconnect ();

    /**
     * Converts this connection into a one-shot connection. After the first time the slot or
     * listener is notified, it will automatically be disconnected.
     *
     * <p><em>NOTE:</em> if you are dispatching signals in a multithreaded environment, it is
     * possible for your connected listener to be notified before this call has a chance to mark it
     * as one-shot. Thus you could receive multiple notifications. If you require this to be
     * avoided, you must synchronize on the signal/value/etc. on which you are adding a
     * listener:</p>
     *
     * <pre>{@code
     * Signal<Foo> signal = ...;
     * Connection conn;
     * synchronized (signal) {
     *   conn = signal.connect(slot).once();
     * }
     * }</pre>
     *
     * @return this connection instance for convenient chaining.
     */
    Connection once ();

    /**
     * Changes the priority of this connection to the specified value. Connections are notified from
     * highest priority to lowest priority. The default priority is zero.
     *
     * <p>This should generally be done simultaneously with creating a connection. For example:</p>
     *
     * <pre>{@code
     * Signal<Foo> signal = ...;
     * Connection conn = signal.connect(new Slot<Foo>() { ... }).atPrio(5);
     * }</pre>
     *
     * <p><em>NOTE:</em> if you are dispatching signals in a multithreaded environment, it is
     * possible for your connected listener to be notified at priority zero before this call has a
     * chance to update its priority. If you require this to be avoided, you must synchronize on
     * the signal/value/etc. on which you are adding a listener:</p>
     *
     * <pre>{@code
     * Signal<Foo> signal = ...;
     * Connection conn;
     * synchronized (signal) {
     *   conn = signal.connect(slot).atPrio(5);
     * }
     * }</pre>
     *
     * @return this connection instance for convenient chaining.
     */
    Connection atPrio (int priority);

    /**
     * Changes this connection to one held by a weak reference. It only remains connected as long
     * as its target listener is referenced elsewhere.
     *
     * <p><em>NOTE:</em> weak references are not supported in JavaScript. When using this library
     * in GWT, the reference remains strong.</p>
     *
     * @return this connection instance for convenient chaining.
     */
    Connection holdWeakly ();
}

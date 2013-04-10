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
     * @return this connection instance for convenient chaining.
     */
    Connection once ();

    /**
     * Changes the priority of this connection to the specified value. This should generally be
     * done simultaneously with creating a connection. For example:
     *
     * <pre>{@code
     * Signal<Foo> signal = ...;
     * Connection conn = signal.connect(new Slot<Foo>() { ... }).atPriority(5);
     * }</pre></p>
     *
     * @return this connection instance for convenient chaining.
     */
    Connection atPriority (int priority);

    /**
     * Changes the listener to be held by a weak reference, so it only remains in memory and
     * connected only as long as it's referenced elsewhere.
     *
     * <p><em>NOTE:</em> weak references are not supported in JavaScript. When using this library
     * in GWT, the reference remains strong.</p>
     *
     * @return this connection instance for convenient chaining.
     */
    Connection holdWeakly();
}

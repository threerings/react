//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * Provides a mechanism to cancel a slot or listener registration, or to perform post-registration
 * adjustment like making the registration single-shot.
 */
public abstract class Connection
{
    private static abstract class Wrapper extends Connection {
        public Connection once () { throw new UnsupportedOperationException(); }
        public Connection atPrio (int prio) { throw new UnsupportedOperationException(); }
        public Connection holdWeakly () { throw new UnsupportedOperationException(); }
    }

    /** A connection which no-ops on {@link #disconnect} and throws an exception for all other
      * methods. This is for the following code pattern:
      *
      * <pre>{@code
      * Connection _conn = Connection.NOOP;
      * void open () {
      *    _conn = whatever.connect(...);
      * }
      * void close () {
      *    _conn.disconnect();
      *    _conn = Connection.NOOP;
      * }
      * }</pre>
      *
      * In that it allows {@code close} to avoid a null check if it's possible for {@code close} to
      * be called with no call to {@code open} or repeatedly.
      */
    public static final Connection NOOP = new Wrapper() {
        public void disconnect() {} // noop!
    };

    /**
     * Creates a connection that disconnects multiple connections at once.
     */
    public static Connection join (final Connection... cons) {
        return new Wrapper() {
            @Override public void disconnect () {
                for (int ii = 0; ii < cons.length; ii++) {
                    if (cons[ii] == null) continue;
                    cons[ii].disconnect();
                    cons[ii] = null;
                }
            }
        };
    }

    /**
     * Disconnects this registration. Subsequent events will not be dispatched to the associated
     * slot or listener.
     */
    public abstract void disconnect ();

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
    public abstract Connection once ();

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
    public abstract Connection atPrio (int priority);

    /**
     * Changes this connection to one held by a weak reference. It only remains connected as long
     * as its target listener is referenced elsewhere.
     *
     * <p><em>NOTE:</em> weak references are not supported in JavaScript. When using this library
     * in GWT, the reference remains strong.</p>
     *
     * @return this connection instance for convenient chaining.
     */
    public abstract Connection holdWeakly ();
}

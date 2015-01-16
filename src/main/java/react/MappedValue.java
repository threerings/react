//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * Plumbing to implement mapped values in such a way that they automatically manage a connection to
 * their underlying value. When the mapped value adds its first connection, it establishes a
 * connection to the underlying value, and when it removes its last connection it clears its
 * connection from the underlying value.
 */
abstract class MappedValue<T> extends AbstractValue<T>
{
    /**
     * Establishes a connection to our source value. Called when go from zero to one listeners.
     * When we go from one to zero listeners, the connection will automatically be cleared.
     *
     * @return the newly established connection.
     */
    protected abstract Connection connect ();

    @Override
    protected void connectionAdded () {
        super.connectionAdded();
        if (_conn == null) _conn = connect();
    }

    @Override
    protected void connectionRemoved () {
        super.connectionRemoved();
        if (!hasConnections() && _conn != null) {
            _conn.close();
            _conn = null;
        }
    }

    protected Connection _conn;
}

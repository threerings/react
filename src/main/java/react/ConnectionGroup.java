//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import java.util.HashSet;
import java.util.Set;

/**
 * Collects connections to allow mass operations on them.
 */
public class ConnectionGroup implements Closeable
{
    /**
     * Closes all connections in this group.
     */
    public void close () {
        for (Closeable c : _connections) c.close();
        _connections.clear();
    }

    /**
     * Adds the supplied connection to this group.
     * @return the supplied connection.
     */
    public Closeable add (Closeable c) {
        _connections.add(c);
        return c;
    }

    /**
     * Removes a connection from this group while leaving its connected status unchanged.
     */
    public void remove (Closeable c) {
        _connections.remove(c);
    }

    protected Set<Closeable> _connections = new HashSet<Closeable>();
}

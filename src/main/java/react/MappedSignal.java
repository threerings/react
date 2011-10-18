//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * Plumbing to implement mapped signals in such a way that they can expose the {@link Connection}
 * that wires them up.
 */
class MappedSignal<T> extends AbstractSignal<T> implements MappedSignalView<T>
{
    @Override public Connection connection () {
        return _conn;
    }

    protected void setConnection (Connection conn) {
        _conn = conn;
    }

    protected Connection _conn;
}

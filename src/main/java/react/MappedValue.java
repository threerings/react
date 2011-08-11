//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * More plumbing to implement mapped values in such a way that they can expose the {@link
 * Connection} that wires them up.
 */
abstract class MappedValue<T> extends AbstractValue<T> implements MappedValueView<T>
{
    @Override public Connection connection () {
        return _conn;
    }

    protected void setConnection (Connection conn) {
        _conn = conn;
    }

    protected Connection _conn;
}

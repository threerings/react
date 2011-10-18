//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * An interface implemented by {@link SignalView}s that represent a mapping of some underlying
 * model, and which necessarily require the use of a {@link Connection} under the hood. This
 * interface allows the connection to be accessed so that it may be cleared if desired.
 */
public interface MappedSignalView<T> extends SignalView<T>
{
    /** Returns the connection that maintains this mapped signal. */
    Connection connection ();
}

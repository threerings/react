//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * Used to observe changes to a value. One must override only one of the {@link #onChange} methods,
 * depending on how much information is desired.
 */
public abstract class Listener<T> extends Reactor.RListener
{
    /**
     * Called when the value to which this listener is bound has changed.
     */
    public abstract void onChange (T value, T oldValue);
}

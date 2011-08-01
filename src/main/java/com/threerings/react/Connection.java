//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package com.threerings.react;

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
     * @return this connection instance for convenient chaining.
     */
    Connection once ();
}

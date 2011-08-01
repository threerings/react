//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package com.threerings.react;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Tests aspects of the {@link Value} class.
 */
public class ValueTest
{
    @Test public void testSimpleListener () {
        Value<Integer> value = Value.create(42);
        final boolean[] fired = new boolean[] { false };
        value.listen(new Value.Listener<Integer>() {
            public void onChange (Integer nvalue, Integer ovalue) {
                assertEquals(42, ovalue.intValue());
                assertEquals(15, nvalue.intValue());
                fired[0] = true;
            }
        });
        value.update(15);
        assertEquals(15, value.get().intValue());
        assertTrue(fired[0]);
    }

    @Test public void testAsSignal () {
        Value<Integer> value = Value.create(42);
        final boolean[] fired = new boolean[] { false };
        value.asSignal().connect(new Slot<Integer>() {
            public void onEmit (Integer value) {
                assertEquals(15, value.intValue());
                fired[0] = true;
            }
        });
        value.update(15);
        assertTrue(fired[0]);
    }

    @Test public void testAsOnceSignal () {
        Value<Integer> value = Value.create(42);
        final int[] fired = new int[] { 0 };
        value.asSignal().connect(new Slot<Integer>() {
            public void onEmit (Integer value) {
                fired[0]++;
            }
        }).once();
        value.update(15);
        value.update(42);
        assertEquals(1, fired[0]);
    }
}

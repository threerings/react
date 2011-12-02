//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Tests aspects of the {@link Values} class.
 */
public class ValuesTest
{
    @Test public void testAggregateValue () {
        Value<Boolean> a = Value.create(false);
        Value<Boolean> b = Value.create(true);
        Value<Boolean> c = Value.create(false);
        final boolean[] fired = new boolean[] { false };

        MappedValueView<Boolean> anded = Values.and(a, b, c);
        assertFalse(anded.get());
        anded.connect(new Value.Listener<Boolean>() {
            public void onChange (Boolean value, Boolean oldValue) {
                assertFalse(value);
                assertFalse(oldValue);
                fired[0] = true;
            }
        }).once();
        a.update(true);
        assertTrue(fired[0]);
        fired[0] = false;

        anded.connect(new Value.Listener<Boolean>() {
            public void onChange (Boolean value, Boolean oldValue) {
                assertTrue(value);
                assertFalse(oldValue);
                fired[0] = true;
            }
        }).once();
        c.update(true);
        assertTrue(anded.get());
        assertTrue(fired[0]);
        fired[0] = false;

        anded.connect(new Value.Listener<Boolean>() {
            public void onChange (Boolean value, Boolean oldValue) {
                assertFalse(value);
                assertTrue(oldValue);
                fired[0] = true;
            }
        }).once();
        b.update(false);
        assertFalse(anded.get());
        assertTrue(fired[0]);
        fired[0] = false;
    }

    @Test public void testSignalAsValue () {
        Signal<Integer> intsig = Signal.create();
        MappedValueView<Integer> intval = Values.asValue(intsig, 15);
        assertEquals(15, intval.get().intValue());

        final boolean[] fired = new boolean[] { false };
        intval.connect(new Value.Listener<Integer>() {
            public void onChange (Integer value, Integer ovalue) {
                assertEquals(25, value.intValue());
                assertEquals(15, ovalue.intValue());
                fired[0] = true;
            }
        });

        intsig.emit(25);
        assertEquals(25, intval.get().intValue());
        assertTrue(fired[0]);

        // disconnect the internal listener and make sure no updates happen
        intval.connection().disconnect();
        fired[0] = false;
        intsig.emit(15);
        assertEquals(25, intval.get().intValue());
        assertFalse(fired[0]);
    }
}

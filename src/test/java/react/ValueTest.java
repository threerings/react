//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Tests aspects of the {@link Value} class.
 */
public class ValueTest
{
    public static class Counter extends Value.Listener<Object> {
        public int notifies;
        @Override public void onChange () {
            notifies++;
        }
    }

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
        assertEquals(42, value.update(15).intValue());
        assertEquals(15, value.get().intValue());
        assertTrue(fired[0]);
    }

    @Test public void testAsSignal () {
        Value<Integer> value = Value.create(42);
        final boolean[] fired = new boolean[] { false };
        value.connect(new Slot<Integer>() {
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
        SignalTest.Counter counter = new SignalTest.Counter();
        value.connect(counter).once();
        value.update(15);
        value.update(42);
        assertEquals(1, counter.notifies);
    }

    @Test public void testMappedValue () {
        Value<Integer> value = Value.create(42);
        MappedValueView<String> mapped = value.map(Functions.TO_STRING);

        Counter counter = new Counter();
        mapped.listen(counter);
        mapped.connect(SignalTest.require("15"));

        value.update(15);
        assertEquals(1, counter.notifies);
        value.update(15);
        assertEquals(1, counter.notifies);
        value.updateForce(15);
        assertEquals(2, counter.notifies);

        // disconnect the mapped value and ensure that it no longer updates
        mapped.connection().disconnect();
        value.update(25);
        assertEquals(2, counter.notifies);
    }

    @Test public void testConnectNotify () {
        Value<Integer> value = Value.create(42);
        final boolean[] fired = new boolean[] { false };
        value.connectNotify(new Slot<Integer>() {
            public void onEmit (Integer value) {
                assertEquals(42, value.intValue());
                fired[0] = true;
            }
        });
        assertTrue(fired[0]);
    }

    @Test public void testListenNotify () {
        Value<Integer> value = Value.create(42);
        final boolean[] fired = new boolean[] { false };
        value.listenNotify(new Value.Listener<Integer>() {
            public void onChange (Integer value) {
                assertEquals(42, value.intValue());
                fired[0] = true;
            }
        });
        assertTrue(fired[0]);
    }
}

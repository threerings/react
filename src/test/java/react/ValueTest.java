//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import org.junit.*;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Tests aspects of the {@link Value} class.
 */
public class ValueTest
{
    @Test public void testSimpleListener () {
        Value<Integer> value = Value.create(42);
        final boolean[] fired = new boolean[] { false };
        value.connect(new Value.Listener<Integer>() {
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
        ValueView<String> mapped = value.map(Functions.TO_STRING);

        SignalTest.Counter counter = new SignalTest.Counter();
        Connection c1 = mapped.connect(counter);
        Connection c2 = mapped.connect(SignalTest.require("15"));

        value.update(15);
        assertEquals(1, counter.notifies);
        value.update(15);
        assertEquals(1, counter.notifies);
        value.updateForce(15);
        assertEquals(2, counter.notifies);

        // disconnect from the mapped value and ensure that it disconnects in turn
        c1.close();
        c2.close();
        assertFalse(value.hasConnections());
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
        value.connectNotify(new Slot<Integer>() {
            public void onEmit (Integer value) {
                assertEquals(42, value.intValue());
                fired[0] = true;
            }
        });
        assertTrue(fired[0]);
    }

    @Test public void testDisconnect () {
        final Value<Integer> value = Value.create(42);
        final int[] expectedValue = { value.get() };
        final int[] fired = { 0 };
        Slot<Integer> listener = new Slot<Integer>() {
            public void onEmit (Integer newValue) {
                assertEquals(expectedValue[0], newValue.intValue());
                fired[0] += 1;
                value.disconnect(this);
            }
        };
        Connection conn = value.connectNotify(listener);
        value.update((expectedValue[0] = 12));
        assertEquals("Disconnecting in listenNotify disconnects", 1, fired[0]);
        conn.close();// Just see what happens when calling disconnect while disconnected

        value.connect(listener);
        value.connect(new SignalTest.Counter());
        value.connect(listener);
        value.update((expectedValue[0] = 13));
        value.update((expectedValue[0] = 14));
        assertEquals("Disconnecting in listen disconnects", 3, fired[0]);

        value.connect(listener).close();
        value.update((expectedValue[0] = 15));
        assertEquals("Disconnecting before geting an update still disconnects", 3, fired[0]);
    }

    @Test public void testSlot () {
        final Value<Integer> value = Value.create(42);
        final int[] expectedValue = { value.get() };
        final int[] fired = { 0 };
        Slot<Integer> listener = new Slot<Integer>() {
            public void onEmit (Integer newValue) {
                assertEquals(expectedValue[0], newValue.intValue());
                fired[0] += 1;
                value.disconnect(this);
            }
        };
        value.connect(listener);
        value.update((expectedValue[0] = 12));
        assertEquals("Calling disconnect with a slot disconnects", 1, fired[0]);

        value.connect(listener).close();
        value.update((expectedValue[0] = 14));
        assertEquals(1, fired[0]);
    }

    @Test public void testWeakListener () {
        final Value<Integer> value = Value.create(42);
        final AtomicInteger fired = new AtomicInteger(0);

        ValueView.Listener<Integer> listener = new ValueView.Listener<Integer>() {
            @Override
            public void onChange(Integer value, Integer oldValue) {
                fired.incrementAndGet();
            }
        };
        System.gc();
        System.gc();
        System.gc();

        Connection conn = value.addConnection(listener);
        value.update(41);
        assertEquals(1, fired.get());
        assertTrue(value.hasConnections());

        // make sure that calling holdWeakly twice doesn't cause weirdness
        conn.holdWeakly();
        value.update(42);
        assertEquals(2, fired.get());
        assertTrue(value.hasConnections());

        // clear out the listener and do our best to convince the JVM to collect it
        listener = null;
        System.gc();
        System.gc();
        System.gc();

        // now check that the listener has been collected and is not notified
        value.update(40);
        assertEquals(2, fired.get());
        assertFalse(value.hasConnections());
    }
}

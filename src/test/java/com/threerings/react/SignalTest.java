//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package com.threerings.react;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Tests basic signals and slots behavior.
 */
public class SignalTest
{
    @Test public void testSignalToSlot () {
        Signal<Integer> signal = Signal.create();
        AccSlot<Integer> slot = new AccSlot<Integer>();
        signal.connect(slot);
        signal.emit(1);
        signal.emit(2);
        signal.emit(3);
        assertEquals(Arrays.asList(1, 2, 3), slot.events);
    }

    @Test public void testOneShotSlot () {
        Signal<Integer> signal = Signal.create();
        AccSlot<Integer> slot = new AccSlot<Integer>();
        signal.connect(slot).once();
        signal.emit(1); // slot should be removed after this emit
        signal.emit(2);
        signal.emit(3);
        assertEquals(Arrays.asList(1), slot.events);
    }

    @Test public void testSlotPriority () {
        final int[] counter = new int[] { 0 };
        class TestSlot extends Slot<Void> {
            public int order;
            public void onEmit () {
                order = ++counter[0];
            }
        }
        TestSlot slot1 = new TestSlot() {
            @Override public int priority () { return 1; }
        };
        TestSlot slot2 = new TestSlot() {
            @Override public int priority () { return 2; }
        };
        TestSlot slot3 = new TestSlot() {
            @Override public int priority () { return 3; }
        };
        TestSlot slot4 = new TestSlot() {
            @Override public int priority () { return 3; }
        };

        UnitSignal signal = new UnitSignal();
        signal.connect(slot3);
        signal.connect(slot1);
        signal.connect(slot2);
        signal.connect(slot4);
        signal.emit();
        assertEquals(1, slot1.order);
        assertEquals(2, slot2.order);
        assertEquals(3, slot3.order);
        assertEquals(4, slot4.order);
    }

    @Test public void testAddDuringDispatch () {
        final Signal<Integer> signal = Signal.create();
        final AccSlot<Integer> toAdd = new AccSlot<Integer>();
        signal.connect(new Slot<Integer>() {
            public void onEmit () {
                signal.connect(toAdd);
            }
        }).once();

        // this will connect our new signal but not dispatch to it
        signal.emit(5);
        assertEquals(0, toAdd.events.size());

        // now dispatch an event that should go to the added signal
        signal.emit(42);
        assertEquals(Arrays.asList(42), toAdd.events);
    }

    @Test public void testRemoveDuringDispatch () {
        final Signal<Integer> signal = Signal.create();
        final AccSlot<Integer> toRemove = new AccSlot<Integer>();
        final Connection rconn = signal.connect(toRemove);

        // dispatch one event and make sure it's received
        signal.emit(5);
        assertEquals(Arrays.asList(5), toRemove.events);

        // now add our removing signal, and dispatch again
        signal.connect(new Slot<Integer>() {
            public void onEmit () {
                rconn.disconnect();
            }
            public int priority () {
                return 1; // ensure that we're before toRemove
            }
        });
        signal.emit(42);
        // since toRemove will have been removed during this dispatch, it will receive the signal
        // in question, even though the higher priority signal triggered first
        assertEquals(Arrays.asList(5, 42), toRemove.events);
        // finally dispatch one more event and make sure toRemove didn't get it
        signal.emit(9);
        assertEquals(Arrays.asList(5, 42), toRemove.events);
    }

    @Test public void testAddAndRemoveDuringDispatch () {
        final Signal<Integer> signal = Signal.create();
        final AccSlot<Integer> toAdd = new AccSlot<Integer>();
        final AccSlot<Integer> toRemove = new AccSlot<Integer>();
        final Connection rconn = signal.connect(toRemove);

        // dispatch one event and make sure it's received by toRemove
        signal.emit(5);
        assertEquals(Arrays.asList(5), toRemove.events);

        // now add our adder/remover signal, and dispatch again
        signal.connect(new Slot<Integer>() {
            public void onEmit () {
                rconn.disconnect();
                signal.connect(toAdd);
            }
        });
        signal.emit(42);
        // make sure toRemove got this event and toAdd didn't
        assertEquals(Arrays.asList(5, 42), toRemove.events);
        assertEquals(0, toAdd.events.size());

        // finally emit one more and ensure that toAdd got it and toRemove didn't
        signal.emit(9);
        assertEquals(Arrays.asList(9), toAdd.events);
        assertEquals(Arrays.asList(5, 42), toRemove.events);
    }

    protected static class AccSlot<T> extends Slot<T> {
        public List<T> events = new ArrayList<T>();
        public void onEmit (T event) {
            events.add(event);
        }
    }
}

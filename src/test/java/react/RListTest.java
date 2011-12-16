//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import java.util.ListIterator;

import react.RList;

import org.junit.*;
import static org.junit.Assert.*;

public class RListTest
{
    public static class Counter extends RList.Listener<Object> {
        public int notifies;
        @Override public void onAdd (int index, Object elem) {
            notifies++;
        }
        @Override public void onSet (int index, Object newElem, Object oldElem) {
            notifies++;
        }
        @Override public void onRemove (int index, Object elem) {
            notifies++;
        }
    }

    @Test public void addAndRemove () {
        RList<String> list = RList.create();
        Counter counter = new Counter();
        list.connect(counter);
        list.connect(requireAdd("1")).once();
        list.add("1");
        assertEquals(1, counter.notifies);

        // add the same element, and ensure that we're notified again
        list.add("2");
        assertEquals(2, counter.notifies);

        // remove elements, ensure that we're notified
        list.remove("1");
        list.connect(requireRemove("2")).once();
        list.remove(0);
        assertEquals(4, counter.notifies);

        // remove it again, ensure that we're not notified
        list.remove("2");
        assertEquals(4, counter.notifies);

        // force remove it, ensure that we are notified
        list.removeForce("2");
        assertEquals(5, counter.notifies);

        // remove a non-existent element, ensure that we're not notified
        list.remove("3");
        assertEquals(5, counter.notifies);
    }

    @Test public void testSet () {
        RList<String> list = RList.create();
        list.add("1");
        list.add("2");

        Counter counter = new Counter();
        list.connect(counter);

        list.connect(new RList.Listener<String>() {
            public void onSet (int index, String newElem, String oldElem) {
                assertEquals(1, index);
                assertEquals("2", oldElem);
                assertEquals("3", newElem);
            }
        });
        list.set(1, "3");
        assertEquals(1, counter.notifies);
    }

    @Test public void listIterate () {
        RList<String> list = RList.create();
        list.add("1");
        list.add("2");
        Counter counter = new Counter();
        list.connect(counter);

        ListIterator<String> literator = list.listIterator();
        literator.next();
        // removing the last next call makes one remove notification
        list.connect(requireRemove("1")).once();
        literator.remove();
        assertEquals(1, counter.notifies);

        // setting the last next call makes one set notification
        literator.next();
        list.connect(new RList.Listener<String>() {
            public void onSet (int index, String newElem, String oldElem) {
                assertEquals(0, index);
                assertEquals("2", oldElem);
                assertEquals("3", newElem);
            }
        }).once();
        literator.set("3");
        assertEquals(2, counter.notifies);

        // adding on the iterator makes one notification
        list.connect(requireAdd("4")).once();
        literator.add("4");
        assertEquals(3, counter.notifies);

        // 3 and 4 in the list now
        assertEquals(2, list.size());
    }

    @Test public void testSizeView () {
        RList<String> list = RList.create();
        list.add("one");
        assertEquals(1, list.sizeView().get().intValue());
        list.remove("one");
        assertEquals(0, list.sizeView().get().intValue());

        SignalTest.Counter counter = new SignalTest.Counter();
        list.sizeView().connect(counter);
        list.add("two");
        assertEquals(1, counter.notifies);
        list.add("three");
        assertEquals(2, counter.notifies);
        list.remove("two");
        assertEquals(3, counter.notifies);
    }

    protected static <T> RList.Listener<T> requireAdd (final T reqElem) {
        return new RList.Listener<T>() {
            public void onAdd (T elem) {
                assertEquals(reqElem, elem);
            }
        };
    }

    protected static <T> RList.Listener<T> requireRemove (final T reqElem) {
        return new RList.Listener<T>() {
            public void onRemove (T elem) {
                assertEquals(reqElem, elem);
            }
        };
    }

}

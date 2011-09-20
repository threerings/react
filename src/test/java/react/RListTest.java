//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package test.java.react;

import java.util.ListIterator;
import react.RList;
import org.junit.*;
import static org.junit.Assert.*;

public class RListTest
{
    public static class Counter extends RList.Listener<Object> {
        public int notifies;
        @Override public void onAdd (Object elem) {
            notifies++;
        }
        @Override public void onRemove (Object elem) {
            notifies++;
        }
    }

    @Test public void addAndRemove () {
        RList<String> list = RList.createArrayRList();
        Counter counter = new Counter();
        list.listen(counter);
        list.listen(requireAdd("1")).once();
        list.add("1");
        assertEquals(1, counter.notifies);

        // add the same element, and ensure that we're notified again
        list.add("2");
        assertEquals(2, counter.notifies);

        // remove elements, ensure that we're notified
        list.remove("1");
        list.listen(requireRemove("2")).once();
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

    @Test public void listIterate () {
        RList<String> list = RList.createArrayRList();
        list.add("1");
        list.add("2");
        Counter counter = new Counter();
        list.listen(counter);

        ListIterator<String> literator = list.listIterator();
        literator.next();
        // removing the last next call makes one remove notification
        list.listen(requireRemove("1")).once();
        literator.remove();
        assertEquals(1, counter.notifies);

        // Setting the last next call makes two notifications
        list.listen(requireRemove("2")).once();
        list.listen(requireAdd("3")).once();
        literator.next();
        literator.set("3");
        assertEquals(3, counter.notifies);

        // Adding on the iterator makes one notification
        list.listen(requireAdd("4")).once();
        literator.add("4");
        assertEquals(4, counter.notifies);

        // 3 and 4 in the list now
        assertEquals(2, list.size());
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

//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Tests aspects of the {@link RSet} class.
 */
public class RSetTest
{
    public static class Counter extends RSet.Listener<Object> {
        public int notifies;
        @Override public void onAdd (Object elem) {
            notifies++;
        }
        @Override public void onRemove (Object elem) {
            notifies++;
        }
    }

    @Test public void testBasicNotify () {
        RSet<Integer> set = RSet.create(new HashSet<Integer>());
        Counter counter = new Counter();
        set.listen(counter);

        // add an element, ensure that we're notified
        set.listen(requireAdd(42)).once();
        set.add(42);
        assertEquals(1, counter.notifies);

        // add the same element, and ensure that we're not notified
        set.add(42);
        assertEquals(1, counter.notifies);

        // force add the same element, ensure that we are notified
        set.addForce(42);
        assertEquals(2, counter.notifies);

        // remove an element, ensure that we're notified
        set.listen(requireRemove(42)).once();
        set.remove(42);
        assertEquals(3, counter.notifies);

        // remove it again, ensure that we're not notified
        set.remove(42);
        assertEquals(3, counter.notifies);

        // force remove it, ensure that we are notified
        set.removeForce(42);
        assertEquals(4, counter.notifies);

        // remove a non-existent element, ensure that we're not notified
        set.remove(25);
        assertEquals(4, counter.notifies);
    }

    @Test public void testAggregatesEtc () {
        RSet<Integer> set = RSet.create(new HashSet<Integer>());
        Counter counter = new Counter();
        set.listen(counter);

        // test adding multiple entries
        set.addAll(Arrays.asList(1, 2, 3, 4));
        assertEquals(4, counter.notifies);

        // test removing by iterator
        Iterator<Integer> iter = set.iterator();
        iter.next();
        iter.remove();
        assertEquals(5, counter.notifies);
        assertEquals(3, set.size());
        int v1 = iter.next(), v2 = iter.next();

        // test notification on remove all
        set.removeAll(Arrays.asList(v1, 5, 6));
        assertEquals(6, counter.notifies);
        assertEquals(2, set.size());

        // test notification on retain all
        set.retainAll(Arrays.asList(v2, 7, 8));
        assertEquals(7, counter.notifies);
        assertEquals(1, set.size());

        // finally test notification on clear
        set.clear();
        assertEquals(8, counter.notifies);
        assertEquals(0, set.size());
    }

    @Test public void testContainsView () {
        RSet<Integer> set = RSet.create(new HashSet<Integer>());
        set.add(1);

        // create some contains key views and ensure their initial values are correct
        MappedValueView<Boolean> containsOne = set.containsView(1);
        MappedValueView<Boolean> containsTwo = set.containsView(2);
        assertTrue(containsOne.get());
        assertFalse(containsTwo.get());

        // listen for notifications
        ValueTest.Counter counter = new ValueTest.Counter();
        containsOne.listen(counter);
        containsTwo.listen(counter);

        // remove the element for one and ensure that we're notified
        containsOne.connect(SignalTest.require(false)).once();
        set.remove(1);
        assertEquals(1, counter.notifies);

        // make sure we're not repeat notified
        set.remove(1);
        assertEquals(1, counter.notifies);

        // force a remove and make sure we are repeat notified
        set.removeForce(1);
        assertEquals(2, counter.notifies);

        // add an element for two and ensure that we're notified
        containsTwo.connect(SignalTest.require(true)).once();
        set.add(2);
        assertEquals(3, counter.notifies);

        // make sure we're not repeat notified
        set.add(2);
        assertEquals(3, counter.notifies);

        // force an add and make sure we are repeat notified
        set.addForce(2);
        assertEquals(4, counter.notifies);
    }

    protected static <T> RSet.Listener<T> requireAdd (final T reqElem) {
        return new RSet.Listener<T>() {
            public void onAdd (T elem) {
                assertEquals(reqElem, elem);
            }
        };
    }

    protected static <T> RSet.Listener<T> requireRemove (final T reqElem) {
        return new RSet.Listener<T>() {
            public void onRemove (T elem) {
                assertEquals(reqElem, elem);
            }
        };
    }
}

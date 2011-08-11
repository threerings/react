//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Tests aspects of the {@link RMap} class.
 */
public class RMapTest
{
    @Test public void testBasicNotify () {
        RMap<Integer,String> map = RMap.create(new HashMap<Integer,String>());
        final int[] notifies = new int[] { 0 };

        // add a mapping, ensure that we're notified
        map.listen(new RMap.Listener<Integer,String>() {
            public void onPut (Integer key, String value, String ovalue) {
                assertEquals(42, key.intValue());
                assertEquals("LTUAE", value);
                assertNull(ovalue);
                notifies[0]++;
            }
        });
        map.put(42, "LTUAE");
        assertEquals(1, notifies[0]);

        // add the same mapping, and ensure that we're not notified
        map.put(42, "LTUAE");
        assertEquals(1, notifies[0]);

        // remove a mapping, ensure that we're notified
        map.listen(new RMap.Listener<Integer,String>() {
            public void onRemove (Integer key, String ovalue) {
                assertEquals(42, key.intValue());
                assertEquals("LTUAE", ovalue);
                notifies[0]++;
            }
        });
        map.remove(42);
        assertEquals(2, notifies[0]);

        // remove a non-existent mapping, ensure that we're not notified
        map.remove(25);
        assertEquals(2, notifies[0]);
    }

    @Test public void testForceNotify () {
        RMap<Integer,String> map = RMap.create(new HashMap<Integer,String>());
        final int[] notifies = new int[] { 0 };

        // add a mapping, ensure that we're notified
        map.listen(new RMap.Listener<Integer,String>() {
            public void onPut (Integer key, String value, String ovalue) {
                assertEquals(42, key.intValue());
                assertEquals("LTUAE", value);
                notifies[0]++;
            }
        });
        map.put(42, "LTUAE");
        assertEquals(1, notifies[0]);

        // update the mapping with the same value, and be sure we're notified
        map.putForce(42, "LTUAE");
        assertEquals(2, notifies[0]);

        // remove a mapping, ensure that we're notified
        map.listen(new RMap.Listener<Integer,String>() {
            public void onRemove (Integer key, String ovalue) {
                assertEquals(42, key.intValue());
                notifies[0]++;
            }
        });
        map.remove(42);
        assertEquals(3, notifies[0]);

        // remove the now non-existent mapping, ensure that we're notified
        map.removeForce(42);
        assertEquals(4, notifies[0]);
    }

    @Test public void testKeySet () {
        RMap<Integer,String> map = RMap.create(new HashMap<Integer,String>());
        map.put(42, "LTUAE");
        map.put(1, "one");

        final int[] notifies = new int[] { 0 };
        map.listen(new RMap.Listener<Integer,String>() {
            public void onRemove (Integer key, String ovalue) {
                notifies[0]++;
            }
        });
        Set<Integer> keys = map.keySet();

        // test basic keySet bits
        assertTrue(keys.contains(42));
        assertTrue(keys.contains(1));

        // remove an element from the key set, ensure that we're notified
        keys.remove(1);
        assertEquals(1, notifies[0]);

        // remove non-existent elements, and ensure that we're not notified
        keys.remove(1);
        assertEquals(1, notifies[0]);
        keys.remove(99);
        assertEquals(1, notifies[0]);

        // remove an element via the key set iterator, ensure that we're notified
        Iterator<Integer> iter = keys.iterator();
        iter.next();
        iter.remove();
        assertEquals(2, notifies[0]);

        // make sure it's not still in the key set
        keys.remove(42);
        assertEquals(2, notifies[0]);

        // finally check that the map is empty, just for kicks
        assertEquals(0, map.size());
    }

    @Test public void testValues () {
        RMap<Integer,String> map = RMap.create(new HashMap<Integer,String>());
        map.put(42, "LTUAE");
        map.put(1, "one");

        final int[] notifies = new int[] { 0 };
        map.listen(new RMap.Listener<Integer,String>() {
            public void onRemove (Integer key, String ovalue) {
                notifies[0]++;
            }
        });
        Collection<String> values = map.values();

        // test basic value bits
        assertTrue(values.contains("LTUAE"));
        assertTrue(values.contains("one"));

        // remove an element directly, ensure we're notified
        values.remove("one");
        assertEquals(1, notifies[0]);

        // make sure it's no longer in the collection
        values.remove("one");
        assertEquals(1, notifies[0]);

        // remove an element via the iterator, ensure we're notified
        Iterator<String> iter = values.iterator();
        iter.next();
        iter.remove();
        assertEquals(2, notifies[0]);

        // make sure it's not still in the collection
        values.remove("LTUAE");
        assertEquals(2, notifies[0]);

        // finally check that the map is empty, just for kicks
        assertEquals(0, map.size());
    }

    @Test public void testEntrySet () {
        RMap<Integer,String> map = RMap.create(new HashMap<Integer,String>());
        map.put(42, "LTUAE");
        map.put(1, "one");

        final int[] puts = new int[] { 0 };
        final int[] removes = new int[] { 0 };
        map.listen(new RMap.Listener<Integer,String>() {
            public void onPut (Integer key, String value, String ovalue) {
                puts[0]++;
            }
            public void onRemove (Integer key, String ovalue) {
                removes[0]++;
            }
        });

        // test the update of a value from the entry set
        map.listen(new RMap.Listener<Integer,String>() {
            public void onPut (Integer key, String value, String ovalue) {
                assertEquals(42, key.intValue());
                assertEquals("Mu", value);
                assertEquals("LTUAE", ovalue);
            }
        });
        Map.Entry<Integer,String> entry = null;
        for (Map.Entry<Integer,String> e : map.entrySet()) {
            if (e.getKey() == 42) entry = e;
        }
        entry.setValue("Mu");
        assertEquals(1, puts[0]);

        // test some basic entry set properties
        Set<Map.Entry<Integer,String>> entries = map.entrySet();
        assertEquals(2, entries.size());
        assertTrue(entries.contains(new HashMap.SimpleEntry<Integer,String>(42, "Mu")));
        assertTrue(entries.contains(new HashMap.SimpleEntry<Integer,String>(1, "one")));

        // test removal via the entry set
        entries.remove(new HashMap.SimpleEntry<Integer,String>(42, "Mu"));
        assertEquals(1, removes[0]);
        // make sure it's no longer there
        entries.remove(new HashMap.SimpleEntry<Integer,String>(42, "Mu"));
        assertEquals(1, removes[0]);

        // test removal via the itereator
        Iterator<Map.Entry<Integer,String>> iter = entries.iterator();
        iter.next();
        iter.remove();
        assertEquals(2, removes[0]);
        assertFalse(entries.contains(new HashMap.SimpleEntry<Integer,String>(1, "one")));
        // make sure it's no longer there
        entries.remove(new HashMap.SimpleEntry<Integer,String>(1, "one"));
        assertEquals(2, removes[0]);

        // finally check that the map is empty, just for kicks
        assertEquals(0, map.size());
    }

    @Test public void testContainsKeyView () {
        RMap<Integer,String> map = RMap.create(new HashMap<Integer,String>());
        map.put(1, "one");

        // create some contains key views and ensure their initial values are correct
        MappedValueView<Boolean> containsOne = map.containsKeyView(1);
        MappedValueView<Boolean> containsTwo = map.containsKeyView(2);
        assertTrue(containsOne.get());
        assertFalse(containsTwo.get());

        // listen for notifications
        final int[] notifies = new int[] { 0 };
        Value.Listener<Boolean> lner = new Value.Listener<Boolean>() {
            public void onChange () {
                notifies[0]++;
            }
        };
        containsOne.listen(lner);
        containsTwo.listen(lner);

        // remove the mapping for one and ensure that we're notified
        containsOne.connect(require(false)).once();
        map.remove(1);
        assertEquals(1, notifies[0]);

        // make sure we're not repeat notified
        map.remove(1);
        assertEquals(1, notifies[0]);

        // add a mapping for two and ensure that we're notified
        containsTwo.connect(require(true)).once();
        map.put(2, "two");
        assertEquals(2, notifies[0]);

        // make sure we're not repeat notified
        map.put(2, "ii");
        assertEquals(2, notifies[0]);
    }

    @Test public void testGetView () {
        RMap<Integer,String> map = RMap.create(new HashMap<Integer,String>());
        map.put(1, "one");

        // create some views and ensure their initial values are correct
        MappedValueView<String> oneView = map.getView(1);
        MappedValueView<String> twoView = map.getView(2);
        assertEquals("one", oneView.get());
        assertNull(twoView.get());

        // listen for notifications
        final int[] notifies = new int[] { 0 };
        Value.Listener<String> lner = new Value.Listener<String>() {
            public void onChange () {
                notifies[0]++;
            }
        };
        oneView.listen(lner);
        twoView.listen(lner);

        // remove the mapping for one and ensure that we're notified
        oneView.connect(this.<String>require(null)).once();
        map.remove(1);
        assertEquals(1, notifies[0]);

        // make sure we're not repeat notified
        map.remove(1);
        assertEquals(1, notifies[0]);

        // add a mapping for two and ensure that we're notified
        twoView.connect(require("two")).once();
        map.put(2, "two");
        assertEquals(2, notifies[0]);

        // make sure we're not notified when the same value is put
        map.put(2, "two");
        assertEquals(2, notifies[0]);

        // make sure we are notified when the value changes
        twoView.connect(require("ii")).once();
        map.put(2, "ii");
        assertEquals(3, notifies[0]);
    }

    protected <T> Slot<T> require (final T reqValue) {
        return new Slot<T>() {
            public void onEmit (T value) {
                assertEquals(reqValue, value);
            }
        };
    }
}

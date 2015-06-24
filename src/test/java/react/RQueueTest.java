//
// React - a library for functional-reactive-like programming
// Copyright (c) 2015, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import react.RQueue;

import org.junit.*;
import static org.junit.Assert.*;

public class RQueueTest
{
    public static class Counter extends RQueue.Listener<Object> {
        public int notifies;
        @Override public void onOffer (Object elem) { notifies++; }
        @Override public void onPoll (Object elem) { notifies++; }
    }

    @Test public void offerAndPoll () {
        RQueue<String> queue = RQueue.create();
        Counter counter = new Counter();
        queue.connect(counter);
        queue.connect(requireOffer("1")).once();
        queue.offer("1");
        assertEquals(1, counter.notifies);

        // offer more elements, and ensure that we're notified
        queue.offer("1");
        queue.offer("2");
        assertEquals(3, counter.notifies);

        // poll elements, ensure that we're notified
        queue.poll();
        queue.poll();
        queue.connect(requirePoll("2")).once();
        queue.poll();
        assertEquals(6, counter.notifies);

        // poll again, ensure that we're not notified
        queue.poll();
        assertEquals(6, counter.notifies);
    }

    protected static <T> RQueue.Listener<T> requireOffer (final T reqElem) {
        return new RQueue.Listener<T>() {
            public void onOffer (T elem) {
                assertEquals(reqElem, elem);
            }
        };
    }

    protected static <T> RQueue.Listener<T> requirePoll (final T reqElem) {
        return new RQueue.Listener<T>() {
            public void onPoll (T elem) {
                assertEquals(reqElem, elem);
            }
        };
    }
}

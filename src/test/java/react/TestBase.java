//
// React - a library for functional-reactive-like programming
// Copyright (c) 2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import static org.junit.Assert.*;

public abstract class TestBase {

    public static class Counter implements Slot<Object> {
        public void trigger () {
            _count++;
        }
        public void assertTriggered (int count) {
            assertEquals(count, _count);
        }
        public void assertTriggered (String message, int count) {
            assertEquals(message, count, _count);
        }
        public void reset () {
            _count = 0;
        }

        @Override public void onEmit (Object value) { trigger(); }

        protected int _count;
    }
}

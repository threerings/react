//
// React - a library for functional-reactive-like programming
// Copyright (c) 2015, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import java.util.HashSet;

/**
 * An extension of {@link AutoCloseable} chiefly to eliminate the checked exception thrown by
 * {@link #close}. React resources generally do not encounter failure during closure, thus the
 * checked exception is a needless burden to pass on to callers.
 */
public interface Closeable extends AutoCloseable {

    /** Maintains a set of closeables to allow mass operations on them. */
    class Set implements Closeable {

        /** Closes all connections in this set and empties it. */
        @Override public void close () {
            if (_set != null) {
                MultiFailureException error = null;
                for (AutoCloseable c : _set) try {
                    c.close();
                } catch (Exception e) {
                    if (error == null) error = new MultiFailureException();
                    error.addSuppressed(e);
                }
                _set.clear();
                if (error != null) throw error;
            }
        }

        /** Adds the supplied connection to this set.
          * @return the supplied connection.*/
        public <T extends AutoCloseable> T add (T c) {
            if (_set == null) _set = new HashSet<>();
            _set.add(c);
            return c;
        }

        /** Removes a closeable from this set while leaving its status unchanged. */
        public void remove (AutoCloseable c) {
            if (_set != null) _set.remove(c);
        }

        protected HashSet<AutoCloseable> _set; // lazily created
    }

    /** Provides some {@link Closeable}-related utilities. */
    class Util {

        /** A closable which no-ops on {@link #close} and throws an exception for all other
          * methods. This is for the following code pattern:
          *
          * <pre>{@code
          * Closable _conn = Closeable.Util.NOOP;
          * void open () {
          *    _conn = whatever.connect(...);
          * }
          * void close () {
          *    _conn = Closeable.Util.close(_conn);
          * }
          * }</pre>
          *
          * In that it allows {@code close} to avoid a null check if it's possible for
          * {@code close} to be called with no call to {@code open} or repeatedly.
          */
        public static final Closeable NOOP = new Closeable() {
            public void close () {} // noop!
        };

        /** Creates a closable that closes multiple connections at once. */
        public static Closeable join (final Closeable... cons) {
            return new Closeable() {
                @Override public void close () {
                    for (int ii = 0; ii < cons.length; ii++) {
                        if (cons[ii] == null) continue;
                        cons[ii].close();
                        cons[ii] = null;
                    }
                }
            };
        }

        /** Closes {@code con} and returns {@link #NOOP}. This enables code like:
          * {@code con = Connection.close(con);} which simplifies disconnecting and resetting to
          * {@link #NOOP}, a given connection reference. */
        public static Closeable close (Closeable con) {
            con.close();
            return NOOP;
        }
    }

    /** Closes this closeable resource. */
    void close ();
}

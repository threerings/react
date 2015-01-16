//
// React - a library for functional-reactive-like programming
// Copyright (c) 2015, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * An extension of {@link AutoCloseable} chiefly to eliminate the checked exception thrown by
 * {@link #close}. React resources generally do not encounter failure during closure, thus the
 * checked exception is a needless burden to pass on to callers.
 */
public interface Closeable extends AutoCloseable {

    /** Closes this closeable resource. */
    void close ();
}

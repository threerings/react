//
// React - a library for functional-reactive-like programming
// Copyright (c) 2015, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import java.util.Collection;

/**
 * A base class for reactive collections ({@link RList}, {@link RMap}, {@link RSet}).
 */
public abstract class RCollection<T> extends Reactor {

    /**
     * Returns the number of elements in this collection.
     */
    public abstract int size ();

    /**
     * Exposes the size of this collection as a value.
     */
    public synchronized ValueView<Integer> sizeView () {
        if (_sizeView == null) {
            _sizeView = Value.create(size());
        }
        return _sizeView;
    }

    /**
     * Returns a reactive value which is true when this collection is empty, false otherwise.
     */
    public ValueView<Boolean> isEmptyView () {
        return sizeView().map(Functions.lessThanEqual(0));
    }

    /**
     * Returns a reactive value which is false when this collection is empty, true otherwise.
     */
    public ValueView<Boolean> isNonEmptyView () {
        return sizeView().map(Functions.greaterThan(0));
    }

    /**
     * Updates the reactive size value. The underlying collection should call this method whenever
     * its size changes.
     */
    protected void updateSize () {
        if (_sizeView != null) _sizeView.update(size());
    }

    /** Used to expose the size of this set as a value. Initialized lazily. */
    private Value<Integer> _sizeView;
}

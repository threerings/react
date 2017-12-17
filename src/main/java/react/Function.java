//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011-present, React Authors
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * Models a single argument function.
 */
public interface Function<F, T> {

  /**
   * Applies this function to the supplied input value. A function is expected to have no side
   * effects; violate that assumption at your peril.
   */
  T apply (F input);
}

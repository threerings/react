//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * A signal that emits events of type {@code T}. {@link Slot}s may be connected to a signal to be
 * notified upon event emission.
 */
public class Signal<T> extends AbstractSignal<T> {

  /**
   * A signal that emits an event with no associated data. It can be used like so:
   */
  public static class Unit extends Signal<Void> {
    /** Connects a zero-argument listener to this signal. */
    public Connection connect (Runnable slot) {
      return connect(v -> slot.run());
    }

    /** Causes this signal to emit an event to its connected slots. */
    public void emit () {
      notifyEmit(null);
    }
  }

  /**
   * Convenience method for creating a signal without repeating the type parameter.
   */
  public static <T> Signal<T> create () {
    return new Signal<T>();
  }

  /**
   * Convenience method for creating a {@link Unit} signal.
   */
  public static Signal.Unit createUnit () {
    return new Signal.Unit();
  }

  /**
   * Causes this signal to emit the supplied event to connected slots.
   */
  public void emit (T event) {
    notifyEmit(event);
  }
}

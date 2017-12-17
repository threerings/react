//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011-present, React Authors
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * A synonym for {@link SignalView.Listener} and {@link ValueView.Listener} (ignoring the old
 * value) name. Also provides some filtering and composition methods.
 */
public interface Slot<T> extends SignalView.Listener<T>, ValueView.Listener<T> {

  /** A slot that does nothing. Useful when you don't want to fiddle with null checks. */
  public static Slot<Object> NOOP = v -> {}; // noop!

  /**
   * Returns a slot that logs the supplied message (via {@link System#err}) with the emitted
   * value appended to it before passing the emitted value on to {@code slot}. Useful for
   * debugging.
   */
  public static <T> Slot<T> trace (String message, Slot<T> slot) {
    return value -> {
      System.err.println(message + value);
      slot.onEmit(value);
    };
  }

  /**
   * Returns a slot that maps values via {@code f} and then passes them to this slot.
   * This is essentially function composition in that {@code slot.compose(f)} means
   * {@code slot(f(value)))} where this slot is treated as a side effecting void function.
   */
  default <S> Slot<S> compose (Function<S,T> f) {
    return value -> this.onEmit(f.apply(value));
  }

  /**
   * Returns a slot that is only notified when the signal to which this slot is connected emits a
   * value which causes {@code pred} to return true.
   */
  default <S extends T> Slot<S> filtered (Function<? super S,Boolean> pred) {
    return value -> {
      if (pred.apply(value)) this.onEmit(value);
    };
  }

  /**
   * Returns a new slot that invokes this slot and then evokes {@code after}.
   */
  default <S extends T> Slot<S> andThen (Slot<? super S> after) {
    return value -> {
      this.onEmit(value);
      after.onEmit(value);
    };
  }

  default void onChange (T newValue, T oldValue) { onEmit(newValue); }
}

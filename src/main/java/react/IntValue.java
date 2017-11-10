//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * A {@link Value} specialized for ints, which has some useful methods. Note: this specialization
 * does not mean "avoids auto-boxing", it just means "adds useful methods".
 */
public class IntValue extends Value<Integer> {

  /**
   * Creates an instance with the specified starting value.
   */
  public IntValue (int value) {
    super(value);
  }

  /**
   * Increments this value by {@code amount}.
   * @return the incremented value. Note that this differs from {@link #update}, which returns
   * the previous value.
   */
  public int increment (int amount) {
    return updateInt(get() + amount);
  }

  /**
   * Increments this value by {@code amount}, clamping at {@code max} if the current value plus
   * {@code amount} exceeds {@code max}.
   * @return the incremented and clamped value. Note that this differs from {@link #update},
   * which returns the previous value.
   */
  public int incrementClamp (int amount, int max) {
    return updateInt(Math.min(get() + amount, max));
  }

  /**
   * Increments this value by {@code amount} (or decrements if negative), clamping to the range
   * {@code [min, max]}.
   * @return the incremented and clamped value. Note that this differs from {@link #update},
   * which returns the previous value.
   */
  public int incrementClamp (int amount, int min, int max) {
    return updateInt(Math.max(min, Math.min(get() + amount, max)));
  }

  /**
   * Decrements this value by {@code amount}, clamping at {@code min} if the current value minus
   * {@code amount} is less than {@code min}.
   * @return the decremented and clamped value. Note that this differs from {@link #update},
   * which returns the previous value.
   */
  public int decrementClamp (int amount, int min) {
    return updateInt(Math.max(get() - amount, min));
  }

  protected int updateInt (int value) {
    update(value);
    return value;
  }
}

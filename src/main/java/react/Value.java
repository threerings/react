//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * A container for a single value, which may be observed for changes.
 */
public class Value<T> extends AbstractValue<T>
{
  /**
   * Convenience method for creating an instance with the supplied starting value.
   */
  public static <T> Value<T> create (T value) {
    return new Value<T>(value);
  }

  /**
   * Creates an instance with the supplied starting value.
   */
  public Value (T value) {
    // we can't have any listeners at this point, so no need to notify
    _value = value;
  }

  /**
   * Updates this instance with the supplied value. Registered listeners are notified only if the
   * value differs from the current value, as determined via {@link Object#equals}.
   * @return the previous value contained by this instance.
   */
  public T update (T value) {
    return updateAndNotifyIf(value);
  }

  /**
   * Updates this instance with the supplied value. Registered listeners are notified regardless
   * of whether the new value is equal to the old value.
   * @return the previous value contained by this instance.
   */
  public T updateForce (T value) {
    return updateAndNotify(value);
  }

  @Override public T get () {
    return _value;
  }

  @Override protected T updateLocal (T value) {
    T oldValue = _value;
    _value = value;
    return oldValue;
  }

  protected T _value;
}

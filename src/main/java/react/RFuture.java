//
// React - a library for functional-reactive-like programming
// Copyright (c) 2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Represents an asynchronous result. Unlike standard Java futures, you cannot block on this
 * result. You can {@link #map} or {@link #flatMap} it, and listen for success or failure via the
 * {@link #success} and {@link #failure} signals.
 *
 * <p> The benefit over just using {@code Callback} is that results can be composed. You can
 * subscribe to an object, flatmap the result into a service call on that object which returns the
 * address of another object, flat map that into a request to subscribe to that object, and finally
 * pass the resulting object to some other code via a slot. Failure can be handled once for all of
 * these operations and you avoid nesting yourself three callbacks deep. </p>
 */
public abstract class RFuture<T> extends Reactor {

  /** Used by {@link #sequence(RFuture,RFuture)}. */
  public static class T2<A,B> {
    public final A a;
    public final B b;
    public T2 (A a, B b) {
      this.a = a;
      this.b = b;
    }

    @Override public int hashCode () {
      return Objects.hashCode(a) ^ Objects.hashCode(b);
    }
    @Override public boolean equals (Object other) {
      if (!(other instanceof T2<?,?>)) return false;
      T2<?,?> ot = (T2<?,?>)other;
      return Objects.equals(a, ot.a) && Objects.equals(a, ot.b);
    }
  }

  /** Used by {@link #sequence(RFuture,RFuture,RFuture)}. */
  public static class T3<A,B,C> {
    public final A a;
    public final B b;
    public final C c;
    public T3 (A a, B b, C c) {
      this.a = a;
      this.b = b;
      this.c = c;
    }

    @Override public int hashCode () {
      return Objects.hashCode(a) ^ Objects.hashCode(b);
    }
    @Override public boolean equals (Object other) {
      if (!(other instanceof T3<?,?,?>)) return false;
      T3<?,?,?> ot = (T3<?,?,?>)other;
      return Objects.equals(a, ot.a) && Objects.equals(a, ot.b) && Objects.equals(c, ot.c);
    }
  }

  /** Returns a future with a pre-existing success value. */
  public static <T> RFuture<T> success (T value) {
    return result(Try.success(value));
  }

  /** Returns a future result for a {@code Void} method. */
  public static RFuture<Void> success () {
    return success(null);
  }

  /** Returns a future with a pre-existing failure value. */
  public static <T> RFuture<T> failure (Throwable cause) {
    return result(Try.<T>failure(cause));
  }

  /** Returns a future with an already-computed result. */
  public static <T> RFuture<T> result (Try<T> result) {
    return new RFuture<T>() {
      public Try<T> result () { return result; }
    };
  }

  /** Returns a future containing a list of all success results from {@code futures} if all of the
    * futures complete successfully, or a {@link MultiFailureException} aggregating all failures,
    * if any of the futures fails.
    *
    * <p>If {@code futures} is an ordered collection, the resulting list will match the order of
    * the futures. If not, result list is in {@code futures}' iteration order.</p> */
  public static <T> RFuture<List<T>> sequence (Collection<? extends RFuture<T>> futures) {
    // if we're passed an empty list of futures, succeed immediately with an empty list
    if (futures.isEmpty()) return RFuture.success(Collections.<T>emptyList());

    RPromise<List<T>> pseq = RPromise.create();
    int count = futures.size();
    class Sequencer {
      public synchronized void onResult (int idx, Try<T> result) {
        if (result.isSuccess()) {
          _results[idx] = result.get();
        } else {
          if (_error == null) _error = new MultiFailureException();
          _error.addFailure(result.getFailure());
        }
        if (--_remain == 0) {
          if (_error != null) pseq.fail(_error);
          else {
            @SuppressWarnings("unchecked") T[] results = (T[])_results;
            pseq.succeed(Arrays.asList(results));
          }
        }
      }
      protected final Object[] _results = new Object[count];
      protected int _remain = count;
      protected MultiFailureException _error;
    }
    Sequencer seq = new Sequencer();
    Iterator<? extends RFuture<T>> iter = futures.iterator();
    for (int ii = 0; iter.hasNext(); ii++) {
      int idx = ii;
      iter.next().onComplete(new SignalView.Listener<Try<T>>() {
        public void onEmit (Try<T> result) { seq.onResult(idx, result); }
      });
    }
    return pseq;
  }

  /** Returns a future containing the results of {@code a} and {@code b} if both futures complete
    * successfully, or a {@link MultiFailureException} aggregating all failures, if either of the
    * futures fails. */
  public static <A,B> RFuture<T2<A,B>> sequence (RFuture<A> a, RFuture<B> b) {
    @SuppressWarnings("unchecked") RFuture<Object> oa = (RFuture<Object>)a;
    @SuppressWarnings("unchecked") RFuture<Object> ob = (RFuture<Object>)b;
    return sequence(Arrays.asList(oa, ob)).map(new Function<List<Object>,T2<A,B>>() {
      public T2<A,B> apply (List<Object> results) {
        @SuppressWarnings("unchecked") A a = (A)results.get(0);
        @SuppressWarnings("unchecked") B b = (B)results.get(1);
        return new T2<A,B>(a, b);
      }
    });
  }

  /** Returns a future containing the results of {@code a}, {@code b}, and {@code c} if all
    * futures complete successfully, or a {@link MultiFailureException} aggregating all failures,
    * if any of the futures fails. */
  public static <A,B,C> RFuture<T3<A,B,C>> sequence (RFuture<A> a, RFuture<B> b, RFuture<C> c) {
    @SuppressWarnings("unchecked") RFuture<Object> oa = (RFuture<Object>)a;
    @SuppressWarnings("unchecked") RFuture<Object> ob = (RFuture<Object>)b;
    @SuppressWarnings("unchecked") RFuture<Object> oc = (RFuture<Object>)c;
    return sequence(Arrays.asList(oa, ob, oc)).map(new Function<List<Object>,T3<A,B,C>>() {
      public T3<A,B,C> apply (List<Object> results) {
        @SuppressWarnings("unchecked") A a = (A)results.get(0);
        @SuppressWarnings("unchecked") B b = (B)results.get(1);
        @SuppressWarnings("unchecked") C c = (C)results.get(2);
        return new T3<A,B,C>(a, b, c);
      }
    });
  }

  /** Returns a future containing a list of all success results from {@code futures}. Any failure
    * results are simply omitted from the list. The success results are also in no particular
    * order. If all of {@code futures} fail, the resulting list will be empty. */
  public static <T> RFuture<Collection<T>> collect (Collection<? extends RFuture<T>> futures) {
    // if we're passed an empty list of futures, succeed immediately with an empty list
    if (futures.isEmpty()) return RFuture.<Collection<T>>success(Collections.<T>emptyList());

    RPromise<Collection<T>> pseq = RPromise.create();
    int count = futures.size();
    SignalView.Listener<Try<T>> collector = new SignalView.Listener<Try<T>>() {
      public synchronized void onEmit (Try<T> result) {
        if (result.isSuccess()) _results.add(result.get());
        if (--_remain == 0) pseq.succeed(_results);
      }
      protected final List<T> _results = new ArrayList<T>();
      protected int _remain = count;
    };
    for (RFuture<T> future : futures) future.onComplete(collector);
    return pseq;
  }

  /** Causes {@code slot} to be notified if/when this future is completed with success. If it has
    * already succeeded, the slot will be notified immediately.
    * @return this future for chaining. */
  public RFuture<T> onSuccess (SignalView.Listener<? super T> slot) {
    return onComplete(new SignalView.Listener<Try<T>>() {
      public void onEmit (Try<T> result) {
        if (result.isSuccess()) slot.onEmit(result.get());
      }
    });
  }

  /** Causes {@code slot} to be notified if/when this future is completed with failure. If it has
    * already failed, the slot will be notified immediately.
    * @return this future for chaining. */
  public RFuture<T> onFailure (SignalView.Listener<? super Throwable> slot) {
    return onComplete(new SignalView.Listener<Try<T>>() {
      public void onEmit (Try<T> result) {
        if (result.isFailure()) slot.onEmit(result.getFailure());
      }
    });
  }

  /** Causes {@code slot} to be notified when this future is completed. If it has already
    * completed, the slot will be notified immediately.
    * @return this future for chaining. */
  public RFuture<T> onComplete (SignalView.Listener<? super Try<T>> slot) {
    Try<T> result = result();
    if (result != null) slot.onEmit(result);
    else addConnection(slot);
    return this;
  }

  /** Returns a value that indicates whether this future has completed. */
  public ValueView<Boolean> isComplete () {
    if (_isCompleteView == null) {
      Value<Boolean> isCompleteView = Value.create(false);
      onComplete(new SignalView.Listener<Try<T>>() {
        public void onEmit (Try<T> result) {
          isCompleteView.update(true);
        }
      });
      _isCompleteView = isCompleteView;
    }
    return _isCompleteView;
  }

  /** Returns whether this future is complete right now. This is an unfortunate name, but I
    * foolishly defined {@link #isComplete} to return a reactive view of completeness. */
  public boolean isCompleteNow () {
    return result() != null;
  }

  /** Convenience method to {@link ValueView#connectNotify} {@code slot} to {@link #isComplete}.
    * This is useful for binding the disabled state of UI elements to this future's completeness
    * (i.e. disabled while the future is incomplete, then reenabled when it is completed).
    * @return this future for chaining. */
  public RFuture<T> bindComplete (ValueView.Listener<Boolean> slot) {
    isComplete().connectNotify(slot);
    return this;
  }

  /** Transforms this future by mapping its result upon arrival. */
  public <R> RFuture<R> transform (Function<Try<? super T>,Try<R>> func) {
    RPromise<R> xf = RPromise.create();
    onComplete(new SignalView.Listener<Try<T>>() {
      public void onEmit (Try<T> result) {
        Try<R> xfResult;
        try {
          xfResult = func.apply(result);
        } catch (Throwable t) {
          xf.fail(t);
          return;
        }
        xf.complete(xfResult);
      }
    });
    return xf;
  }

  /** Maps the value of a successful result using {@code func} upon arrival. */
  public <R> RFuture<R> map (Function<? super T, R> func) {
    Object sigh = Try.lift(func);
    @SuppressWarnings("unchecked") Function<Try<? super T>,Try<R>> lifted =
      (Function<Try<? super T>,Try<R>>)sigh;
    return transform(lifted);
  }

  /** Maps the value of a failed result using {@code func} upon arrival. Ideally one could
    * generalize the type {@code T} here but Java doesn't allow type parameters with lower
    * bounds. */
  public RFuture<T> recover (Function<? super Throwable, T> func) {
    Object sigh = new Function<Try<T>,Try<T>>() {
      public Try<T> apply (Try<T> result) {
        return result.recover(func);
      }
    };
    @SuppressWarnings("unchecked") Function<Try<? super T>,Try<T>> lifted =
      (Function<Try<? super T>,Try<T>>)sigh;
    return transform(lifted);
  }

  /** Maps a successful result to a new result using {@code func} when it arrives. Failure on the
    * original result or the mapped result are both dispatched to the mapped result. This is
    * useful for chaining asynchronous actions. It's also known as monadic bind. */
  public <R> RFuture<R> flatMap (Function<? super T, RFuture<R>> func) {
    RPromise<R> mapped = RPromise.create();
    onComplete(new SignalView.Listener<Try<T>>() {
      public void onEmit (Try<T> result) {
        if (result.isFailure()) mapped.fail(result.getFailure());
        else {
          RFuture<R> mappedResult;
          try {
            mappedResult = func.apply(result.get());
          } catch (Throwable t) {
            mapped.fail(t);
            return;
          }
          mappedResult.onComplete(mapped::complete);
        }
      }
    });
    return mapped;
  }

  /** Returns the result of this future, or null if it is not yet complete.
    *
    * <p><em>NOTE:</em> don't use this method! You should wire up reactions to the completion of
    * this future via {@link #onSuccess} or {@link #onFailure}. React is not a blocking async
    * library where on might block a calling thread on the result of a future and then obtain the
    * result synchronously. This is <em>only</em> appropriate when you're trying to abstract over
    * synchronous and asynchronous variants of a computation, and you want to use the future
    * machinery in both cases, but in the synchronous case you know that your future will be
    * complete by the time you want to obtain its result.
    */
  public abstract Try<T> result ();

  @Override RListener placeholderListener () {
    return Slot.NOOP;
  }

  private ValueView<Boolean> _isCompleteView;
}

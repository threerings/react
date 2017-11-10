//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011-present, React Authors
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * A base class for all reactive classes. This is an implementation detail, but is public so that
 * third parties may use it to create their own reactive classes, if desired.
 */
public abstract class Reactor {

  /** The base class for all reactor listeners. */
  public abstract interface RListener {}

  /**
   * Returns true if this reactor has at least one connection.
   */
  public boolean hasConnections () {
    return _listeners != null;
  }

  /**
   * Clears all connections from this reactor. This is not used in normal circumstances, but is
   * made available for libraries which build on react and need a way to forcibly disconnect all
   * connections to reactive state.
   *
   * @throws IllegalStateException if this reactor is in the middle of dispatching an event.
   */
  public synchronized void clearConnections () {
    if (isDispatching()) throw new IllegalStateException(
      "Cannot clear connections while dispatching.");
    assert _pendingRuns == null;
    _listeners = null;
  }

  /** Returns the listener to be used when a weakly held listener is discovered to have been
    * collected while dispatching. This listener should NOOP when signaled. */
  abstract RListener placeholderListener ();

  protected synchronized Cons addConnection (RListener listener) {
    if (listener == null) throw new NullPointerException("Null listener");
    return addCons(new Cons(this, listener));
  }

  protected synchronized Cons addCons (final Cons cons) {
    if (isDispatching()) {
      _pendingRuns = append(_pendingRuns, new Runs() {
        public void run () {
          _listeners = Cons.insert(_listeners, cons);
          connectionAdded();
        }
      });
    } else {
      _listeners = Cons.insert(_listeners, cons);
      connectionAdded();
    }
    return cons;
  }

  protected synchronized void disconnect (final Cons cons) {
    if (isDispatching()) {
      _pendingRuns = append(_pendingRuns, new Runs() {
        public void run () {
          _listeners = Cons.remove(_listeners, cons);
          connectionRemoved();
        }
      });
    } else {
      _listeners = Cons.remove(_listeners, cons);
      connectionRemoved();
    }
  }

  protected synchronized void removeConnection (final RListener listener) {
    if (isDispatching()) {
      _pendingRuns = append(_pendingRuns, new Runs() {
        public void run () {
          _listeners = Cons.removeAll(_listeners, listener);
          connectionRemoved();
        }
      });
    } else {
      _listeners = Cons.removeAll(_listeners, listener);
      connectionRemoved();
    }
  }

  /**
   * Called prior to mutating any underlying model; allows subclasses to reject mutation.
   */
  protected void checkMutate () {
    // noop
  }

  /**
   * Called when a connection has been added to this reactor.
   */
  protected void connectionAdded () {
    // noop
  }

  /**
   * Called when a connection may have been removed from this reactor.
   */
  protected void connectionRemoved () {
    // noop
  }

  /**
   * Emits the supplied event to all connected slots. We omit a bunch of generic type shenanigans
   * here and force the caller to just cast things, because this is all under the hood where
   * there's zero chance of fucking up and this results in simpler, easier to read code.
   */
  protected void notify (final Notifier notifier, final Object a1, final Object a2,
                         final Object a3) {
    Cons lners;
    synchronized (this) {
      // if we're currently dispatching, defer this notification until we're done
      if (_listeners == DISPATCHING) {
        _pendingRuns = append(_pendingRuns, new Runs() {
          public void run () {
            Reactor.this.notify(notifier, a1, a2, a3);
          }
        });
        return;
      }
      lners = _listeners;
      Cons sentinel = DISPATCHING;
      _listeners = sentinel;
    }

    RuntimeException exn = null;
    try {
      // perform this dispatch, catching and accumulating any errors
      for (Cons cons = lners; cons != null; cons = cons.next) {
        try {
          notifier.notify(cons.listener(), a1, a2, a3);
        } catch (RuntimeException ex) {
          if (exn != null) exn.addSuppressed(ex);
          else exn = ex;
        }
        if (cons.oneShot()) cons.close();
      }

    } finally {
      // note that we're no longer dispatching
      synchronized (this) { _listeners = lners; }

      // perform any operations that were deferred while we were dispatching
      Runs run;
      while ((run = nextRun()) != null) {
        try {
          run.run();
        } catch (RuntimeException ex) {
          if (exn != null) exn.addSuppressed(ex);
          else exn = ex;
        }
      }
    }

    // finally throw any exception(s) that occurred during dispatch
    if (exn != null) throw exn;
  }

  private synchronized Runs nextRun () {
    Runs run = _pendingRuns;
    if (run != null) _pendingRuns = run.next;
    return run;
  }

  // always called while lock is held on this reactor
  private final boolean isDispatching () {
    return _listeners == DISPATCHING;
  }

  protected Cons _listeners;
  protected Runs _pendingRuns;

  /**
   * Returns true if both values are null, reference the same instance, or are
   * {@link Object#equals}.
   */
  protected static <T> boolean areEqual (T o1, T o2) {
    return (o1 == o2 || (o1 != null && o1.equals(o2)));
  }

  protected static Runs append (Runs head, Runs action) {
    if (head == null) return action;
    head.next = append(head.next, action);
    return head;
  }

  protected static abstract class Runs implements Runnable {
    public Runs next;
  }

  protected static abstract class Notifier {
    public abstract void notify (Object listener, Object a1, Object a2, Object a3);
  }

  protected static final Cons DISPATCHING = new Cons(null, null);
}

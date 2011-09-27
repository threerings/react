//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

public class WrappingCons<L extends Reactor.RListener> extends Cons<L>
{
    public final Object listener;

    public WrappingCons (Reactor<L> owner, L wrapper, Object listener) {
        super(owner, wrapper);
        this.listener = listener;
    }

    @Override Object listener () { return listener; }
}

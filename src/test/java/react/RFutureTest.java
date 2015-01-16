//
// React - a library for functional-reactive-like programming
// Copyright (c) 2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;

import org.junit.*;
import static org.junit.Assert.*;

public class RFutureTest extends TestBase {

    public class FutureCounter {
        public final Counter successes = new Counter();
        public final Counter failures = new Counter();
        public final Counter completes = new Counter();

        public void bind (RFuture<?> future) {
            reset();
            future.onSuccess(successes);
            future.onFailure(failures);
            future.onComplete(completes);
        }

        public void check (String state, int scount, int fcount, int ccount) {
            successes.assertTriggered("Successes " + state, scount);
            failures.assertTriggered("Failures " + state, fcount);
            completes.assertTriggered("Completes " + state, ccount);
        }

        public void reset () {
            successes.reset();
            failures.reset();
            completes.reset();
        }
    }

    @Test public void testImmediate () {
        FutureCounter counter = new FutureCounter();

        RFuture<String> success = RFuture.success("Yay!");
        counter.bind(success);
        counter.check("immediate succeed", 1, 0, 1);

        RFuture<String> failure = RFuture.failure(new Exception("Boo!"));
        counter.bind(failure);
        counter.check("immediate failure", 0, 1, 1);
    }

    @Test public void testDeferred () {
        FutureCounter counter = new FutureCounter();

        RPromise<String> success = RPromise.create();
        counter.bind(success);
        counter.check("before succeed", 0, 0, 0);
        success.succeed("Yay!");
        counter.check("after succeed", 1, 0, 1);

        RPromise<String> failure = RPromise.create();
        counter.bind(failure);
        counter.check("before fail", 0, 0, 0);
        failure.fail(new Exception("Boo!"));
        counter.check("after fail", 0, 1, 1);

        assertFalse(success.hasConnections());
        assertFalse(failure.hasConnections());
    }

    @Test public void testMappedImmediate () {
        FutureCounter counter = new FutureCounter();

        RFuture<String> success = RFuture.success("Yay!");
        counter.bind(success.map(Functions.NON_NULL));
        counter.check("immediate succeed", 1, 0, 1);

        RFuture<String> failure = RFuture.failure(new Exception("Boo!"));
        counter.bind(failure.map(Functions.NON_NULL));
        counter.check("immediate failure", 0, 1, 1);
    }

    @Test public void testMappedDeferred () {
        FutureCounter counter = new FutureCounter();

        RPromise<String> success = RPromise.create();
        counter.bind(success.map(Functions.NON_NULL));
        counter.check("before succeed", 0, 0, 0);
        success.succeed("Yay!");
        counter.check("after succeed", 1, 0, 1);

        RPromise<String> failure = RPromise.create();
        counter.bind(failure.map(Functions.NON_NULL));
        counter.check("before fail", 0, 0, 0);
        failure.fail(new Exception("Boo!"));
        counter.check("after fail", 0, 1, 1);

        assertFalse(success.hasConnections());
        assertFalse(failure.hasConnections());
    }

    @Test public void testFlatMappedImmediate () {
        FutureCounter scounter = new FutureCounter();
        FutureCounter fcounter = new FutureCounter();
        Function<String,RFuture<Boolean>> successMap = new Function<String,RFuture<Boolean>>() {
            public RFuture<Boolean> apply (String value) {
                return RFuture.success(value != null);
            }
        };
        Function<String,RFuture<Boolean>> failMap = new Function<String,RFuture<Boolean>>() {
            public RFuture<Boolean> apply (String value) {
                return RFuture.failure(new Exception("Barzle!"));
            }
        };

        RFuture<String> success = RFuture.success("Yay!");
        scounter.bind(success.flatMap(successMap));
        fcounter.bind(success.flatMap(failMap));
        scounter.check("immediate success/success", 1, 0, 1);
        fcounter.check("immediate success/failure", 0, 1, 1);

        RFuture<String> failure = RFuture.failure(new Exception("Boo!"));
        scounter.bind(failure.flatMap(successMap));
        fcounter.bind(failure.flatMap(failMap));
        scounter.check("immediate failure/success", 0, 1, 1);
        scounter.check("immediate failure/failure", 0, 1, 1);
    }

    @Test public void testFlatMappedDeferred () {
        FutureCounter scounter = new FutureCounter();
        FutureCounter fcounter = new FutureCounter();
        Function<String,RFuture<Boolean>> successMap = new Function<String,RFuture<Boolean>>() {
            public RFuture<Boolean> apply (String value) {
                return RFuture.success(value != null);
            }
        };
        Function<String,RFuture<Boolean>> failMap = new Function<String,RFuture<Boolean>>() {
            public RFuture<Boolean> apply (String value) {
                return RFuture.failure(new Exception("Barzle!"));
            }
        };

        RPromise<String> success = RPromise.create();
        scounter.bind(success.flatMap(successMap));
        scounter.check("before succeed/succeed", 0, 0, 0);
        fcounter.bind(success.flatMap(failMap));
        fcounter.check("before succeed/fail", 0, 0, 0);
        success.succeed("Yay!");
        scounter.check("after succeed/succeed", 1, 0, 1);
        fcounter.check("after succeed/fail", 0, 1, 1);

        RPromise<String> failure = RPromise.create();
        scounter.bind(failure.flatMap(successMap));
        fcounter.bind(failure.flatMap(failMap));
        scounter.check("before fail/success", 0, 0, 0);
        fcounter.check("before fail/failure", 0, 0, 0);
        failure.fail(new Exception("Boo!"));
        scounter.check("after fail/success", 0, 1, 1);
        fcounter.check("after fail/failure", 0, 1, 1);

        assertFalse(success.hasConnections());
        assertFalse(failure.hasConnections());
    }

    @Test public void testFlatMappedDoubleDeferred () {
        FutureCounter scounter = new FutureCounter();
        FutureCounter fcounter = new FutureCounter();

        {   RPromise<String> success = RPromise.create();
            final RPromise<Boolean> innerSuccessSuccess = RPromise.create();
            scounter.bind(success.flatMap(new Function<String,RFuture<Boolean>>() {
                public RFuture<Boolean> apply (String value) {
                    return innerSuccessSuccess;
                }
            }));
            scounter.check("before succeed/succeed", 0, 0, 0);
            final RPromise<Boolean> innerSuccessFailure = RPromise.create();
            fcounter.bind(success.flatMap(new Function<String,RFuture<Boolean>>() {
                public RFuture<Boolean> apply (String value) {
                    return innerSuccessFailure;
                }
            }));
            fcounter.check("before succeed/fail", 0, 0, 0);

            success.succeed("Yay!");
            scounter.check("after first succeed/succeed", 0, 0, 0);
            fcounter.check("after first succeed/fail", 0, 0, 0);
            innerSuccessSuccess.succeed(true);
            scounter.check("after second succeed/succeed", 1, 0, 1);
            innerSuccessFailure.fail(new Exception("Boo hoo!"));
            fcounter.check("after second succeed/fail", 0, 1, 1);

            assertFalse(success.hasConnections());
            assertFalse(innerSuccessSuccess.hasConnections());
            assertFalse(innerSuccessFailure.hasConnections());
        }

        {   RPromise<String> failure = RPromise.create();
            final RPromise<Boolean> innerFailureSuccess = RPromise.create();
            scounter.bind(failure.flatMap(new Function<String,RFuture<Boolean>>() {
                public RFuture<Boolean> apply (String value) {
                    return innerFailureSuccess;
                }
            }));
            scounter.check("before fail/succeed", 0, 0, 0);
            final RPromise<Boolean> innerFailureFailure = RPromise.create();
            fcounter.bind(failure.flatMap(new Function<String,RFuture<Boolean>>() {
                public RFuture<Boolean> apply (String value) {
                    return innerFailureFailure;
                }
            }));
            fcounter.check("before fail/fail", 0, 0, 0);

            failure.fail(new Exception("Boo!"));
            scounter.check("after first fail/succeed", 0, 1, 1);
            fcounter.check("after first fail/fail", 0, 1, 1);
            innerFailureSuccess.succeed(true);
            scounter.check("after second fail/succeed", 0, 1, 1);
            innerFailureFailure.fail(new Exception("Is this thing on?"));
            fcounter.check("after second fail/fail", 0, 1, 1);

            assertFalse(failure.hasConnections());
            assertFalse(innerFailureSuccess.hasConnections());
            assertFalse(innerFailureFailure.hasConnections());
        }
    }

    @Test public void testSequenceImmediate () {
        FutureCounter counter = new FutureCounter();

        RFuture<String> success1 = RFuture.success("Yay 1!");
        RFuture<String> success2 = RFuture.success("Yay 2!");

        RFuture<String> failure1 = RFuture.failure(new Exception("Boo 1!"));
        RFuture<String> failure2 = RFuture.failure(new Exception("Boo 2!"));

        RFuture<List<String>> sucseq = RFuture.sequence(list(success1, success2));
        counter.bind(sucseq);
        sucseq.onSuccess(new Slot<List<String>>() {
            public void onEmit (List<String> results) {
                assertEquals(list("Yay 1!", "Yay 2!"), results);
            }
        });
        counter.check("immediate seq success/success", 1, 0, 1);

        counter.bind(RFuture.sequence(list(success1, failure1)));
        counter.check("immediate seq success/failure", 0, 1, 1);

        counter.bind(RFuture.sequence(list(failure1, success2)));
        counter.check("immediate seq failure/success", 0, 1, 1);

        counter.bind(RFuture.sequence(list(failure1, failure2)));
        counter.check("immediate seq failure/failure", 0, 1, 1);
    }

    @Test public void testSequenceDeferred () {
        FutureCounter counter = new FutureCounter();

        RPromise<String> success1 = RPromise.create(), success2 = RPromise.create();
        RPromise<String> failure1 = RPromise.create(), failure2 = RPromise.create();

        RFuture<List<String>> suc2seq = RFuture.sequence(list(success1, success2));
        counter.bind(suc2seq);
        suc2seq.onSuccess(new Slot<List<String>>() {
            public void onEmit (List<String> results) {
                assertEquals(list("Yay 1!", "Yay 2!"), results);
            }
        });
        counter.check("before seq succeed/succeed", 0, 0, 0);
        success1.succeed("Yay 1!");
        success2.succeed("Yay 2!");
        counter.check("after seq succeed/succeed", 1, 0, 1);

        RFuture<List<String>> sucfailseq = RFuture.sequence(list(success1, failure1));
        sucfailseq.onFailure(new Slot<Throwable>() {
            public void onEmit (Throwable cause) {
                assertTrue(cause instanceof MultiFailureException);
                assertEquals("1 failures: java.lang.Exception: Boo 1!", cause.getMessage());
            }
        });
        counter.bind(sucfailseq);
        counter.check("before seq succeed/fail", 0, 0, 0);
        failure1.fail(new Exception("Boo 1!"));
        counter.check("after seq succeed/fail", 0, 1, 1);

        RFuture<List<String>> failsucseq = RFuture.sequence(list(failure1, success2));
        failsucseq.onFailure(new Slot<Throwable>() {
            public void onEmit (Throwable cause) {
                assertTrue(cause instanceof MultiFailureException);
                assertEquals("1 failures: java.lang.Exception: Boo 1!", cause.getMessage());
            }
        });
        counter.bind(failsucseq);
        counter.check("after seq fail/succeed", 0, 1, 1);

        RFuture<List<String>> fail2seq = RFuture.sequence(list(failure1, failure2));
        fail2seq.onFailure(new Slot<Throwable>() {
            public void onEmit (Throwable cause) {
                assertTrue(cause instanceof MultiFailureException);
                assertEquals("2 failures: java.lang.Exception: Boo 1!, java.lang.Exception: Boo 2!",
                             cause.getMessage());
            }
        });
        counter.bind(fail2seq);
        counter.check("before seq fail/fail", 0, 0, 0);
        failure2.fail(new Exception("Boo 2!"));
        counter.check("after seq fail/fail", 0, 1, 1);
    }

    @Test public void testSequenceEmpty () {
        FutureCounter counter = new FutureCounter();
        RFuture<List<String>> seq = RFuture.sequence(Collections.<RFuture<String>>emptyList());
        counter.bind(seq);
        counter.check("sequence empty list succeeds", 1, 0, 1);
    }

    @Test public void testSequenceTuple () {
        FutureCounter counter = new FutureCounter();
        RFuture<String> string = RFuture.success("string");
        RFuture<Integer> integer = RFuture.success(42);

        RFuture<RFuture.T2<String,Integer>> sucsuc = RFuture.sequence(string, integer);
        sucsuc.onSuccess(new Slot<RFuture.T2<String,Integer>>() {
            public void onEmit (RFuture.T2<String,Integer> tup) {
                assertEquals("string", tup.a);
                assertEquals((Integer)42, tup.b);
            }
        });
        counter.bind(sucsuc);
        counter.check("tuple2 seq success/success", 1, 0, 1);

        RFuture<Integer> fail = RFuture.failure(new Exception("Alas, poor Yorrick."));
        RFuture<RFuture.T2<String,Integer>> sucfail = RFuture.sequence(string, fail);
        counter.bind(sucfail);
        counter.check("tuple2 seq success/fail", 0, 1, 1);

        RFuture<RFuture.T2<Integer,String>> failsuc = RFuture.sequence(fail, string);
        counter.bind(failsuc);
        counter.check("tuple2 seq fail/success", 0, 1, 1);
    }

    @Test public void testCollectEmpty () {
        FutureCounter counter = new FutureCounter();
        RFuture<Collection<String>> seq = RFuture.collect(Collections.<RFuture<String>>emptyList());
        counter.bind(seq);
        counter.check("collect empty list succeeds", 1, 0, 1);
    }

    // fucking Java generics and arrays... blah
    protected <T> List<T> list (T one, T two) {
        List<T> list = new ArrayList<T>();
        list.add(one);
        list.add(two);
        return list;
    }
}

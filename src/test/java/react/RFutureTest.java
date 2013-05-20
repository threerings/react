//
// React - a library for functional-reactive-like programming
// Copyright (c) 2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

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

}

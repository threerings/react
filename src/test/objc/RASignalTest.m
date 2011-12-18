//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import "RASignalTest.h"

#import "RASignal.h"

@implementation RASignalTest
- (void)testEmission {
    RASignal *sig = [[RASignal alloc] init];
    __block int x = 0;
    [sig connectUnit:^{ x++; }];
    [sig connectSignal:^(id value){ STAssertEquals(value, @"Hello", nil); x++; }];
    [sig emitEvent:@"Hello"];
    STAssertEquals(x, 2, nil);
}

@end

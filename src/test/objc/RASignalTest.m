//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import "RASignalTest.h"

#import "RAObjectSignal.h"

@implementation RASignalTest
- (void)testEmission {
    RAObjectSignal *sig = [[RAObjectSignal alloc] init];
    __block int x = 0;
    [sig connectUnit:^{ x++; }];
    [sig connectSlot:^(id value){ STAssertEquals(value, @"Hello", nil); x++; }];
    [sig emitEvent:@"Hello"];
    STAssertEquals(x, 2, nil);
}

@end

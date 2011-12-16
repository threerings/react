//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import "RAUnitSignalTest.h"
#import "RAUnitSignal.h"
#import "RAConnection.h"

@implementation RAUnitSignalTest

- (void)testEmission
{
    RAUnitSignal *sig = [[RAUnitSignal alloc] init];
    __block int x = 0;
    [sig connectBlock:^{ x++; }];
    [sig emit];
    [sig emit];
    STAssertEquals(x, 2, nil);
}

- (void)testMultipleListeners
{
    RAUnitSignal *sig = [[RAUnitSignal alloc] init];
    __block int x = 0;
    [sig connectBlock:^{ x++; }];
    [sig connectBlock:^{ x++; }];
    [sig emit];
    STAssertEquals(x, 2, nil);
}

- (void)testDisconnecting
{
    RAUnitSignal *sig = [[RAUnitSignal alloc] init];
    __block int x = 0;
    RAConnection *conn = [sig connectBlock:^{ x++; }];
    [[sig connectBlock:^{ x++; }] once];
    [sig emit];
    STAssertEquals(x, 2, nil);
    [sig emit];
    STAssertEquals(x, 3, nil);
    [conn disconnect];
    [sig emit];
    STAssertEquals(x, 3, nil);
}

@end

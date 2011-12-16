//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import "RAUnitSignalTest.h"
#import "RAUnitSignal.h"
#import "RAConnectionGroup.h"
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
    __block int y = 0;
    [[sig connectBlock:^{ x++; }] once];
    [sig connectBlock:^{ y++; }];
    RAConnection *conn = [sig connectBlock:^{ x++; }];
    [sig emit];
    STAssertEquals(x, 2, nil);
    STAssertEquals(y, 1, nil);
    [sig emit];
    STAssertEquals(x, 3, nil);
    STAssertEquals(y, 2, nil);
    [conn disconnect];
    [sig emit];
    STAssertEquals(x, 3, nil);
    STAssertEquals(y, 3, nil);
}

- (void)testGroup {
    RAConnectionGroup *group = [[RAConnectionGroup alloc] init];
    RAUnitSignal *sig = [[RAUnitSignal alloc] init];
    __block int x = 0;
    [group addConnection:[sig connectBlock:^{ x++; }]];
    [group addConnection:[sig connectBlock:^{ x++; }]];
    [sig connectBlock:^{ x++; }];
    [sig emit];
    STAssertEquals(x, 3, nil);
    [group disconnectAll];
    [sig emit];
    STAssertEquals(x, 4, nil);
}

- (void)testDisconnectingProperBlock
{
}

@end

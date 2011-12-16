//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import "RAUnitSignalTest.h"
#import "RAUnitSignal.h"

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

@end

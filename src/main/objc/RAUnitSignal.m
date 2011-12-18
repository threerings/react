//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import "RAUnitSignal.h"
#import "RAReactor+Protected.h"
#import "RAConnection.h"

@implementation RAUnitSignal

- (void) emit {
    for (RAConnection *cur = [self prepareForEmission]; cur != nil; cur = cur->next) {
        cur->listener();
        if (cur->oneShot) [cur disconnect];
    }
    [self finishEmission];
}

- (RAConnection*)connectUnit:(RAUnitBlock)block {
    return [self withPriority:RA_DEFAULT_PRIORITY connectUnit:block];
}

- (RAConnection*) withPriority:(int)priority connectUnit:(RAUnitBlock)block {
    RAConnection *cons = [[RAConnection alloc] init];
    // TODO - copy necessary?
    cons->listener = [block copy];
    return [self withPriority:priority connectConnection:cons];
}

@end

//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import "RADoubleReactor.h"
#import "RAReactor+Protected.h"
#import "RAConnection+Package.h"
#import "RADoubleReactor+Protected.h"

@implementation RADoubleReactor
- (void)dispatchEvent:(double)event {
    for (RAConnection *cur = [self prepareForEmission]; cur != nil; cur = cur->next) {
        if ([self isConnected:cur]) {
            ((RADoubleSlot)cur->block)(event);
            if (cur->oneShot) [cur disconnect];
        }
    }
    [self finishEmission];
}

- (RAConnection*)connectSlot:(RADoubleSlot)block {
    return [self withPriority:RA_DEFAULT_PRIORITY connectSlot:block];
}

- (RAConnection*)withPriority:(int)priority connectSlot:(RADoubleSlot)block {
    return [self connectConnection:[[RAConnection alloc] initWithBlock:[block copy] atPriority:priority onReactor:self]];
}

- (RAConnection*)connectUnit:(RAUnitBlock)block {
    return [self withPriority:RA_DEFAULT_PRIORITY connectUnit:block];
}

- (RAConnection*)withPriority:(int)priority connectUnit:(RAUnitBlock)block {
    return [self withPriority:priority connectSlot:^(double event) { block(); }];
}
@end

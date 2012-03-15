//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import "RABoolReactor.h"
#import "RAReactor+Protected.h"
#import "RAConnection+Package.h"
#import "RABoolReactor+Protected.h"

@implementation RABoolReactor
- (void)dispatchEvent:(BOOL)event {
    for (RAConnection *cur = [self prepareForEmission]; cur != nil; cur = cur->next) {
        ((RABoolSlot)cur->block)(event);
        if (cur->oneShot) [cur disconnect];
    }
    [self finishEmission];
}

- (RAConnection*)connectSlot:(RABoolSlot)block {
    return [self withPriority:RA_DEFAULT_PRIORITY connectSlot:block];
}

- (RAConnection*)withPriority:(int)priority connectSlot:(RABoolSlot)block {
    return [self connectConnection:[[RAConnection alloc] initWithBlock:[block copy] atPriority:priority onReactor:self]];
}

- (RAConnection*)connectUnit:(RAUnitBlock)block {
    return [self withPriority:RA_DEFAULT_PRIORITY connectUnit:block];
}

- (RAConnection*)withPriority:(int)priority connectUnit:(RAUnitBlock)block {
    return [self withPriority:priority connectSlot:^(BOOL event) { block(); }];
}
@end

//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import "RASignal.h"
#import "RAReactor+Protected.h"
#import "RAConnection.h"

@implementation RASignal
- (void) emitEvent:(id)event {
    for (RAConnection *cur = [self prepareForEmission]; cur != nil; cur = cur->next) {
        ((RASignalBlock)cur->block)(event);
        if (cur->oneShot) [cur disconnect];
    }
    [self finishEmission];
}

- (RAConnection*) connectSignal:(RASignalBlock)block {
      return [self withPriority:RA_DEFAULT_PRIORITY connectSignal:block];
}

- (RAConnection*) withPriority:(int)priority connectSignal:(RASignalBlock)block {
    RAConnection *cons = [[RAConnection alloc] init];
    cons->block = [block copy];
    return [self withPriority:priority connectConnection:cons];
}

- (RAConnection*) connectUnit:(RAUnitBlock)block {
    return [self withPriority:RA_DEFAULT_PRIORITY connectUnit:block];
}

- (RAConnection*) withPriority:(int)priority connectUnit:(RAUnitBlock)block {
     return [self withPriority:priority connectSignal:^(id event) { block(); }];
}
@end

//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import "RAConnectionGroup.h"
#import "RAConnection.h"

@implementation RAConnectionGroup {
    NSMutableSet *_conns;
}

- (id)init {
    if (!(self = [super init])) return nil;
    _conns = [[NSMutableSet alloc] init];
    return self;
}

- (void)disconnectAll {
    for (RAConnection *conn in _conns) {
        [conn disconnect];
    }
    [_conns removeAllObjects];
}

- (void)addConnection:(RAConnection*)conn {
    [_conns addObject:conn];
}

- (void)removeConnection:(RAConnection*)conn {
    [_conns removeObject:conn];
}

@end

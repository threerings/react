//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import <Foundation/Foundation.h>

@class RAConnection;

/** Holds on to multiple connections to allow for simultaneous disconnection. */
@interface RAConnectionGroup : NSObject

/** Adds a connection to this group. */
- (void)addConnection:(RAConnection*)conn;

/** Removes a connection from this group. */
- (void)removeConnection:(RAConnection*)conn;

/** Disconnects all connections in this group, and then removes them from the group. */
- (void)disconnectAll;
@end

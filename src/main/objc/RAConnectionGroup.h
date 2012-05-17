//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import <Foundation/Foundation.h>

#import "RADefs.h"

@class RAConnection;
@class RAReactor;
@class RABoolReactor;
@class RADoubleReactor;
@class RAFloatReactor;
@class RAIntReactor;
@class RAObjectReactor;

/** Holds on to multiple connections to allow for simultaneous disconnection. */
@interface RAConnectionGroup : NSObject {
@protected
    NSMutableSet *_conns;
}

/** Adds a connection to this group. */
- (RAConnection*)addConnection:(RAConnection*)conn;

/** Removes a connection from this group. */
- (RAConnection*)removeConnection:(RAConnection*)conn;

/** Disconnects all connections in this group, and then removes them from the group. */
- (void)disconnectAll;

/** Connects the given unit at the default priority and adds its connection to this group.  */
- (RAConnection*)onReactor:(RAReactor*)reactor connectUnit:(RAUnitBlock)block;

/** Connects the given unit at the given priority and adds its connection to this group.  */
- (RAConnection*)onReactor:(RAReactor*)reactor withPriority:(int)priority connectUnit:(RAUnitBlock)block;

/**
 * Connects the given block to receieve emissions from the given signal at the default priority and
 * adds the connection to this group.
 */
- (RAConnection*)onBoolReactor:(RABoolReactor*)reactor connectSlot:(RABoolSlot)block;

/** Connects the given block at the given priority and adds its connection to this group.  */
- (RAConnection*)onBoolReactor:(RABoolReactor*)reactor withPriority:(int)priority connectSlot:(RABoolSlot)block;

/**
 * Connects the given block to receieve emissions from the given signal at the default priority
 * and adds it connection to this group
 */
- (RAConnection*)onDoubleReactor:(RADoubleReactor*)reactor connectSlot:(RADoubleSlot)block;

/** Connects the given block at the given priority and adds its connection to this group.  */
- (RAConnection*)onDoubleReactor:(RADoubleReactor*)reactor withPriority:(int)priority connectSlot:(RADoubleSlot)block;

/**
 * Connects the given block to receieve emissions from the given signal at the default priority and
 * adds the connection to this group.
 */
- (RAConnection*)onFloatReactor:(RAFloatReactor*)reactor connectSlot:(RAFloatSlot)block;

/** Connects the given block at the given priority and adds its connection to this group.  */
- (RAConnection*)onFloatReactor:(RAFloatReactor*)reactor withPriority:(int)priority connectSlot:(RAFloatSlot)block;

/**
 * Connects the given block to receieve emissions from the given signal at the default priority and
 * adds the connection to this group.
 */
- (RAConnection*)onIntReactor:(RAIntReactor*)reactor connectSlot:(RAIntSlot)block;

/** Connects the given block at the given priority and adds its connection to this group.  */
- (RAConnection*)onIntReactor:(RAIntReactor*)reactor withPriority:(int)priority connectSlot:(RAIntSlot)block;

/**
 * Connects the given block to receieve emissions from the given signal at the default priority and
 * adds the connection to this group.
 */
- (RAConnection*)onObjectReactor:(RAObjectReactor*)reactor connectSlot:(RAObjectSlot)block;

/** Connects the given block at the given priority and adds its connection to this group.  */
- (RAConnection*)onObjectReactor:(RAObjectReactor*)reactor withPriority:(int)priority connectSlot:(RAObjectSlot)block;

@end

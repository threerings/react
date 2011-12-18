//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import <Foundation/Foundation.h>

@class RAConnection;
@class RAConnectionGroup;

typedef void (^RAUnitBlock)(void);

#define RA_DEFAULT_PRIORITY 0

@interface RAUnitSignal : NSObject
- (void) emit;
- (RAConnection*) connectBlock:(RAUnitBlock)block;
- (RAConnection*) withPriority:(int)priority connectBlock:(RAUnitBlock)block;
- (void) disconnect:(RAConnection*)conn;
@end

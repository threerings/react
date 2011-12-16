//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import <Foundation/Foundation.h>

@class RAConnection;

@interface RAUnitSignal : NSObject
- (void) emit;
- (RAConnection*) connectBlock:(void (^)(void))block;
@end

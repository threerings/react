//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import <Foundation/Foundation.h>

@class RAConnection;

@interface RAConnectionGroup : NSObject
- (void)addConnection:(RAConnection*)conn;
- (void)removeConnection:(RAConnection*)conn;
- (void)disconnectAll;
@end

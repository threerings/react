//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import <Foundation/Foundation.h>

#import "RADefs.h"

@class RAConnection;

@interface RAReactor : NSObject
- (void) disconnect:(RAConnection*)conn;
@end

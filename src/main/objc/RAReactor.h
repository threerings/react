//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import <Foundation/Foundation.h>

#import "RADefs.h"

@class RAConnection;

/**
 * Handles the basics of connection and dispatching management.
 */
@interface RAReactor : NSObject

/** Keeps the given connection from receiving further emissions. */
- (void) disconnect:(RAConnection*)conn;

@end

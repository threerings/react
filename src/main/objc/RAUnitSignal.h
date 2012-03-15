//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import <Foundation/Foundation.h>
#import "RAReactor.h"

/** Emits events with no corresponding data. */
@interface RAUnitSignal : RAReactor

/** Invokes all connected blocks. */
- (void)emit;

/** Connects the given block to be called on future emissions. */
- (RAConnection*)connectUnit:(RAUnitBlock)block;

/** Connects the given block at the given priority to receive future emissions. */
- (RAConnection*)withPriority:(int)priority connectUnit:(RAUnitBlock)block;
@end

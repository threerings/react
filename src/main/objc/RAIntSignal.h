//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import "RAIntReactor.h"

@interface RAIntSignal : RAIntReactor
/** @name Emission */

/** Emits the supplied value to all connected slots. */
- (void)emitEvent:(int)event;
@end

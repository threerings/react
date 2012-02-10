//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import "RAReactor.h"

@interface RAObjectReactor : RAReactor
/** @name Connection */

/** Connects the given block to receieve emissions from this signal at the default priority.  */
- (RAConnection*) connectSlot:(RAObjectSlot)block;

/** Connects the given block at the given priority.  */
- (RAConnection*) withPriority:(int)priority connectSlot:(RAObjectSlot)block;
@end

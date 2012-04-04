//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import "RAIntReactor.h"

@interface RAIntValue : RAIntReactor
@property(nonatomic,readwrite) int value;
- (id)init;
- (id)initWithValue:(int)value;
@end

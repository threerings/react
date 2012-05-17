//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import "RABoolValue.h"
#import "RABoolReactor+Protected.h"

@implementation RABoolValue

- (id)init {
    return [self initWithValue:NO];
}

- (id)initWithValue:(BOOL)value {
    if (!(self = [super init])) return nil;
    _value = value;
    return self;
}

- (BOOL)value { return _value; }

- (void)setValue:(BOOL)value {
    if (value == _value) return;
    _value = value;
    [self dispatchEvent:_value];
}

@end

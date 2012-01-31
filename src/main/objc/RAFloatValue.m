//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import "RAFloatValue.h"
#import "RAFloatReactor+Protected.h"

@implementation RAFloatValue {
    float _value;
}

- (float)value { return _value; }

- (void)setValue:(float)value {
    if (value == _value) return;
    _value = value;
    [self dispatchEvent:_value];
}

@end

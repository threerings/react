//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import "RAFloatReactor.h"

@interface RAFloatValue : RAFloatReactor {
@protected
    float _value;
}

@property(nonatomic,readwrite) float value;
- (id)init;
- (id)initWithValue:(float)value;
@end

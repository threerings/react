//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import "RADoubleReactor.h"

@interface RADoubleValue : RADoubleReactor {
@protected
    double _value;
}

@property(nonatomic,readwrite) double value;
- (id)init;
- (id)initWithValue:(double)value;
@end

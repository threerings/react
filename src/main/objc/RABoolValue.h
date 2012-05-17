//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import "RABoolReactor.h"

@interface RABoolValue : RABoolReactor {
@protected
    BOOL _value;
}

@property(nonatomic,readwrite) BOOL value;
- (id)init;
- (id)initWithValue:(BOOL)value;
@end

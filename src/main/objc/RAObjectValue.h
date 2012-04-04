//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import "RAObjectReactor.h"

@interface RAObjectValue : RAObjectReactor
@property(nonatomic,readwrite,strong) id value;
- (id)init;
- (id)initWithValue:(id)value;
@end

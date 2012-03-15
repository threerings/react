//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import "RAConnection.h"

@interface RAConnection (package)
- (id)initWithBlock:(id)block atPriority:(int)priority onReactor:(RAReactor*)reactor;
@end

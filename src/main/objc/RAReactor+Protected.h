//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import <Foundation/Foundation.h>

@interface RAReactor (protected)
- (RAConnection*) withPriority:(int)priority connectConnection:(RAConnection*)connection;
- (RAConnection*)prepareForEmission;
- (void)finishEmission;
@end

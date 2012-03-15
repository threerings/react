//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import <Foundation/Foundation.h>

#define RA_IS_CONNECTED(connection) (connection->reactor != nil)

@interface RAReactor (protected)
- (RAConnection*)connectConnection:(RAConnection*)connection;
- (RAConnection*)prepareForEmission;
- (void)finishEmission;
@end

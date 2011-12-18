//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import <Foundation/Foundation.h>

@class RAReactor;

@interface RAConnection : NSObject {
    @package
        BOOL oneShot;
        RAReactor *signal;
        RAConnection *next;
        id block;
        int priority;
}

-(RAConnection*) once;
-(void) disconnect;

@end

//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE


#import "RAConnection.h"
#import "RAUnitSignal.h"

@implementation RAConnection

-(RAConnection*) once {
    oneShot = YES;
    return self;
}

-(void) disconnect {
    [signal disconnect:self];
}

@end

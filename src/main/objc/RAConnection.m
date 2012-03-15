//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import "RAConnection.h"
#import "RAConnection+Package.h"
#import "RAReactor.h"

@implementation RAConnection

- (RAConnection*)once {
    oneShot = YES;
    return self;
}

- (void)disconnect {
    [reactor disconnect:self];
}

@end

@implementation RAConnection(package)
- (id)initWithBlock:(id)newblock atPriority:(int)newpriority onReactor:(RAReactor*)newreactor {
    if (!(self = [super init])) return nil;
    block = newblock;
    priority = newpriority;
    reactor = newreactor;
    return self;
}
@end

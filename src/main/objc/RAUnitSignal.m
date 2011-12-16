//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import "RAUnitSignal.h"

@interface Cons : NSObject {
    @public
    void(^listener)(void);
    Cons *next;
}
@end

@implementation Cons
@end

@implementation RAUnitSignal {
    Cons *head;
}

- (void) emit {
    for (Cons *cur = head; cur != nil; cur = cur->next) cur->listener();
}

- (RAConnection*) connectBlock:(void (^)(void))block {
    Cons *cons = [[Cons alloc] init];
    cons->listener = [block copy];
    if (head) cons->next = head;
    head = cons;
    return nil;
}

@end

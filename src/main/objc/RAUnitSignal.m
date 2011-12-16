//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import "RAUnitSignal.h"
#import "RAConnection.h"

@implementation RAUnitSignal {
    RAConnection *head;
}

- (void) emit {
    for (RAConnection *cur = head; cur != nil; cur = cur->next) {
        cur->listener();
        if (cur->oneShot) [cur disconnect];
    }

}

- (RAConnection*) connectBlock:(void (^)(void))block {
    RAConnection *cons = [[RAConnection alloc] init];
    cons->listener = [block copy];
    cons->signal = self;
    if (head) cons->next = head;
    head = cons;
    return head;
}

- (void) disconnect:(RAConnection*)conn {
    // TODO - defer this if an emission is in progress
    if (conn == head) {
        head = head->next;
        return;
    }
    RAConnection *prev = head;
    for (RAConnection *cur = head->next; cur != nil; cur = cur->next) {
        if (cur == conn) {
            prev->next = cur->next;
            return;
        }
    }

}

@end

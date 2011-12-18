//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import "RAUnitSignal.h"
#import "RAConnection.h"
#import "RAConnectionGroup.h"


@interface PostDispatchAction : NSObject {
    @public
    void (^action)(void);
    PostDispatchAction *next;
}
@end
@implementation PostDispatchAction
- (id)initWithAction:(RAUnitBlock)postaction {
    if (!(self = [super init])) return nil;
    self->action = postaction;
    return self;
}
- (void)insertAction:(RAUnitBlock)newaction {
    if (next) [next insertAction:newaction];
    else next = [[PostDispatchAction alloc] initWithAction:newaction];
}
@end

@implementation RAUnitSignal {
    RAConnection *head;
    PostDispatchAction *pending;
}

- (void) emit {
    NSAssert(pending == nil, @"Asked to emit while emission in progress");
    pending = [[PostDispatchAction alloc] initWithAction:^{ }];
    for (RAConnection *cur = head; cur != nil; cur = cur->next) {
        cur->listener();
        if (cur->oneShot) [cur disconnect];
    }
    for (; pending != nil; pending = pending->next) {
        pending->action();
    }
    pending = nil;
}

void insertConn(RAConnection* conn,  RAConnection* head);
void insertConn(RAConnection* conn,  RAConnection* head) {
    if (head->next) insertConn(conn, head->next);
    else head->next = conn;
}

- (void) insertConn:(RAConnection*)conn {
    if (!head) head = conn;
    else insertConn(conn, head);
}

- (RAConnection*) connectBlock:(RAUnitBlock)block {
    RAConnection *cons = [[RAConnection alloc] init];
    cons->listener = [block copy];
    cons->signal = self;
    if (pending != nil) [pending insertAction:^{ [self insertConn:cons]; }];
    else [self insertConn:cons];
    return cons;
}

- (void) removeConn:(RAConnection*)conn {
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
        prev = cur;
    }
}

- (void) disconnect:(RAConnection*)conn {
    if (pending != nil) [pending insertAction:^{ [self removeConn:conn]; }];
    else [self removeConn:conn];
}

@end

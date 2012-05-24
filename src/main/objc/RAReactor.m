//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import "RAReactor.h"
#import "RAReactor+Protected.h"
#import "RAConnection.h"

@interface PostDispatchAction : NSObject {
@public
    RAUnitBlock action;
    PostDispatchAction *next;
}
@end
@implementation PostDispatchAction
- (id)initWithAction:(RAUnitBlock)postaction {
    if (!(self = [super init])) return nil;
    self->action = [postaction copy];
    return self;
}
- (void)insertAction:(RAUnitBlock)newaction {
    if (next) [next insertAction:newaction];
    else next = [[PostDispatchAction alloc] initWithAction:newaction];
}
@end

@interface RAReactor () {
@protected
    RAConnection *head;
    PostDispatchAction *pending;
}

@end

static void insertConn(RAConnection* conn,  RAConnection* head) {
    if (head->next && head->next->priority >= conn->priority) insertConn(conn, head->next);
    else {
        conn->next = head->next;
        head->next = conn;
    }
}

@implementation RAReactor

- (void)insertConn:(RAConnection*)conn {
    if (!head || conn->priority > head->priority) {
        conn->next = head;
        head = conn;
    } else insertConn(conn, head);
}

- (void)removeConn:(RAConnection*)conn {
    if (head == nil) return;
    else if (conn == head) {
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

- (void)disconnect:(RAConnection*)conn {
    if (RA_IS_CONNECTED(conn)) {
        // mark the connection as disconnected by nilling out the reactor reference
        conn->reactor = nil;
        
        if (pending != nil) {
            [pending insertAction:^{ [self removeConn:conn]; }];
        }
        else [self removeConn:conn];
    }
}

- (void)disconnectAll {
    for (RAConnection* cur = head; cur != nil; cur = cur->next) {
        cur->reactor = nil;
    }
    
    if (pending != nil) {
        [pending insertAction:^{ self->head = nil; }];
    }
    else head = nil;
}

- (RAConnection*)connectUnit:(RAUnitBlock)block {
    @throw [NSException exceptionWithName:NSInternalInconsistencyException
        reason:[NSString stringWithFormat:@"You must override %@ in a subclass",
        NSStringFromSelector(_cmd)] userInfo:nil];
}

- (RAConnection*)withPriority:(int)priority connectUnit:(RAUnitBlock)block {
    @throw [NSException exceptionWithName:NSInternalInconsistencyException
        reason:[NSString stringWithFormat:@"You must override %@ in a subclass",
        NSStringFromSelector(_cmd)] userInfo:nil];
}
@end

@implementation RAReactor (protected)
- (RAConnection*)connectConnection:(RAConnection*)connection {
    if (pending != nil) {
        [pending insertAction:^{
            // ensure the connection hasn't already been disconnected
            if (RA_IS_CONNECTED(connection)) {
                [self insertConn:connection];
            }
        }];
    }
    else [self insertConn:connection];
    return connection;

}
- (RAConnection*)prepareForEmission {
    NSAssert(pending == nil, @"Asked to emit while emission in progress");
    pending = [[PostDispatchAction alloc] initWithAction:^{ }];
    return head;
}

- (void)finishEmission {
    for (; pending != nil; pending = pending->next) pending->action();
    pending = nil;
}

@end

//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import "RAObjectSignal.h"
#import "RAObjectReactor+Protected.h"

@implementation RAObjectSignal
- (void)emitEvent:(id)event {
    [self dispatchEvent:event];
}
@end

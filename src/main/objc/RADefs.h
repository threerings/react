//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

#import <Foundation/Foundation.h>
#define RA_DEFAULT_PRIORITY 0

typedef void (^RAUnitBlock)(void);
typedef void (^RABoolSlot)(BOOL);
typedef void (^RADoubleSlot)(double);
typedef void (^RAObjectSlot)(id);
typedef void (^RAFloatSlot)(float);
typedef void (^RAIntSlot)(int);

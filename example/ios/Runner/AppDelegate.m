// Copyright 2014 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#import "AppDelegate.h"
#import <Flutter/Flutter.h>
#import "GeneratedPluginRegistrant.h"

#if __has_include(<flutter_callkit_incoming/flutter_callkit_incoming-Swift.h>)
#import <flutter_callkit_incoming/flutter_callkit_incoming-Swift.h>
#else
#import "flutter_callkit_incoming-Swift.h"
#endif

@implementation AppDelegate

- (BOOL)application:(UIApplication*)application
    didFinishLaunchingWithOptions:(NSDictionary*)launchOptions {
  [GeneratedPluginRegistrant registerWithRegistry:self];
    
    
    NSTimeInterval delayInSeconds = 5.0;
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
    dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
        Data * data = [[Data alloc]initWithId:@"44d915e1-5ff4-4bed-bf13-c423048ec97a" nameCaller:@"Hien Nguyen" handle:@"0123456789" type:1];
        [SwiftFlutterCallkitIncomingPlugin.sharedInstance showCallkitIncoming:data fromPushKit:YES];
    });

  return [super application:application didFinishLaunchingWithOptions:launchOptions];
}


@end

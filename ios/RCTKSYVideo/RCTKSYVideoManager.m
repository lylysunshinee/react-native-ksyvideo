//
//  RCTKSYVideoManager.m
//  RCTKSYVideo
//
//  Created by mayudong on 2017/11/27.
//  Copyright © 2017年 mayudong. All rights reserved.
//

#import "RCTKSYVideoManager.h"
#import "RCTKSYVideo.h"

@implementation RCTKSYVideoManager

RCT_EXPORT_MODULE()

@synthesize bridge = _bridge;

- (UIView *)view
{
    return [[RCTKSYVideo alloc] initWithEventDispatcher:self.bridge.eventDispatcher];
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

RCT_EXPORT_VIEW_PROPERTY(src, NSDictionary);
RCT_EXPORT_VIEW_PROPERTY(seek, float);
RCT_EXPORT_VIEW_PROPERTY(timeout, NSDictionary);
RCT_EXPORT_VIEW_PROPERTY(bufferTime, float);
RCT_EXPORT_VIEW_PROPERTY(bufferSize, float);
RCT_EXPORT_VIEW_PROPERTY(resizeMode, NSString);
RCT_EXPORT_VIEW_PROPERTY(repeat, BOOL);
RCT_EXPORT_VIEW_PROPERTY(paused, BOOL);
RCT_EXPORT_VIEW_PROPERTY(muted, BOOL);
RCT_EXPORT_VIEW_PROPERTY(mirror, BOOL);
RCT_EXPORT_VIEW_PROPERTY(volume, float);
RCT_EXPORT_VIEW_PROPERTY(degree, int);
RCT_EXPORT_VIEW_PROPERTY(playInBackground, BOOL);
//RCT_EXPORT_VIEW_PROPERTY(playWhenInactive, BOOL);


RCT_EXPORT_VIEW_PROPERTY(onVideoTouch, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onVideoLoadStart, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onVideoLoad, RCTBubblingEventBlock);

RCT_EXPORT_VIEW_PROPERTY(onVideoError, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onVideoProgress, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onVideoSeek, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onVideoEnd, RCTBubblingEventBlock);

RCT_EXPORT_VIEW_PROPERTY(onReadyForDisplay, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onPlaybackStalled, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onPlaybackResume, RCTBubblingEventBlock);



//供js调用的函数，暂时没有实现
RCT_EXPORT_METHOD(saveBitmap:data){
}
RCT_EXPORT_METHOD(recordVideo:data){
}
RCT_EXPORT_METHOD(stopRecordVideo:data){
}

@end

//
//  RCTKSYVideo.h
//  RCTKSYVideo
//
//  Created by mayudong on 2017/11/27.
//  Copyright © 2017年 mayudong. All rights reserved.
//

#import <React/RCTView.h>
#import <UIKit/UIKit.h>

@class RCTEventDispatcher;

@interface RCTKSYVideo : UIView

@property (nonatomic, copy) RCTBubblingEventBlock onVideoLoadStart;
@property (nonatomic, copy) RCTBubblingEventBlock onVideoLoad;

@property (nonatomic, copy) RCTBubblingEventBlock onVideoError;
@property (nonatomic, copy) RCTBubblingEventBlock onVideoProgress;
@property (nonatomic, copy) RCTBubblingEventBlock onVideoSeek;
@property (nonatomic, copy) RCTBubblingEventBlock onVideoEnd;
@property (nonatomic, copy) RCTBubblingEventBlock onVideoTouch;

@property (nonatomic, copy) RCTBubblingEventBlock onReadyForDisplay;
@property (nonatomic, copy) RCTBubblingEventBlock onPlaybackStalled;
@property (nonatomic, copy) RCTBubblingEventBlock onPlaybackResume;

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher NS_DESIGNATED_INITIALIZER;

@end

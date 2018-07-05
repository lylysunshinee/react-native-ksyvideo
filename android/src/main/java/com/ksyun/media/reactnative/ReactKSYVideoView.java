package com.ksyun.media.reactnative;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;
import com.ksyun.media.player.KSYMediaRecorder;
import com.ksyun.media.player.KSYTextureView;
import com.ksyun.media.player.recorder.KSYMediaRecorderConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by dengchu on 2017/10/26.
 */

public class ReactKSYVideoView extends RelativeLayout implements LifecycleEventListener, MediaController.MediaPlayerControl{

    public enum Events {
        EVENT_TOUCH("onVideoTouch"),
        EVENT_LOAD_START("onVideoLoadStart"),
        EVENT_LOAD("onVideoLoad"),
        EVENT_ERROR("onVideoError"),
        EVENT_PROGRESS("onVideoProgress"),
        EVENT_SEEK("onVideoSeek"),
        EVENT_END("onVideoEnd"),
        EVENT_STALLED("onPlaybackStalled"),
        EVENT_RESUME("onPlaybackResume"),
        EVENT_READY_FOR_DISPLAY("onReadyForDisplay");

        private final String mName;

        Events(final String name) {
            mName = name;
        }

        @Override
        public String toString() {
            return mName;
        }
    }

    public static final String EVENT_PROP_FAST_FORWARD = "canPlayFastForward";
    public static final String EVENT_PROP_SLOW_FORWARD = "canPlaySlowForward";
    public static final String EVENT_PROP_SLOW_REVERSE = "canPlaySlowReverse";
    public static final String EVENT_PROP_REVERSE = "canPlayReverse";
    public static final String EVENT_PROP_STEP_FORWARD = "canStepForward";
    public static final String EVENT_PROP_STEP_BACKWARD = "canStepBackward";

    public static final String EVENT_PROP_DURATION = "duration";
    public static final String EVENT_PROP_PLAYABLE_DURATION = "playableDuration";
    public static final String EVENT_PROP_CURRENT_TIME = "currentTime";
    public static final String EVENT_PROP_SEEK_TIME = "seekTime";
    public static final String EVENT_PROP_NATURALSIZE = "naturalSize";
    public static final String EVENT_PROP_WIDTH = "width";
    public static final String EVENT_PROP_HEIGHT = "height";
    public static final String EVENT_PROP_ORIENTATION = "orientation";

    public static final String EVENT_PROP_ERROR = "error";
    public static final String EVENT_PROP_WHAT = "what";
    public static final String EVENT_PROP_EXTRA = "extra";

    private static final String TAG = "React VideoView";
    public static final int UPDATE_PROGRESS = 0;
    private String mDataSource;
    private KSYTextureView ksyTextureView;
    private Handler mHandler;
    private int mVideoWidth;
    private int mVideoHeight;
    private File imageFile;
    private int mResizeMode = KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT;
    private float mProgressUpdateInterval = 250.0f;
    private ThemedReactContext mThemedReactContext;
    private RCTEventEmitter mEventEmitter;
    private boolean mUseNativeControls = false;
    private Handler videoControllerHandler = new Handler();
    private MediaController mediaController;
    private long mVideoBufferedDuration = 0;
    private long mVideoDuration = 0;
    private boolean mPaused = false;
    private boolean mRepeat = false;
    private boolean mPlayInBackground = false;
    private boolean mMuted = false;
    private float mVolume = 0.5f;
    private boolean mMirror = false;
    private int mDegree = 0;
    private int mPrepareTimeout = 5;
    private int mReadTimeout = 30;
    private int mBufferSize = 15;
    private int mBufferTime = 2;
    @Override
    public void requestLayout() {
        super.requestLayout();

        // The spinner relies on a measure + layout pass happening after it calls requestLayout().
        // Without this, the widget never actually changes the selection and doesn't call the
        // appropriate listeners. Since we override onLayout in our ViewGroups, a layout pass never
        // happens after a call to requestLayout, so we simulate one here.
        post(measureAndLayout);
    }

    private final Runnable measureAndLayout = new Runnable() {
        @Override
        public void run() {
            measure(
                    MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY));
            layout(getLeft(), getTop(), getRight(), getBottom());
        }
    };

    public ReactKSYVideoView(Context context) {
        super(context);
        init(context);
    }

    public ReactKSYVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ReactKSYVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void Release(){
        if (ksyTextureView!=null){
            ksyTextureView.stop();
            ksyTextureView.release();
        }
    }

    public int setVideoProgress(int currentProgress) {
        if (ksyTextureView == null)
            return 0;
        long duration = ksyTextureView.getDuration();
        long time = 0;
        if (duration > 0) {

            time = currentProgress > 0 ? currentProgress : ksyTextureView.getCurrentPosition();

            WritableMap event = Arguments.createMap();
            event.putDouble(EVENT_PROP_CURRENT_TIME, time / 1000.0);
            event.putDouble(EVENT_PROP_PLAYABLE_DURATION, mVideoBufferedDuration / 1000.0); //TODO:mBufferUpdateRunnable
            mEventEmitter.receiveEvent(getId(), Events.EVENT_PROGRESS.toString(), event);

            Message msg = new Message();
            msg.what = UPDATE_PROGRESS;
            if (mHandler != null)
                mHandler.sendMessageDelayed(msg, Math.round(mProgressUpdateInterval));
        }
        return (int)time;
    }

    private IMediaPlayer.OnLogEventListener mOnLogEventListener = new IMediaPlayer.OnLogEventListener(){
        @Override
        public void onLogEvent(IMediaPlayer var1, String var2)
        {
            Log.e(TAG, var2);
        }
    };

    private IMediaPlayer.OnPreparedListener mOnPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer mp) {
            if (ksyTextureView != null) {
                // 开始播放视频
                ksyTextureView.start();

                mVideoDuration = ksyTextureView.getDuration();
                mVideoWidth = ksyTextureView.getVideoWidth();
                mVideoHeight = ksyTextureView.getVideoHeight();
                if (mVideoDuration > 0) {
                    setVideoProgress(0);
                }

                WritableMap naturalSize = Arguments.createMap();
                naturalSize.putInt(EVENT_PROP_WIDTH, mVideoWidth);
                naturalSize.putInt(EVENT_PROP_HEIGHT, mVideoHeight);
                if (mp.getVideoWidth() > mp.getVideoHeight())
                    naturalSize.putString(EVENT_PROP_ORIENTATION, "landscape");
                else
                    naturalSize.putString(EVENT_PROP_ORIENTATION, "portrait");

                WritableMap event = Arguments.createMap();
                event.putDouble(EVENT_PROP_DURATION, mVideoDuration / 1000.0);
                event.putDouble(EVENT_PROP_CURRENT_TIME, mp.getCurrentPosition() / 1000.0);
                event.putMap(EVENT_PROP_NATURALSIZE, naturalSize);
                // TODO: Actually check if you can.
                event.putBoolean(EVENT_PROP_FAST_FORWARD, true);
                event.putBoolean(EVENT_PROP_SLOW_FORWARD, true);
                event.putBoolean(EVENT_PROP_SLOW_REVERSE, true);
                event.putBoolean(EVENT_PROP_REVERSE, true);
                event.putBoolean(EVENT_PROP_FAST_FORWARD, true);
                event.putBoolean(EVENT_PROP_STEP_BACKWARD, true);
                event.putBoolean(EVENT_PROP_STEP_FORWARD, true);
                mEventEmitter.receiveEvent(getId(), Events.EVENT_LOAD.toString(), event);
                applyModifiers();
                if (mUseNativeControls) {
                    initializeMediaControllerIfNeeded();
                    mediaController.setMediaPlayer(ReactKSYVideoView.this);
                    mediaController.setAnchorView(ReactKSYVideoView.this);

                    videoControllerHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mediaController.setEnabled(true);
                            mediaController.show();
                        }
                    });
                }
            }
        }
    };

    private IMediaPlayer.OnCompletionListener mOnCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer mp) {
            mEventEmitter.receiveEvent(getId(), Events.EVENT_END.toString(), null);
        }
    };

    public IMediaPlayer.OnInfoListener mOnInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {

            switch (i) {
                case KSYMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    mEventEmitter.receiveEvent(getId(), Events.EVENT_STALLED.toString(), Arguments.createMap());
                    break;
                case KSYMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    mEventEmitter.receiveEvent(getId(), Events.EVENT_RESUME.toString(), Arguments.createMap());
                    break;
                case KSYMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    mEventEmitter.receiveEvent(getId(), Events.EVENT_READY_FOR_DISPLAY.toString(), Arguments.createMap());
                    break;
                case KSYMediaPlayer.MEDIA_INFO_SUGGEST_RELOAD:
                    if (ksyTextureView != null)
                        ksyTextureView.reload(mDataSource, true);
                    break;
                default:
            }
            return false;
        }
    };

    private IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(IMediaPlayer mp, int percent) {
            mVideoBufferedDuration = mVideoDuration * percent / 100;
        }
    };


    private IMediaPlayer.OnErrorListener mOnErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer mp, int what, int extra) {
            WritableMap error = Arguments.createMap();
            error.putInt(EVENT_PROP_WHAT, what);
            error.putInt(EVENT_PROP_EXTRA, extra);
            WritableMap event = Arguments.createMap();
            event.putMap(EVENT_PROP_ERROR, error);
            mEventEmitter.receiveEvent(getId(), Events.EVENT_ERROR.toString(), event);
            return false;
        }
    };

    private void init(Context context) {

        mThemedReactContext = (ThemedReactContext) context;
        mEventEmitter = mThemedReactContext.getJSModule(RCTEventEmitter.class);
        mThemedReactContext.addLifecycleEventListener(this);
//        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
//        layoutParams.gravity = Gravity.CENTER;
//        ksyTextureView = new KSYTextureView(context);//(KSYTextureView) this.findViewById(R.id.ksy_textureview);
//        ksyTextureView.setLayoutParams(layoutParams);
        LayoutInflater.from(getContext()).inflate(R.layout.mediaplayer_layout, this, true);

        ksyTextureView = (KSYTextureView) this.findViewById(R.id.ksy_textureview);
        ksyTextureView.setKeepScreenOn(true);
        ksyTextureView.setScreenOnWhilePlaying(true);
        ksyTextureView.setOnPreparedListener(mOnPreparedListener);
        ksyTextureView.setOnCompletionListener(mOnCompletionListener);
        ksyTextureView.setOnInfoListener(mOnInfoListener);
        ksyTextureView.setOnErrorListener(mOnErrorListener);
        ksyTextureView.setOnLogEventListener(mOnLogEventListener);
        ksyTextureView.setOnBufferingUpdateListener(mOnBufferingUpdateListener);

        imageFile = new File(Environment.getExternalStorageDirectory(), "DCIM/image");

        if (!imageFile.exists()) {
            imageFile.mkdir();
        }

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case UPDATE_PROGRESS:
                        setVideoProgress(0);
                        break;
                }
            }
        };
    }

    private void initializeMediaControllerIfNeeded() {
        if (mediaController == null) {
            mediaController = new MediaController(this.getContext());
        }
    }

    public void cleanupMediaPlayerResources() {
        if (mediaController != null) {
            mediaController.hide();
        }
    }

    public void saveBitmap() {
        String imageName = System.currentTimeMillis() + ".jpg";
        File file = new File(imageFile, imageName);
        FileOutputStream outputStream;
        Bitmap bitmap = ksyTextureView.getScreenShot();
        Context context = getContext();
        while (!(context instanceof Activity) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            //图片保存到相册
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), imageName, null);
            Uri uri = Uri.fromFile(file);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(context, "截图保存至相册!", Toast.LENGTH_SHORT).show();

    }

    public void setDataSource(String url) {
        WritableMap src = Arguments.createMap();
        src.putString(ReactKSYVideoViewManager.PROP_SRC_URI, url);

        WritableMap event = Arguments.createMap();
        event.putMap(ReactKSYVideoViewManager.PROP_SRC, src);
        mEventEmitter.receiveEvent(getId(), Events.EVENT_LOAD_START.toString(), event);
        mDataSource = url;
        try {
            ksyTextureView.setDataSource(url);
            ksyTextureView.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPausedModifier(boolean paused) {
        mPaused = paused;
        if (ksyTextureView != null) {
            if (mPaused)
                ksyTextureView.pause();
            else
                ksyTextureView.start();
        }
    }

    public void setResizeModeModifier(int resizeMode) {
        mResizeMode = resizeMode;
        if (ksyTextureView != null)
            ksyTextureView.setVideoScalingMode(mResizeMode);
    }

    public void setRotateDegree(int degree) {
        mDegree = degree;
        if (ksyTextureView != null)
            ksyTextureView.setRotateDegree(degree);
    }

    public void setMirror(boolean mirror){
        mMirror = mirror;
        if (ksyTextureView != null)
            ksyTextureView.setMirror(mirror);
    }

    public void seekToModifier(long time){

        if (ksyTextureView != null) {
            WritableMap event = Arguments.createMap();
            event.putDouble(EVENT_PROP_CURRENT_TIME, ksyTextureView.getCurrentPosition() / 1000.0);
            event.putDouble(EVENT_PROP_SEEK_TIME, time / 1000.0);
            mEventEmitter.receiveEvent(getId(), Events.EVENT_SEEK.toString(), event);

            ksyTextureView.seekTo(time);
            //super.seekTo(time);
        }
    }

    public void setMutedModifier(boolean mute){
        mMuted = mute;
        if (ksyTextureView != null) {
            if (mMuted) {
                ksyTextureView.setPlayerMute(1);
            }
            else{
                ksyTextureView.setPlayerMute(0);
                ksyTextureView.setVolume(mVolume, mVolume);
            }
        }
    }

    public void setVolumeModifier(float volume){
        mVolume = volume;
        setMutedModifier(mMuted);
    }

    public void setRepeatModifier(boolean repeat){
        mRepeat = repeat;
        if(ksyTextureView != null)
            ksyTextureView.setLooping(repeat);
    }

    public void setProgressUpdateInterval(float progressUpdateInterval){
        mProgressUpdateInterval = progressUpdateInterval;
    }

    public void setPlayInBackground(boolean background){
        mPlayInBackground = background;
    }

    public void setControls(boolean controls) {
        mUseNativeControls = controls;
    }

    public void setTimeout(int prepareTimeout, int readTimeout){
        if (mPrepareTimeout > 0 && mReadTimeout >0){
            mPrepareTimeout = prepareTimeout;
            mReadTimeout = readTimeout;
            if (ksyTextureView != null)
                ksyTextureView.setTimeout(mPrepareTimeout, mReadTimeout);
        }
    }

    public void setBufferSize(int bufferSize){
        if (mBufferSize > 0){
            mBufferSize = bufferSize;
            if (ksyTextureView != null)
                ksyTextureView.setBufferSize(mBufferSize);
        }
    }

    public void setBufferTime(int bufferTime){
        if (mBufferTime > 0){
            mBufferTime = bufferTime;
            if (ksyTextureView != null)
                ksyTextureView.setBufferTimeMax(mBufferTime);
        }
    }

    public void applyModifiers() {
        setResizeModeModifier(mResizeMode);
        setRepeatModifier(mRepeat);
        setPausedModifier(mPaused);
        setMutedModifier(mMuted);
        setMirror(mMirror);
        setRotateDegree(mDegree);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mUseNativeControls) {
            initializeMediaControllerIfNeeded();
            mediaController.show();
        }

        if(event.getAction()==MotionEvent.ACTION_DOWN)
            mEventEmitter.receiveEvent(getId(), Events.EVENT_TOUCH.toString(), Arguments.createMap());
        return super.onTouchEvent(event);
    }

    /*LifecycleEventListener*/
    @Override
    public void onHostPause() {
        if (ksyTextureView != null) {
            ksyTextureView.runInBackground(mPlayInBackground);
        }
    }

    @Override
    public void onHostResume() {
        if (ksyTextureView != null ) {
            ksyTextureView.runInForeground();
            if (!mPaused)
                ksyTextureView.start();
        }
    }

    @Override
    public void onHostDestroy() {
    }

    /*MediaPlayControl*/
    @Override
    public void start(){
        if (ksyTextureView != null)
            ksyTextureView.start();
    }

    @Override
    public void  pause(){
        if (ksyTextureView != null)
            ksyTextureView.pause();
    }

    @Override
    public boolean isPlaying(){
        if (ksyTextureView != null)
            return ksyTextureView.isPlaying();
        return false;
    }

    @Override
    public void seekTo(int pos) {
        seekToModifier(pos);
    }

    @Override
    public int getDuration(){
        if (ksyTextureView != null)
            return (int)ksyTextureView.getDuration();
        return 0;
    }

    @Override
    public int getCurrentPosition(){
        if (ksyTextureView != null)
            return (int)ksyTextureView.getCurrentPosition();
        return 0;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

}

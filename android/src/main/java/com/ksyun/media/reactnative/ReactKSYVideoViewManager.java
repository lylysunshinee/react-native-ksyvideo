package com.ksyun.media.reactnative;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.ksyun.media.player.KSYMediaPlayer;

import java.util.Map;

import javax.annotation.Nullable;

/**
 * Created by dengchu on 2017/10/26.
 */

public class ReactKSYVideoViewManager extends SimpleViewManager<ReactKSYVideoView> {

    public static final String REACT_CLASS = "RCTKSYVideo";

    public static final String PROP_SRC = "src";
    public static final String PROP_SRC_URI = "uri";
    public static final String PROP_SRC_TYPE = "type";
    public static final String PROP_SRC_IS_NETWORK = "isNetwork";
    public static final String PROP_SRC_MAINVER = "mainVer";
    public static final String PROP_SRC_PATCHVER = "patchVer";
    public static final String PROP_SRC_IS_ASSET = "isAsset";
    public static final String PROP_RESIZE_MODE = "resizeMode";
    public static final String PROP_REPEAT = "repeat";
    public static final String PROP_PAUSED = "paused";
    public static final String PROP_MUTED = "muted";
    public static final String PROP_VOLUME = "volume";
    public static final String PROP_PROGRESS_UPDATE_INTERVAL = "progressUpdateInterval";
    public static final String PROP_SEEK = "seek";
    public static final String PROP_RATE = "rate";
    public static final String PROP_PLAY_IN_BACKGROUND = "playInBackground";
    public static final String PROP_CONTROLS = "controls";
    public static final String PROP_MIRROR = "mirror";
    public static final String PROP_DEGREE = "degree";
    public static final String PROP_TIMEOUT = "timeout";
    public static final String PROP_PREPARETIMEOUT = "prepareTimeout";
    public static final String PROP_READTIMEOUT = "readTimeout";
    public static final String PROP_BUFFERTIME = "bufferTime";
    public static final String PROP_BUFFERSIZE = "bufferSize";

    private static final int COMMAND_SAVEBITMAP_ID = 1;
    private static final String COMMAND_SAVEBITMAP_NAME = "saveBitmap";
    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected ReactKSYVideoView createViewInstance(final ThemedReactContext reactContext) {
        final ReactKSYVideoView mVideoView = new ReactKSYVideoView(reactContext);
        return mVideoView;
    }

    @Override
    public void onDropViewInstance(ReactKSYVideoView view) {//销毁对象
        super.onDropViewInstance(view);
        view.cleanupMediaPlayerResources();
        view.Release();
    }

    @Override
    @Nullable
    public Map getExportedCustomDirectEventTypeConstants() {
        MapBuilder.Builder builder = MapBuilder.builder();
        for (ReactKSYVideoView.Events event : ReactKSYVideoView.Events.values()) {
            builder.put(event.toString(), MapBuilder.of("registrationName", event.toString()));
        }
        return builder.build();
    }

    @Nullable
    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of(
                COMMAND_SAVEBITMAP_NAME, COMMAND_SAVEBITMAP_ID
        );
    }

    @Override
    public void receiveCommand(ReactKSYVideoView video, int commandId, @Nullable ReadableArray args) {
        switch (commandId){
            case COMMAND_SAVEBITMAP_ID:
                video.saveBitmap();
                break;
            default:
                break;
        }
    }

    @ReactProp(name = PROP_SRC)
    public void setSource(ReactKSYVideoView videoView, @Nullable ReadableMap src){
        String source = src.getString(PROP_SRC_URI);
        videoView.setDataSource(source);
    }

    @ReactProp(name = PROP_RESIZE_MODE)
    public void setResizeMode(final ReactKSYVideoView videoView, final String resizeModeOrdinalString) {
        if (resizeModeOrdinalString.equals("stretch"))
            videoView.setResizeModeModifier(KSYMediaPlayer.VIDEO_SCALING_MODE_NOSCALE_TO_FIT);
        else if (resizeModeOrdinalString.equals("cover"))
            videoView.setResizeModeModifier(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        else if(resizeModeOrdinalString.equals("contain"))
            videoView.setResizeModeModifier(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
    }

    @ReactProp(name = PROP_PAUSED, defaultBoolean = false)
    public void setPause (ReactKSYVideoView videoView, @Nullable boolean paused){
        videoView.setPausedModifier(paused);
    }

    @ReactProp(name = PROP_MIRROR, defaultBoolean = false)
    public void setMirror(ReactKSYVideoView videoView, @Nullable boolean mirror){
        videoView.setMirror(mirror);
    }

    @ReactProp(name = PROP_DEGREE, defaultInt = 0)
    public void setDegree(ReactKSYVideoView videoView, @Nullable int degree){
        videoView.setRotateDegree(degree);
    }

    @ReactProp(name = PROP_MUTED, defaultBoolean = false)
    public void setMuted(final ReactKSYVideoView videoView, final boolean muted) {
        videoView.setMutedModifier(muted);
    }

    @ReactProp(name = PROP_VOLUME, defaultFloat = 0.5f)
    public void setVolumn(ReactKSYVideoView videoView, @Nullable double volume){
        videoView.setVolumeModifier((float) volume);
    }


    @ReactProp(name = PROP_REPEAT, defaultBoolean = false)
    public void setRepeat(final ReactKSYVideoView videoView, final boolean repeat) {
        videoView.setRepeatModifier(repeat);
    }

    @ReactProp(name = PROP_PROGRESS_UPDATE_INTERVAL, defaultFloat = 250.0f)
    public void setProgressUpdateInterval(final ReactKSYVideoView videoView, final float progressUpdateInterval) {
        videoView.setProgressUpdateInterval(progressUpdateInterval);
    }

    @ReactProp(name = PROP_SEEK)
    public void setSeek(final ReactKSYVideoView videoView, final float seek) {
        videoView.seekToModifier(Math.round(seek * 1000.0f));
    }

    @ReactProp(name = PROP_PLAY_IN_BACKGROUND, defaultBoolean = false)
    public void setPlayInBackground(final ReactKSYVideoView videoView, final boolean playInBackground) {
        videoView.setPlayInBackground(playInBackground);
    }

    @ReactProp(name = PROP_CONTROLS, defaultBoolean = false)
    public void setControls(final ReactKSYVideoView videoView, final boolean controls) {
        videoView.setControls(controls);
    }

    @ReactProp(name = PROP_TIMEOUT)
    public void setTimeout(final ReactKSYVideoView videoView, @Nullable ReadableMap timeout) {
        int prepareTimeout = timeout.getInt(PROP_PREPARETIMEOUT);
        int readTimeout = timeout.getInt(PROP_READTIMEOUT);
        videoView.setTimeout(prepareTimeout, readTimeout);
    }

    @ReactProp(name = PROP_BUFFERSIZE, defaultInt = 15)
    public void setBufferSize(final ReactKSYVideoView videoView, final int bufferSize) {
        videoView.setBufferSize(bufferSize);
    }

    @ReactProp(name = PROP_BUFFERTIME,  defaultInt = 2)
    public void setBufferTime(final ReactKSYVideoView videoView, final int bufferTime) {
        videoView.setBufferTime(bufferTime);
    }
}
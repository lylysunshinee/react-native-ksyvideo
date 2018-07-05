'use strict';
import React,{ Component}from 'react';
import PropTypes from 'prop-types';
import {
    requireNativeComponent,
    View,
    UIManager,
    findNodeHandle,
    StyleSheet,
    ViewPropTypes
}from 'react-native';
var RCT_VIDEO_REF = 'KSYVideo';
const KSYViewPropType = ViewPropTypes || View.propTypes;
export default class KSYVideo extends Component {
    constructor(props) {
        super(props);
    }

    componentDidMount() {
        const source = this.props.source;
        let uri = source.uri;
        this.setNativeProps({ src: {uri} });
    }

    setNativeProps(nativeProps) {
        this.refs[RCT_VIDEO_REF].setNativeProps(nativeProps);
    }

    seek = (time) => {
        this.setNativeProps({ seek: time });
    };

    _onTouch = (event)=>{
        if (!this.props.onTouch){
            return;
        }
        this.props.onTouch();
    }

    _onLoadStart = (event)=>{
        if (!this.props.onLoadStart)
        {
            return;
        }
        this.props.onLoadStart(event.nativeEvent);
    }

    _onLoad = (event)=>{
        if(!this.props.onLoad){
            return;
        }
        this.props.onLoad(event.nativeEvent);
    }
    
    _onEnd = (event)=>{
        if (!this.props.onEnd){
            return;
        }
        this.props.onEnd(event.nativeEvent);
    }

    _onError = (event)=>{
        if(!this.props.onError){
            return;
        }
        this.props.onError(event.nativeEvent);
    }

    _onProgress = (event)=>{
        if(!this.props.onProgress){
            return;
        }
        this.props.onProgress(event.nativeEvent);
    }

    _onSeek = (event) => {
        if (this.props.onSeek) {
          this.props.onSeek(event.nativeEvent);
        }
    };

    _onReadyForDisplay = (event) => {
        if (this.props.onReadyForDisplay) {
            this.props.onReadyForDisplay(event.nativeEvent);
        }
    };

    _onPlaybackStalled = (event) => {
        if (this.props.onPlaybackStalled) {
            this.props.onPlaybackStalled(event.nativeEvent);
        }
    };

    _onPlaybackResume = (event) => {
        if (this.props.onPlaybackResume) {
            this.props.onPlaybackResume(event.nativeEvent);
        }
    };

    saveBitmap(){
        UIManager.dispatchViewManagerCommand(
            findNodeHandle(this.refs[RCT_VIDEO_REF]),
            UIManager.RCTKSYVideo.Commands.saveBitmap,
            null
        );
    }

    render(){
        const nativeProps = Object.assign({}, this.props);
        Object.assign(nativeProps, {
           
            onVideoTouch:this._onTouch,
            onVideoLoadStart: this._onLoadStart,
            onVideoLoad:this._onLoad,
            onVideoEnd:this._onEnd,
            onVideoError:this._onError,
            onVideoProgress:this._onProgress,
            onVideoSeek: this._onSeek,
            onReadyForDisplay: this._onReadyForDisplay,
            onPlaybackStalled: this._onPlaybackStalled,
            onPlaybackResume: this._onPlaybackResume,
        });

        return (  
                <RCTKSYVideo           
                    {...nativeProps}
                    ref = {RCT_VIDEO_REF}
                />
        );
    };
}
KSYVideo.propTypes = {
    /* Native only */
    style: KSYViewPropType.style,
    src: PropTypes.object,
    seek: PropTypes.number,
    onVideoTouch: PropTypes.func,
    onVideoLoadStart: PropTypes.func,
    onVideoLoad: PropTypes.func,
    onVideoEnd: PropTypes.func,
    onVideoError: PropTypes.func,
    onVideoProgress: PropTypes.func,
    onVideoSeek: PropTypes.func,

    /* Wrapper component */
    source: PropTypes.oneOfType([
        PropTypes.shape({
          uri: PropTypes.string
        }),
    ]),
    timeout:PropTypes.shape({
        prepareTimeout:PropTypes.number,
        readTimeout:PropTypes.number,
    }),
    bufferTime: PropTypes.number,
    bufferSize: PropTypes.number,

    resizeMode: PropTypes.string,
    repeat: PropTypes.bool,
    paused: PropTypes.bool,
    muted: PropTypes.bool,
    mirror: PropTypes.bool,
    volume: PropTypes.number,
    degree: PropTypes.number,
    playInBackground: PropTypes.bool,
    controls: PropTypes.bool,
    progressUpdateInterval: PropTypes.number,
    onTouch: PropTypes.func,
    onLoadStart: PropTypes.func,
    onLoad: PropTypes.func,
    onEnd: PropTypes.func,
    onError: PropTypes.func,
    onProgress: PropTypes.func,
    onSeek: PropTypes.func,
    onReadyForDisplay: PropTypes.func,
    onPlaybackStalled: PropTypes.func,
    onPlaybackResume: PropTypes.func,

    ...KSYViewPropType,
};

const RCTKSYVideo = requireNativeComponent('RCTKSYVideo',KSYVideo,{
   nativeOnly: {
    src: true,
    seek: true,
  },
});

require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-ksyvideo"
  s.version      = package["version"]
  s.summary      = "A <Video /> element for react-native"
  s.author       = "ksyun"

  s.homepage     = "https://github.com/ksvc"

  s.license      = "MIT"

  s.ios.deployment_target = "7.0"
  s.tvos.deployment_target = "9.0"

  s.source       = { :git => "https://github.com/ksvc/react-native-video-player", :tag => "#{s.version}" }

  s.source_files  = "ios/*.{h,m}"

  s.dependency "React"
  s.dependency "KSYMediaPlayer_iOS"
end

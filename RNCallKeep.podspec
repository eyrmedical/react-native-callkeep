require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name                = "RNCallKeep"
  s.version             = package['version']
  s.summary             = package['description']
  s.homepage            = package['homepage']
  s.license             = package['license']
  s.author              = package['author']
  s.source              = { :git => "https://github.com/eyrmedical/react-native-callkeep.git", :tag => "v#{s.version}" }
  s.requires_arc        = true
  s.platform            = :ios, "10.0"
  s.source_files        = "ios/RNCallKeep/*.{h,m,swift}"
  s.dependency 'React'
end


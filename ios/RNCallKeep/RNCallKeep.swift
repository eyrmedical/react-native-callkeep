import Foundation
import CallKit
import AVKit

let RNCallKeepDidLoadWithEvents = "RNCallKeepDidLoadWithEvents"
let RNCallKeepPerformEndCallAction = "RNCallKeepPerformEndCallAction"
let RNCallKeepAnswerCallAction = "RNCallKeepPerformAnswerCallAction"
let RNCallKeepDidPerformSetMutedCallAction = "RNCallKeepDidPerformSetMutedCallAction"

@objc(RNCallKeep)
public class RNCallKeep: RCTEventEmitter {
    // Cast objc block to swift block
    typealias ClosureType = @convention(block) () -> Void
    @objc static var options: [String: Any]?
    private let cxCallController = CXCallController()
    private let callKeepProvider: CXProvider
    // MARK: - Private params
    private var _hasListener = false
    private var _delayedEvents = [[String: Any]]()
    fileprivate var _answerCallAction: CXAnswerCallAction?
    fileprivate var _endCallAction: CXEndCallAction?
    fileprivate var _isRemoteEnded: Bool = false
    private let _settingsKey = "RNCallKeepSettings"
    // MARK: - Init
    public override init() {
        if let options = RNCallKeep.options {
            // Save default settings
            let standard = UserDefaults.standard
            standard.set(options, forKey: _settingsKey)
            print("[RNCallKeep][setup]")
        } else {
            print("[RNCallKeep][setup] Error: Options not set")
        }

        let settings = UserDefaults.standard.object(forKey: _settingsKey) as! [String: Any]
        let config = RNCallKeep.getProviderConfiguration(settings: settings)
        self.callKeepProvider = CXProvider(configuration: config)
        super.init()
        self.callKeepProvider.setDelegate(self, queue: nil)
        print("[RNCallKeep][setup] Successfully created the CXProvider")

        NotificationCenter.default.addObserver(self,
                                               selector: #selector(reportIncomingCall(notification:)),
                                               name: Notification.Name("RNCallKeep.reportIncomingCall"),
                                               object: nil)
    }

    @objc public class func setOptions(options: [String: Any]) {
        RNCallKeep.options = options
    }

    /// Return provider config
    /// - Parameter settings: List of available settings
    @objc(getProviderConfiguration:)
    class func getProviderConfiguration(settings: [String: Any]) -> CXProviderConfiguration {
        let appName: String
        if let appNameSetting = settings["appName"] as? String {
            appName = appNameSetting
        } else {
            print("[RNCallKeep][getProviderConfiguration] Missing key: appName")
            appName = "Eyr"
        }

        let config = CXProviderConfiguration(localizedName: appName)
        if let ringtoneSound = settings["ringtoneSound"] as? String {
            config.ringtoneSound = ringtoneSound
        }

        if let imgName = settings["imageName"] as? String,
           let img = UIImage(named: imgName),
           let data = img.pngData() {
            config.iconTemplateImageData = data
        }

        config.supportsVideo = true
        config.maximumCallGroups = 1
        config.maximumCallsPerCallGroup = 1
        config.supportedHandleTypes = [.generic]

        return config
    }

    public override class func requiresMainQueueSetup() -> Bool {
        return true
    }

    // MARK: - Private func
    fileprivate func requestTransaction(_ transaction: CXTransaction) {
        self.cxCallController.request(transaction, completion: { err in
            if let err = err {
                print("[RNCallKeep][requestTransaction] Error request transaction \(transaction.actions): \(err)")
                return
            }
            print("[RNCallKeep][requestTransaction] Requested transaction successfully")
        })
    }

    fileprivate func configureAudioSession() {
        // See https://forums.developer.apple.com/thread/64544
        let audioSession = AVAudioSession.sharedInstance()
        do {
            try audioSession.setCategory(.playAndRecord, options: [.allowBluetooth, .allowBluetoothA2DP])
            try audioSession.setMode(.default)
            try audioSession.setPreferredSampleRate(44100.0)
            try audioSession.setPreferredIOBufferDuration(0.005)
            try audioSession.setActive(true)
        } catch {
            print("Error configuring audio session: \(error)")
        }
    }

    // MARK: - Override methods
    public override func startObserving() {
        _hasListener = true
        if _delayedEvents.count > 0 {
            self.sendEvent(withName: RNCallKeepDidLoadWithEvents, body: _delayedEvents)
        }
    }

    public override func stopObserving() {
        _hasListener = false
    }

    public override func supportedEvents() -> [String]! {
        return [
            RNCallKeepDidLoadWithEvents,
            RNCallKeepAnswerCallAction,
            RNCallKeepPerformEndCallAction,
            RNCallKeepDidPerformSetMutedCallAction
        ]
    }

    func sendEventWithNameWrapper(_ name: String, body: Any) {
        if _hasListener {
            self.sendEvent(withName: name, body: body)
        } else {
            let dict: [String: Any] = ["name": name, "body": body]
            _delayedEvents.append(dict)
        }
    }

    // MARK: - Exported methods
    /// Activating a mute call action
    /// - Parameter uuidString: Device's uuid
    /// - Parameter muted: Mute or unmute the recipient
    @objc(setMutedCall:muted:)
    func setMutedCall(_ uuidString: String, muted: Bool) {
        print("[RNCallKeep][setMutedCall] \(uuidString)")
        guard let uuid = UUID(uuidString: uuidString) else {
            print("[RNCallKeep][setMutedCall] Cant find uuid")
            return
        }

        let mutedAction = CXSetMutedCallAction(call: uuid, muted: muted)
        let transaction = CXTransaction()
        transaction.addAction(mutedAction)

        // Request transaction
        self.requestTransaction(transaction)
    }

    @objc(endCall:)
    func endCall(_ uuidString: String) {
        print("[RNCallKeep][endCall] \(uuidString)")
        guard let uuid = UUID(uuidString: uuidString) else {
            print("[RNCallKeep][endCall] Cant find uuid")
            return
        }
        // Check if call is ended remotely
        // If true, no need to execute transaction
        if _isRemoteEnded {
            _isRemoteEnded = false
            return
        }

        let endCallAction = CXEndCallAction(call: uuid)
        let transaction = CXTransaction()
        transaction.addAction(endCallAction)

        // Request transaction
        self.requestTransaction(transaction)
    }

    @objc(answerIncomingCall:)
    func answerIncomingCall(_ uuidString: String) {
        print("[RNCallKeep][answerIncomingCall] \(uuidString)")
        guard let uuid = UUID(uuidString: uuidString) else {
            print("[RNCallKeep][answerIncomingCall] Cant find uuid")
            return
        }
        let answerCallAction = CXAnswerCallAction(call: uuid)
        let transaction = CXTransaction(action: answerCallAction)
        self.requestTransaction(transaction)
    }

    @objc public func fulfillAnswerCallAction() {
        if let action = _answerCallAction {
            action.fulfill()
            _answerCallAction = nil
        }
    }

    @objc public func fulfillEndCallAction() {
        if let action = _endCallAction {
            action.fulfill()
            _endCallAction = nil
        }
    }

    @objc(getInitialEvents:reject:)
    public func getInitialEvents(_ resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        resolve(_delayedEvents)
    }

    @objc(reportEndCallWithUUID:reason:)
    public func reportEndCallWithUUID(_ uuidString: String, reason: Int) {

        guard let uuid = UUID(uuidString: uuidString) else {
            print("[RNCallKeep][endCall] Cant find uuid")
            return
        }

        switch reason {
        case 1:
            self.callKeepProvider.reportCall(with: uuid, endedAt: Date(), reason: .failed)
            break
        case 2, 6:
            self.callKeepProvider.reportCall(with: uuid, endedAt: Date(), reason: .remoteEnded)
            _isRemoteEnded = true
            break
        case 3:
            self.callKeepProvider.reportCall(with: uuid, endedAt: Date(), reason: .unanswered)
            break
        case 4:
            self.callKeepProvider.reportCall(with: uuid, endedAt: Date(), reason: .answeredElsewhere)
            break
        case 5:
            self.callKeepProvider.reportCall(with: uuid, endedAt: Date(), reason: .declinedElsewhere)
            break
        default:
            break
        }
    }

    @objc public func reportIncomingCall(notification: Notification) {
        let userInfo = notification.userInfo!
        let object = notification.object
        let uuidString = userInfo["uuidString"] as! String

        guard let uuid = UUID(uuidString: uuidString) else {
            print("[RNCallKeep][reportIncomingCall] Error: Cant create uuid from string")
            return
        }

        let hasVideo = userInfo["hasVideo"] as? Bool ?? true
        let handle = userInfo["handle"] as? String ?? ""
        let name = userInfo["localizedCallerName"] as? String ?? ""

        let cxCallUpdate = CXCallUpdate()
        cxCallUpdate.remoteHandle = CXHandle(type: .generic, value: handle)
        cxCallUpdate.localizedCallerName = name;
        cxCallUpdate.supportsHolding = false
        cxCallUpdate.supportsGrouping = false
        cxCallUpdate.supportsUngrouping = false
        cxCallUpdate.supportsDTMF = false
        cxCallUpdate.hasVideo = hasVideo

        self.callKeepProvider.reportNewIncomingCall(with: uuid,
                                           update: cxCallUpdate,
                                           completion: {err in
            if let err = err {
                print("[RNCallKeep][reportIncomingCall] Error report incoming call: \(err.localizedDescription) ")
            } else {

                // Call completion block
                if let obj = object as AnyObject? {
                    let block : () -> Void = unsafeBitCast(obj, to: ClosureType.self)
                    block()
                }
                print("[RNCallKeep][reportIncomingCall] \(uuidString)")
            }
        })
    }
}

// MARK: - CXProvider delegate
extension RNCallKeep: CXProviderDelegate {

    public func providerDidReset(_ provider: CXProvider) {}

    /// Answer incoming call
    public func provider(_ provider: CXProvider, perform action: CXAnswerCallAction) {
        print("[RNCallKeep][performAnswerCallAction]")
        self.configureAudioSession()
        self.sendEventWithNameWrapper(RNCallKeepAnswerCallAction,
                                      body: ["callUUID": action.callUUID.uuidString.lowercased()])
        _answerCallAction = action
    }

    /// End ongoing call
    public func provider(_ provider: CXProvider, perform action: CXEndCallAction) {
        print("[RNCallKeep][performEndCallAction]")
        self.sendEventWithNameWrapper(RNCallKeepPerformEndCallAction,
                                      body: ["callUUID": action.callUUID.uuidString.lowercased()])
        _endCallAction = action
    }

    /// Muted ongoing call
    public func provider(_ provider: CXProvider, perform action: CXSetMutedCallAction) {
        print("[RNCallKeep][performSetMutedCallAction] \(action.isMuted): \(action.uuid.uuidString.lowercased())")
        // Send event with name wrapper
        self.sendEventWithNameWrapper(RNCallKeepDidPerformSetMutedCallAction,
                                      body: ["muted" : action.isMuted,
                                             "callUUID": action.callUUID.uuidString.lowercased()])
        action.fulfill()
    }

    public func provider(_ provider: CXProvider, didActivate audioSession: AVAudioSession) {
        print("[RNCallKeep][didActivateAudioSession]")
        let userInfo: [AnyHashable: Any] = [
            AVAudioSessionInterruptionTypeKey: AVAudioSession.InterruptionType.ended.rawValue,
            AVAudioSessionInterruptionOptionKey: AVAudioSession.InterruptionOptions.shouldResume.rawValue
        ]
        NotificationCenter.default.post(name: AVAudioSession.interruptionNotification, object: nil, userInfo: userInfo)
        configureAudioSession()
    }
}

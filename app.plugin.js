const {
    withStringsXml,
    withAndroidColors,
    withPlugins,
    withMainActivity,
    AndroidConfig,
    WarningAggregator,
} = require("@expo/config-plugins");
const { addImports } = require("@expo/config-plugins/build/android/codeMod");
const { setColorItem } = AndroidConfig.Colors;
const { setStringItem } = AndroidConfig.Strings;
const { buildResourceItem } = AndroidConfig.Resources;
const { withPermissions } = AndroidConfig.Permissions;

const strings = [
    ["call_notification_accept_button", "Accept"],
    ["call_notification_decline_button", "Decline"],
    ["call_notification_incoming_call", "Incoming video call from Eyr"],
    ["call_notification_ongoing_call", "Ongoing video call from Eyr"],
    ["call_notification_missed_call", "Missed video call from Eyr"],
    ["call_group", "Calls"],
];

const colors = [
    ["primary", "#750F37"],
    ["secondary", "#FAB99C"],
    ["danger", "#FE3B34"],
    ["success", "#25863A"],
];
function withStrings(config, stringsToConcat) {
    return withStringsXml(config, (config) => {
        config.modResults = setStringItem(
            stringsToConcat.map(([name, value, translatable = true]) =>
                buildResourceItem({
                    name,
                    value,
                    translatable,
                })
            ),
            config.modResults
        );
        return config;
    });
}

function withColors(config, colorsToConcat) {
    return withAndroidColors(config, (config) => {
        colorsToConcat.forEach(([name, value]) => {
            config.modResults = setColorItem(
                buildResourceItem({
                    name,
                    value,
                }),
                config.modResults
            );
        });
        return config;
    });
}

function withCallkeepActivity(config) {
    return withMainActivity(config, (config) => {
        const onCreate = `
    Log.d("MainActivity", "onCreate")
    val initialIntent = intent
    val context = applicationContext
    if (initialIntent.action == CallKeepService.ACTION_ACCEPT_CALL) {
      Utils.showOnLockscreen(this@MainActivity, true)
      CallKeepService.reportMainActivityReady(context, initialIntent)
      return
    }
    if (XiaomiUtilities.isMIUI()
      &&
      !XiaomiUtilities.isCustomPermissionGranted(
        applicationContext,
        XiaomiUtilities.OP_SHOW_WHEN_LOCKED
      )
    ) {
      try {
        val xiaomiModifyPermissionsIntent = XiaomiUtilities.getPermissionManagerIntent(
          applicationContext
        )
        startActivity(xiaomiModifyPermissionsIntent)
      } catch (e: Exception) {
        try {
          val xiaomiModifyPermissionsIntent =
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
          xiaomiModifyPermissionsIntent.setData(Uri.parse("package:$packageName"))
          startActivity(xiaomiModifyPermissionsIntent)
        } catch (e1: Exception) {
          e1.printStackTrace()
        }
      }
    }`;

        const onNewIntent = `
  override fun onNewIntent(intent: Intent) {
    Log.d("MainActivity:onNewIntent", intent.action!!)
    val context = applicationContext
    if (intent.action == CallKeepService.ACTION_ACCEPT_CALL) {
      Utils.showOnLockscreen(this@MainActivity, true)
      CallKeepService.reportMainActivityReady(context, intent)
    }
    if (intent.action == CallKeepService.ACTION_END_CALL) {
      Utils.showOnLockscreen(this@MainActivity, false)
      CallKeepService.reportMainActivityReady(context, intent)
    }
    super.onNewIntent(intent)
  }
`;
        if (config.modResults.language === "kt") {
            let content = config.modResults.contents;
            content = addImports(content, [
                "android.content.Intent",
                "com.eyr.callkeep.XiaomiUtilities",
                "com.eyr.callkeep.CallKeepService",
                "com.eyr.callkeep.Utils",
            ]);

            if (!content.includes("if (XiaomiUtilities.isMIUI()")) {
                content = content.replace(
                    /super\.onCreate\(null\)/,
                    "super.onCreate(null)" + onCreate
                );
            }
            if (!content.includes("override fun onNewIntent(intent: Intent) {")) {
                content = content.replace(/}\n$/, onNewIntent + "}\n");
            }
            config.modResults.contents = content;
        } else {
            WarningAggregator.addWarningAndroid(
                "react-native-callkeep",
                "Cannot automatically configure MainActivity if it's not java."
            );
        }
        return config;
    });
}

function withCallkeep(config) {
    return withPlugins(config, [
        [withStrings, strings],
        [withColors, colors],
        [
            withPermissions,
            [
                "USE_FULL_SCREEN_INTENT",
                "WAKE_LOCK",
                "DISABLE_KEYGUARD",
                "BROADCAST_STICKY",
                "BLUETOOTH",
            ],
        ],
        withCallkeepActivity,
    ]);
}

module.exports = withCallkeep;

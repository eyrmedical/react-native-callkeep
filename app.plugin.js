const {
    withStringsXml,
    withAndroidColors,
    withPlugins,
    withMainActivity,
    AndroidConfig,
    WarningAggregator,
} = require("expo/config-plugins");
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

function addJavaImports(javaSource, javaImports) {
    const lines = javaSource.split("\n");
    const lineIndexWithPackageDeclaration = lines.findIndex((line) =>
        line.match(/^package .*;$/)
    );
    for (const javaImport of javaImports) {
        if (!javaSource.includes(javaImport)) {
            const importStatement = `import ${javaImport};`;
            lines.splice(lineIndexWithPackageDeclaration + 2, 0, importStatement);
        }
    }
    return lines.join("\n");
}

function addLines(content, find, offset, toAdd) {
    const lines = content.split("\n");
    let lineIndex = lines.findIndex((line) => line.match(find));
    for (const newLine of toAdd) {
        if (!content.includes(newLine)) {
            lines.splice(lineIndex + offset, 0, newLine);
            lineIndex++;
        }
    }
    return lines.join("\n");
}

function withCallkeepActivity(config) {
    return withMainActivity(config, (config) => {
        const onCreate =
            "    if (XiaomiUtilities.isMIUI()\n" +
            "      &&\n" +
            "      !XiaomiUtilities.isCustomPermissionGranted(\n" +
            "        getApplicationContext(),\n" +
            "        XiaomiUtilities.OP_SHOW_WHEN_LOCKED)) {\n" +
            "      try {\n" +
            "        Intent intent = XiaomiUtilities.getPermissionManagerIntent(getApplicationContext());\n" +
            "        startActivity(intent);\n" +
            "      } catch (Exception e) {\n" +
            "        try {\n" +
            "          Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);\n" +
            '          intent.setData(Uri.parse("package:" + getPackageName()));\n' +
            "          startActivity(intent);\n" +
            "        } catch (Exception e1) {\n" +
            "          e1.printStackTrace();\n" +
            "        }\n" +
            "      }\n" +
            "    }";
        const onNewIntent =
            "  @Override\n" +
            "  public void onNewIntent(Intent intent) {\n" +
            "    try {\n" +
            "      if (intent.getExtras() != null) {\n" +
            "        showOnLockscreen(\n" +
            "          MainActivity.this,\n" +
            "          intent.getExtras().get(INITIAL_CALL_STATE_PROP_NAME) != null);\n" +
            "      }\n" +
            "    } catch (Exception e) {\n" +
            "      e.printStackTrace();\n" +
            "      showOnLockscreen(MainActivity.this, false);\n" +
            "    }\n" +
            "    super.onNewIntent(intent);\n" +
            "  }\n";
        if (config.modResults.language === "java") {
            let content = config.modResults.contents;
            content = addJavaImports(content, [
                "static com.eyr.callkeep.Utils.INITIAL_CALL_STATE_PROP_NAME",
                "static com.eyr.callkeep.Utils.showOnLockscreen",
                "com.eyr.callkeep.XiaomiUtilities",
                "android.content.Intent",
            ]);

            if (!content.includes("if (XiaomiUtilities.isMIUI()")) {
                content = content.replace(
                    /super\.onCreate\(null\);/,
                    "super.onCreate(null);\n" + onCreate
                );
            }
            if (!content.includes("public void onNewIntent(Intent intent) {")) {
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

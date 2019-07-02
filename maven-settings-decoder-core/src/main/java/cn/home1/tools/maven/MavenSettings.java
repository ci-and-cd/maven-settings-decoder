package cn.home1.tools.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class MavenSettings {

    private final boolean debug;
    private final Settings settings;
    private final File settingsFile;
    private final MavenSettingsSecurity settingsSecurity;

    public MavenSettings(
        final boolean debug,
        final String settingsFilePathname,
        final MavenSettingsSecurity settingsSecurity
    ) throws IOException, XmlPullParserException {
        this.debug = debug;
        this.settingsFile = new File(settingsFilePathname);
        this.settingsSecurity = settingsSecurity;

        if (!this.settingsFile.exists()) {
            throw new IllegalArgumentException( //
                String.format("Settings file : %s does not exist%n", this.settingsFile.getAbsolutePath()));
        }

        this.settings = readSettings(this.settingsFile);
    }

    public String getText(final String xpathExpression) {
        //final List<Server> servers = this.settings.getServers();

        final String nodeText = XmlUtils.xmlNodeText(this.settingsFile, xpathExpression);
        if (this.debug) {
            System.err.printf("nodeText:%s%n", nodeText);
        }

        return this.nodeTextValue(nodeText);
    }

    public String nodeTextValue(final String nodeText) {
        final Boolean envVar = nodeText != null && nodeText.startsWith("${env.") && nodeText.endsWith("}");

        if (this.debug) {
            System.err.printf("envVar: %s%n", envVar);
        }

        final String result;
        if (envVar) {
            final String envVarName = nodeText.substring(6, nodeText.length() - 1);
            if (this.debug) {
                System.err.printf("envVarName: %s%n", envVarName);
                final Map<String, String> env = System.getenv();
                for (Map.Entry<String, String> entry : env.entrySet()) {
                    System.err.printf("env name: %s, value: *secret* %n", entry.getKey());
                }
            }
            result = System.getenv(envVarName);
        } else {
            result = this.settingsSecurity.decodeText(nodeText);
        }

        return result;
    }

    static Settings readSettings(final File file) throws IOException, XmlPullParserException {
        return new SettingsXpp3Reader().read(new FileInputStream(file));
    }
}

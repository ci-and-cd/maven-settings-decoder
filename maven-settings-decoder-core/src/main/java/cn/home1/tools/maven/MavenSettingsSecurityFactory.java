package cn.home1.tools.maven;

import java.io.File;

public abstract class MavenSettingsSecurityFactory {

    private MavenSettingsSecurityFactory() {
    }

    public static MavenSettingsSecurity newMavenSettingsSecurity(
        final String securityFilePathname,
        final boolean debug
    ) {
        return new MavenSettingsSecurity(debug, MavenSettingsSecurityFactory.encodedMasterPassword(securityFilePathname));
    }

    public static String encodedMasterPassword(final String securityFilePathname) {
        final String result;

        final File securityFile = new File(securityFilePathname);
        if (!securityFile.exists()) {
            //throw new IllegalArgumentException( //
            //    String.format("Security file : %s does not exist%n", securityFile.getAbsolutePath()));
            result = null;
        } else {
            result = encodedMasterPassword(securityFile);
        }

        return result;
    }

    static String encodedMasterPassword(final File file) {
        return XmlUtils.xmlNodeText(file, "/settingsSecurity/master/text()");
    }
}

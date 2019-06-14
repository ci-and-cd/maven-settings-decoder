package cn.home1.tools.maven;

import java.io.File;

import org.sonatype.plexus.components.cipher.PlexusCipherException;

public class MavenSettingsSecurity {

    private final boolean debug;

    private final String plainTextMasterPassword;

    public MavenSettingsSecurity(final boolean debug, final String encodedMasterPassword) throws PlexusCipherException {
        this.debug = debug;
        this.plainTextMasterPassword = PlexusCipherUtils.decodeMasterPassword(encodedMasterPassword, debug);
    }

    public MavenSettingsSecurity(
        final String securityFilePathname,
        final boolean debug
    ) throws Exception {
        this(debug, MavenSettingsSecurity.encodedMasterPassword(securityFilePathname));
    }

    public String decodeText(final String text) throws PlexusCipherException {
        return PlexusCipherUtils.decodeText(text, this.plainTextMasterPassword, this.debug);
    }

    public String encodeText(final String text) throws PlexusCipherException {
        return PlexusCipherUtils.encodeText(text, this.plainTextMasterPassword, this.debug);
    }

    public String getPlainTextMasterPassword() {
        return this.plainTextMasterPassword;
    }

    public static String encodedMasterPassword(final String securityFilePathname) throws Exception {
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

    static String encodedMasterPassword(final File file) throws Exception {
        return XmlUtils.xmlNodeText(file, "/settingsSecurity/master/text()");
    }
}

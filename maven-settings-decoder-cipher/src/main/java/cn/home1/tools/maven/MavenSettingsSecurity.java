package cn.home1.tools.maven;

import java.io.File;

import org.sonatype.plexus.components.cipher.PlexusCipherException;

public class MavenSettingsSecurity {

    private final boolean debug;

    private final String plainTextMasterPassword;

    public MavenSettingsSecurity(final boolean debug, final String encodedMasterPassword) {
        this.debug = debug;
        try {
            this.plainTextMasterPassword = PlexusCipherUtils.decodeMasterPassword(encodedMasterPassword, debug);
        } catch (final PlexusCipherException ex) {
            throw new IllegalArgumentException(encodedMasterPassword, ex);
        }
    }

    public MavenSettingsSecurity(
        final String securityFilePathname,
        final boolean debug
    ) {
        this(debug, MavenSettingsSecurity.encodedMasterPassword(securityFilePathname));
    }

    public String decodeText(final String text) {
        try {
            return PlexusCipherUtils.decodeText(text, this.plainTextMasterPassword, this.debug);
        } catch (final PlexusCipherException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public String encodeText(final String text) {
        try {
            return PlexusCipherUtils.encodeText(text, this.plainTextMasterPassword, this.debug);
        } catch (final PlexusCipherException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public String getPlainTextMasterPassword() {
        return this.plainTextMasterPassword;
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

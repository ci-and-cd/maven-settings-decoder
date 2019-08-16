package cn.home1.tools.maven;

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
}

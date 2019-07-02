package cn.home1.tools.maven;

import org.sonatype.plexus.components.cipher.DefaultPlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;

public final class PlexusCipherUtils {

    private PlexusCipherUtils() {
    }

    public static String decodeMasterPassword(
        final String encodedMasterPassword,
        final boolean debug
    ) throws PlexusCipherException {
        // key = org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION;
        final String key = "settings.security";
        return decodeText(encodedMasterPassword, key, debug);
    }

    public static String encodeText(
        final String str,
        final String passPhrase
    ) throws PlexusCipherException {
        return encodeText(str, passPhrase, false);
    }

    public static String encodeText(
        final String str,
        final String passPhrase,
        final boolean debug
    ) throws PlexusCipherException {
        final String result;

        if (passPhrase != null) {
            if (debug) {
                System.err.printf("key not null%n");
            }

            if (debug) {
                System.err.printf("str: %s%n", str);
            }

            result = new DefaultPlexusCipher().encryptAndDecorate(str, passPhrase);
        } else {
            if (debug) {
                System.err.printf("key is null%n");
            }
            result = str;
        }

        return result;
    }

    public static String decodeText(
        final String str,
        final String passPhrase
    ) throws PlexusCipherException {
        return decodeText(str, passPhrase, false);
    }

    public static String decodeText(
        final String str,
        final String passPhrase,
        final boolean debug
    ) throws PlexusCipherException {
        final String result;

        if (passPhrase != null) {
            if (debug) {
                System.err.printf("key not null%n");
            }

            final Boolean encoded = str != null && str.startsWith("{") && str.endsWith("}");
            if (debug) {
                System.err.printf("encoded: %s%n", encoded);
            }

            if (encoded) {
                result = new DefaultPlexusCipher().decryptDecorated(str, passPhrase);
            } else {
                result = str;
            }
        } else {
            if (debug) {
                System.err.printf("key is null%n");
            }

            result = str;
        }

        return result;
    }
}

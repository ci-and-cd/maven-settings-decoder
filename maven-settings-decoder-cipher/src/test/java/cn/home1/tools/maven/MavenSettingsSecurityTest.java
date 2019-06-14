package cn.home1.tools.maven;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.components.cipher.PlexusCipherException;

public class MavenSettingsSecurityTest {

    private static final Logger logger = LoggerFactory.getLogger(MavenSettingsSecurityTest.class);

    private boolean debug;
    private String encodedMasterPass;

    @Before
    public void setUp() {
        this.debug = true;
        this.encodedMasterPass = "{x5THqWf1AcsHe5iI1++JM1O9Y0FEUhY91ll5RDEJ9rg=}";
    }

    @Test
    public void testEncodeAndDecode() throws PlexusCipherException {
        this.check(this.encodedMasterPass, "str");
    }

    @Test(expected = PlexusCipherException.class)
    public void testBlankStr() throws PlexusCipherException {
        // nested exception: java.lang.ArrayIndexOutOfBoundsException
        this.check(this.encodedMasterPass, "");
    }

    private void check(final String encodedMasterPassword, final String str) throws PlexusCipherException {
        final MavenSettingsSecurity settingsSecurity = new MavenSettingsSecurity(this.debug, encodedMasterPassword);
        logger.info("encodedMasterPass: [{}]", encodedMasterPassword);
        final String encoded = settingsSecurity.encodeText(str);
        logger.info("str [{}], encoded: [{}]", str, encoded);
        final String decoded = settingsSecurity.decodeText(encoded);
        logger.info("str [{}], decoded: [{}]", str, decoded);

        assertEquals(str, decoded);
    }
}

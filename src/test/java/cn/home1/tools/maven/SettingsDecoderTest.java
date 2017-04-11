package cn.home1.tools.maven;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;

public class SettingsDecoderTest {

  @Test
  public void testReadSettings() throws IOException, XmlPullParserException {
    final File targetFile = dumpClasspathResourceIntoTmpFile("/settings.xml");
    SettingsDecoder.readSettings(targetFile);
  }

  @Test
  public void testMasterPassword() throws Exception {
    final File targetFile = dumpClasspathResourceIntoTmpFile("/settings-security.xml");
    final String encodedMasterPassword = SettingsDecoder.encodedMasterPassword(targetFile);
    System.out.println(encodedMasterPassword);
    System.out.println(SettingsDecoder.decodeMasterPassword(encodedMasterPassword));
  }

  @Test
  public void testEnvVar() throws Exception {
    final File settingsFile = dumpClasspathResourceIntoTmpFile("/settings.xml");
    final File settingsSecurityFile = dumpClasspathResourceIntoTmpFile("/settings-security.xml");
    final String plainText = new SettingsDecoder( //
        settingsFile.getCanonicalPath(), //
        settingsSecurityFile.getCanonicalPath(), //
        true //
    ).getText("//server[id='github']/password/text()");
    System.out.println(plainText);
    assertNotNull(plainText);
    assertFalse(plainText.equals(""));
    assertFalse(plainText.startsWith("${env.") && plainText.endsWith("}"));
  }

  private static File dumpClasspathResourceIntoTmpFile(final String classpathResource) throws IOException {
    final File tmpDir = new File(System.getProperty("java.io.tmpdir", "/tmp"));
    final File targetFile = File.createTempFile("test", ".xml", tmpDir);

    try (final InputStream initialStream = SettingsDecoderTest.class.getResourceAsStream(classpathResource)) {
      targetFile.deleteOnExit();
      java.nio.file.Files.copy(initialStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
    return targetFile;
  }
}

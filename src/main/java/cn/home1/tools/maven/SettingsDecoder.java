package cn.home1.tools.maven;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.plexus.components.cipher.DefaultPlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

public class SettingsDecoder {

  private static final String SETTINGS_SECURITY_FILE_SHORT_OPT = "ss";
  private static final String SETTINGS_SECURITY_FILE_LONG_OPT = "settings-security";
  private static final String SETTINGS_FILE_LONG_OPT = "settings";
  private static final String SETTINGS_FILE_SHORT_OPT = "s";
  private static final String XPATH_SHORT_OPT = "x";
  private static final String XPATH_LONG_OPT = "xml-path";
  private static final String DEBUG_SHORT_OPT = "X";
  private static final String DEBUG_LONG_OPT = "debug";

  private static final int MISSING_OR_INVALID_ARGUMENTS_EXIT_CODE = 1;

  private final boolean debug;
  private final String plainTextMasterPassword;
  private final Settings settings;
  private final File settingsFile;

  public SettingsDecoder(
      final String settingsFileOpt, //
      final String securityFileOpt, //
      final boolean debug //
  ) throws Exception {
    this.debug = debug;

    final String envMavenSettings = System.getenv("MAVEN_SETTINGS");
    final String userHome = System.getProperty("user.home");

    final String settingsFilePath;
    if (settingsFileOpt == null) {
      if (envMavenSettings != null && envMavenSettings.contains("-s ")) {
        final Matcher matcher = Pattern.compile("-s[ ]+([^\\s]+)").matcher(envMavenSettings);
        matcher.find();
        settingsFilePath = matcher.group(1).replace("${HOME}", userHome);
      } else {
        settingsFilePath = userHome + "/.m2/settings.xml";
      }
    } else {
      settingsFilePath = settingsFileOpt;
    }

    this.settingsFile = new File(settingsFilePath);
    if (!this.settingsFile.exists()) {
      throw new IllegalArgumentException( //
          String.format("Settings file : %s does not exist%n", this.settingsFile.getAbsolutePath()));
    }

    final String securityFilePath;
    if (securityFileOpt == null) {
      if (envMavenSettings != null && envMavenSettings.contains("-ss ")) {
        final Matcher matcher = Pattern.compile("-ss[ ]+([^\\s]+)").matcher(envMavenSettings);
        matcher.find();
        securityFilePath = matcher.group(1).replace("${HOME}", userHome);
      } else {
        securityFilePath = userHome + "/.m2/settings-security.xml";
      }
    } else {
      securityFilePath = securityFileOpt;
    }
    this.settings = readSettings(this.settingsFile);

    final File securityFile = new File(securityFilePath);
    if (!securityFile.exists()) {
      //throw new IllegalArgumentException( //
      //    String.format("Security file : %s does not exist%n", securityFile.getAbsolutePath()));
      this.plainTextMasterPassword = null;
    } else {
      final String encodedMasterPassword = encodedMasterPassword(securityFile);
      this.plainTextMasterPassword = decodeMasterPassword(encodedMasterPassword);
    }
    if (this.debug) {
      System.err.printf("masterPassword:%s%n", this.plainTextMasterPassword);
    }
  }

  public SettingsDecoder() throws Exception {
    this(null, null, false);
  }

  public String getText(final String xpathExpression) throws Exception {
    //final List<Server> servers = this.settings.getServers();

    final String nodeText = xmlNodeText(this.settingsFile, xpathExpression);
    if (this.debug) {
      System.err.printf("nodeText:%s%n", nodeText);
    }

    final String plainText = decodeText(nodeText, this.plainTextMasterPassword);

    return plainText;
  }

  public static void main(final String... args) throws Exception {
    final Options options = createOptions();

    final CommandLine commandLine = new DefaultParser().parse(options, args);
    final String settingsFileOpt = commandLine.getOptionValue(SETTINGS_FILE_SHORT_OPT);
    final String securityFileOpt = commandLine.getOptionValue(SETTINGS_SECURITY_FILE_SHORT_OPT);
    final String xpathOpt = commandLine.getOptionValue(XPATH_SHORT_OPT);
    final boolean debug = commandLine.getOptionValue(DEBUG_SHORT_OPT) != null;

    if (debug) {
      System.err.printf("xpathOpt:%s%n", xpathOpt);
    }

    try {
      final String plainText = new SettingsDecoder(settingsFileOpt, securityFileOpt, debug).getText(xpathOpt);
      System.out.printf("%s", plainText);
    } catch (final IllegalArgumentException ignored) {
      printHelp(options);
      System.exit(MISSING_OR_INVALID_ARGUMENTS_EXIT_CODE);
    }
  }

  private static Options createOptions() {
    Options options = new Options();
    options.addOption( //
        SETTINGS_SECURITY_FILE_SHORT_OPT, SETTINGS_SECURITY_FILE_LONG_OPT, true, "location of settings-security.xml.");
    options.addOption(SETTINGS_FILE_SHORT_OPT, SETTINGS_FILE_LONG_OPT, true, "location of settings.xml file.");
    options.addOption(XPATH_SHORT_OPT, XPATH_LONG_OPT, true, "xml-path in settings.xml, only node text is supported.");
    options.addOption(DEBUG_SHORT_OPT, DEBUG_LONG_OPT, true, "produce execution debug output.");
    return options;
  }

  private static void printHelp(final Options options) {
    new HelpFormatter().printHelp("maven-settings-decoder", options);
  }

  static Settings readSettings(final File file) throws IOException, XmlPullParserException {
    return new SettingsXpp3Reader().read(new FileInputStream(file));
  }

  private static String decodeText(final String encodedText, final String key) throws PlexusCipherException {
    final String result;
    if (key != null) {
      final Boolean encoded = encodedText != null && encodedText.startsWith("{") && encodedText.endsWith("}");
      final Boolean envVar = encodedText != null && encodedText.startsWith("${env.") && encodedText.endsWith("}");
      if (encoded) {
        result = new DefaultPlexusCipher().decryptDecorated(encodedText, key);
      } else if (envVar) {
        final String envVarName = encodedText.substring(6, encodedText.length() - 1);
        result = System.getenv(envVarName);
      } else {
        result = encodedText;
      }
    } else {
      result = encodedText;
    }
    return result;
  }

  static String decodeMasterPassword(final String encodedMasterPassword) throws PlexusCipherException {
    // key = org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION;
    final String key = "settings.security";
    return decodeText(encodedMasterPassword, key);
  }

  private static String xmlNodeText(final File file, final String xpathExpression) throws Exception {
    // see: http://www.ibm.com/developerworks/cn/xml/x-javaxpathapi.html
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(false); // Note: never forget this !
    final DocumentBuilder builder = factory.newDocumentBuilder();
    final Document doc = builder.parse(file);

    final Object result = XPathFactory.newInstance().newXPath() //
        .compile(xpathExpression) //
        .evaluate(doc, XPathConstants.STRING);
    return result != null ? result.toString() : null;
  }

  static String encodedMasterPassword(final File file) throws Exception {
    return xmlNodeText(file, "/settingsSecurity/master/text()");
  }

  //  private static String encodedMasterPassword( //
  //      final File file //
  //  ) throws org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException { //
  //    final org.sonatype.plexus.components.sec.dispatcher.model.SettingsSecurity settingsSecurity = //
  //        org.sonatype.plexus.components.sec.dispatcher.SecUtil.read(file.getAbsolutePath(), true);
  //    return settingsSecurity.getMaster();
  //  }
}

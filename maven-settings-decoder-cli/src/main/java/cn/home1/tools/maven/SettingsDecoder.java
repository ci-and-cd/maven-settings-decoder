package cn.home1.tools.maven;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

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

    private MavenSettings settings;

    public SettingsDecoder(
        final String settingsFileOpt,
        final String securityFileOpt,
        final boolean debug //
    ) throws Exception {
        final MavenSettingsSecurity settingsSecurity = new MavenSettingsSecurity(securityFilePathname(securityFileOpt), debug);
        if (debug) {
            System.err.printf("masterPassword:%s%n", settingsSecurity.getPlainTextMasterPassword());
        }

        this.settings = new MavenSettings(debug, settingsFilePathname(settingsFileOpt), settingsSecurity);
    }

    public SettingsDecoder() throws Exception {
        this(null, null, false);
    }

    public String getText(final String xpathOpt) {
        return this.settings.getText(xpathOpt);
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

    private static String pathname(
        final String optionName,
        final String optionValue,
        final String filename
    ) {
        final String pathname;
        if (optionValue == null) {
            final String envMavenSettings = systemEnvMavenSettings();
            if (envMavenSettings != null && envMavenSettings.contains("-" + optionName + " ")) {
                final Matcher matcher = Pattern.compile("-" + optionName + "[ ]+([^\\s]+)").matcher(envMavenSettings);
                matcher.find();
                pathname = matcher.group(1).replace("${HOME}", systemUserHome());
            } else {
                pathname = systemUserHome() + "/.m2/" + filename;
            }
        } else {
            pathname = optionValue;
        }
        return pathname;
    }

    private static void printHelp(final Options options) {
        new HelpFormatter().printHelp("maven-settings-decoder", options);
    }

    private static String settingsFilePathname(final String settingsFileOpt) {
        return pathname("s", settingsFileOpt, "settings.xml");
    }

    private static String securityFilePathname(final String securityFileOpt) {
        return pathname("ss", securityFileOpt, "settings-security.xml");
    }

    private static String systemEnvMavenSettings() {
        return System.getenv("MAVEN_SETTINGS");
    }

    private static String systemUserHome() {
        return System.getProperty("user.home");
    }

    //  private static String encodedMasterPassword( //
    //      final File file //
    //  ) throws org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException { //
    //    final org.sonatype.plexus.components.sec.dispatcher.model.SettingsSecurity settingsSecurity = //
    //        org.sonatype.plexus.components.sec.dispatcher.SecUtil.read(file.getAbsolutePath(), true);
    //    return settingsSecurity.getMaster();
    //  }
}

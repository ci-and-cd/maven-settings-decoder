# maven-settings-decoder
===========================================================================

A tool to decrypt the XML node text stored in maven settings.xml files

Inspired by [jelmerk/maven-settings-decoder](https://github.com/jelmerk/maven-settings-decoder)

Maven 2.1.0+  supports [server password encryption](http://maven.apache.org/guides/mini/guide-encryption.html)

This tool lets you decrypt these encrypted text as long as you have access to both the settings.xml file and the 
settings-security.xml file.

    usage: maven-settings-decoder
     -s,--settings <arg>             location of settings.xml file.
     -ss,--settings-security <arg>   location of settings-security.xml.
     -x,--xml-path <arg>             xpath expression, only node text is supported.

    or set '-s' and '-ss' in MAVEN_SETTINGS environment variable.

Use as a command-line tool:

    java -jar target/maven-settings-decoder-*-exec.jar \
        -s "${HOME}/.m2/settings.xml" \
        -ss "${HOME}/.m2/settings-security.xml" \
        -x "/settings/servers/server[id='local-nexus3-releases']/password/text()"
    
    # or
    
    java -jar target/maven-settings-decoder-*-exec.jar \
        -s "${HOME}/.m2/settings.xml" \
        -ss "${HOME}/.m2/settings-security.xml" \
        -x "//server[id='local-nexus3-releases']/password/text()"

Use as gradle buildscript dependency, so we can access maven's settings.xml from our build script:

        buildscript {
          // cn.home1.tools:maven-settings-decoder is in maven central.
          mavenCentral()
          dependencies {
            ...
            classpath 'cn.home1.tools:maven-settings-decoder:1.0.6.OSS-SNAPSHOT'
          }
        }
        ...
        ext.mavenSettings = new cn.home1.tools.maven.SettingsDecoder();
        ext.nexusSnapshotsUser = mavenSettings.getText("//server[id='${nexus}-snapshots']/username/text()")
        ext.nexusSnapshotsPass = mavenSettings.getText("//server[id='${nexus}-snapshots']/password/text()")
        println "${nexus}-snapshots username: " + mavenSettings.getText("//server[id='${nexus}-snapshots']/username/text()")
        println "${nexus}-snapshots password: " + mavenSettings.getText("//server[id='${nexus}-snapshots']/password/text()")
        ...

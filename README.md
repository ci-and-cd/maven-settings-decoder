# maven-settings-decoder


A tool to decrypt the XML node text stored in maven settings.xml files
===========================================================================

Maven 2.1.0+  supports [server password encryption](http://maven.apache.org/guides/mini/guide-encryption.html)
This tool lets you decrypt these encrypted text as long as you have access to both the settings.xml file and the 
settings-security.xml file.

    usage: maven-settings-decoder
     -s,--settings <arg>             location of settings.xml file.
     -ss,--settings-security <arg>   location of settings-security.xml.
     -x,--xml-path <arg>             xpath expression, only node text is supported.

    or set '-s' and '-ss' in MAVEN_SETTINGS environment variable.

Example:

    java -jar target/maven-settings-decoder-*-exec.jar \
        -s "${HOME}/.m2/settings.xml" \
        -ss "${HOME}/.m2/settings-security.xml" \
        -x "/settings/servers/server[id='local-nexus-releases']/password/text()"
    
    # or
    
    java -jar target/maven-settings-decoder-*-exec.jar \
        -s "${HOME}/.m2/settings.xml" \
        -ss "${HOME}/.m2/settings-security.xml" \
        -x "//server[id='local-nexus-releases']/password/text()"


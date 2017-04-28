#!/usr/bin/env bash

if [ -f codesigning.asc.enc ] && [ "${TRAVIS_PULL_REQUEST}" == 'false' ]; then
    openssl aes-256-cbc -K $encrypted_6107258e0e2a_key -iv $encrypted_6107258e0e2a_iv -in codesigning.asc.enc -out codesigning.asc -d
    gpg --fast-import codesigning.asc
fi

if [ -z "${CI_BUILD_REF_NAME}" ] && [ -n "${TRAVIS_BRANCH}" ]; then CI_BUILD_REF_NAME="${TRAVIS_BRANCH}"; fi

if [ -n "${TRAVIS_EVENT_TYPE}" ] && [ "${TRAVIS_EVENT_TYPE}" != "pull_request" ]; then
    case "$CI_BUILD_REF_NAME" in
        "develop")
            mvn -Dsettings.security=src/test/resources/security-settings.xml -s src/test/resources/settings.xml clean package deploy
            ;;
        release*)
            mvn -Dsettings.security=src/test/resources/security-settings.xml -s src/test/resources/settings.xml clean package deploy
            ;;
        feature*|hotfix*|"master"|*)
            mvn -Dsettings.security=src/test/resources/security-settings.xml -s src/test/resources/settings.xml clean package
            ;;
    esac
else
    mvn -Dsettings.security=src/test/resources/security-settings.xml -s src/test/resources/settings.xml clean package
fi

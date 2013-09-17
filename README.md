# Literate API

 This plugin provides an API for building models of literate build based projects. See also this [wiki page][wiki]

# Environment

The following build environment is required to build this plugin

* `java-1.6` and `maven-3.0.5`

# Build

To build the api locally:

    mvn clean verify

# Release

To release the api:

    mvn release:prepare release:perform -B

  [wiki]: http://wiki.jenkins-ci.org/display/JENKINS/Literate+API

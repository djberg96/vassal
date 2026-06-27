<img src="vassal-app/src/main/resources/icons/scalable/VASSAL.svg" width="100px" align="right"  alt="Vassal Icon"/>

# VASSAL
> The open-source boardgame engine

[![Build Status](https://github.com/vassalengine/vassal/actions/workflows/package.yml/badge.svg)](https://github.com/vassalengine/vassal/actions)
[![License: LGPL v2](https://img.shields.io/badge/License-LGPL%20v2-blue.svg)](https://www.gnu.org/licenses/lgpl-2.0)

VASSAL is a game engine for building and playing online adaptations of board games and card games. Play live on the Internet or by email. VASSAL runs on all platforms, and is free, open-source software.

## Getting started

### Minimum Requirements

VASSAL 3.8 requires Java 25 or later.

The Windows and Mac packages have an appropriate version of Java bundled with
them, so there is no need to install Java separately on those operating
systems. Mac users on Catalina (macOS 10.13) or earlier, see
[here](https://forum.vassalengine.org/t/84048).
On Linux, use your package manager to install Java 25 or later.

### Build and Run from Source

From the repository root, build and launch VASSAL on macOS or Linux with:

```sh
tools/run-vassal.sh
```

On Windows, run the PowerShell version:

```powershell
.\tools\run-vassal.ps1
```

The scripts use half of your available processors for Maven builds by default.
To use a different Maven thread count:

```sh
MAVEN_THREADS=4 tools/run-vassal.sh
```

```powershell
$env:MAVEN_THREADS = "4"
.\tools\run-vassal.ps1
```

On macOS or Linux, pass Java VM options with `VASSAL_JAVA_OPTS`:

```sh
VASSAL_JAVA_OPTS="-Dsun.java2d.uiScale=1.5" tools/run-vassal.sh
```

To run the same steps manually:

```sh
mvn -T 8 -U -pl vassal-app -am package -DskipTests
mvn -T 8 -pl vassal-app dependency:build-classpath -Dmdep.outputFile=/tmp/vassal-app.classpath
java -cp "vassal-app/target/classes:vassal-deprecation/target/classes:$(cat /tmp/vassal-app.classpath)" VASSAL.launch.ModuleManager
```

### Releases

Get the [current release](https://github.com/vassalengine/vassal/releases/latest). Read the [release notes](https://vassalengine.org/wiki/Release_Notes) to see what's new.

## Contributing

#### Bug reports

Please report bugs in the [Technical Support & Bugs](https://forum.vassalengine.org/c/technical-support-bugs/6) section of our forum.

#### Developer's guide

Read the [Developer's guide](developers-guide/developers-guide.adoc)

## License

This project is licensed under the terms of the [LGPLv2 license](LICENSE).

## Acknowledgments
YourKit supports open source projects with innovative and intelligent tools
for monitoring and profiling Java and .NET applications.
YourKit is the creator of
[YourKit Java Profiler](https://www.yourkit.com/java/profiler/),
[YourKit .NET Profiler](https://www.yourkit.com/.net/profiler/),
and [YourKit YouMonitor](https://www.yourkit.com/youmonitor/).

![YourKit-Logo](https://www.yourkit.com/images/yklogo.png)

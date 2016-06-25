#  ProjectPlutoAndroid [![Build Status](https://travis-ci.org/alexamster/ProjectPlutoAndroid.svg?branch=master)](https://travis-ci.org/alexamster/ProjectPlutoAndroid) [![Coverage Status](https://coveralls.io/repos/github/alexamster/ProjectPlutoAndroid/badge.svg?branch=master)](https://coveralls.io/github/alexamster/ProjectPlutoAndroid?branch=master)

###### Android app that will control the Project Pluto board via Bluetooth Low Energy.

## Building the Project:

In order to build the project from command line, make sure you have java installed and simply run:

```
gradlew assembleDebug
```

All required android build tools and dependencies will automatically be installed if they are missing. This is true for all targets, not just debug so ```gradlew assembleRelease ``` will behave in the same fashion.

## Continious Integration

Travis is configured to build after any push and fail if tests or lint do not pass. Code coverage will then be sent to Coveralls, which will fail the build if coverage falls below 90% or if there is a decrease larger then 3% in coverage.

https://travis-ci.org/alexamster/ProjectPlutoAndroid  
https://coveralls.io/github/alexamster/ProjectPlutoAndroid

## Internals

Bluetooth communication is broken up into 3 primary components:

``` BleScanner.java``` Scans for Low energy devices.  
``` BleConnector.java``` Connects to devices using a ScanResult provided by BleScanner.  
``` BleCommunicator.java``` Sends and receives data from connected devices.  

# README #

This README would normally document whatever steps are necessary to get your server up and running.

### What is this repository for? ###

* This is the repository for [CoCo project](http://www.se-sy.org/projects/coco/) - data collection framework - Android client.
* Version: v0.5 (with sensordrone integrated)

### How do I get set up? ###

* Android App
    -  CopresenceDataCollector-v0.5-sensordrone.apk
* Configuration
    - All parameters are configured in `Constants.java`.
* Dependencies (`libs/`)
    - Android 4.0+ (API level >= 14)
    - [StandOut](http://forum.xda-developers.com/showthread.php?t=1688531): required project on build path
    - [Gson](https://code.google.com/p/google-gson/)
    - [Apache Commons libs](https://commons.apache.org/): commons-codec, commons-math
    - [Apache log4j](https://logging.apache.org/log4j/1.2/) and [android-logging-log4j](https://code.google.com/p/android-logging-log4j/)
    - [JTransforms](https://sites.google.com/site/piotrwendykier/software/jtransforms)
    - [musicg](https://code.google.com/p/musicg/) 
    - [Sensordrone Android library](http://developer.sensordrone.com/downloads/)
* Deployment instructions
    - Make sure the `StandOut` project is imported as library.

### How do I use it? ###
* CopresenceDataCollector usage: [public wiki](https://wiki.helsinki.fi/display/SecSys/ColocationDataCollector)
* Data Specification: `data_spec.md`

### Who do I talk to? ###

* Repo owner or admin: Xiang Gao (rekygx@gmail.com)
* Team contact: Hien Truong (hien.truong@cs.helsinki.fi or truongthithuhien@gmail.com)

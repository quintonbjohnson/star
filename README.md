# STAR

Speech-to-text Augmented Reality (STAR) is an Android application that aims to provide text and speech capabilities to users with impaired hearing.

<p align="center">
  <img src ="http://i.imgur.com/4MJ2hPn.png"/>
</p>

## Release Notes
Current Version: 1.0

### New Features
* Ability to have a history of conversations has been added
* Persistent conversations have been added using a SQLite database

### Bug Fixes
* Fixed bug where clear removes all of the user's past messages

### Known Bugs
* Once a conversation has been cleared, it goes to the top of the message list
* New messages do not move a conversation to the top the message list

## Install Guide

### Prerequisites
* [Android Studio](https://developer.android.com/studio/index.html)
* An Android phone or emulator from Android Studio

### Dependent Libraries
N/A

### Download Instructions
* Download the zip from the [repository](https://github.com/quintonj/STAR) 
  * __Note:__ in the event the application is accepted to the Google Play Store, this link will be updated and the application will be able to be installed directly onto an Android phone from the store

### Build / Installation / Run Instructions 
1. Open Android Studio
2. Direct Android Studio to the folder that contains the repository you have downloaded
3. In the top toolbar, edit the Run Configuration and ensure that the module is set to “app” and the launch activity is set to “Default Activity”
4. Run the application by pressing the green arrow and select the emulator or Android phone that you are using

### Troubleshooting
#### I can’t get the app to run
Ensure that the application’s run configuration is set correctly, as stated above in the Build Instructions. The module should be set to "app", the launch activity should be set to "Default Activity".

#### I don’t see any available devices to run the app on when I press the green arrow
Ensure that you have correctly set up an emulator within Android Studio or make sure that the phone you are using is connected properly. If the problem persists, restart Android Studio or your computer.




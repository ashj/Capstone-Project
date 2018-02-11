# Capstone-Project
Capstone Project

# Signing
## run gradle task
```
./gradlew assembleRelease
```

# About installRelease Gradle task requirement
Since I implemented ```Admob```, the app is split between ```paid``` and ```free``` flavors so, run:
```
./gradlew installFreeRelease
```
or/and:
```
./gradlew installPaidRelease
```

# Credits
* https://jgilfelt.github.io/AndroidAssetStudio/icons-launcher.html
  * Launcher icons
* https://material.io/icons & Android Studio Clipart gallery
  * Notification icons
* https://medium.com/@JakobUlbrich/building-a-settings-screen-for-android-part-3-ae9793fd31ec
  * Custom time picker
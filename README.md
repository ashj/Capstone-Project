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
# Capstone-Project
Capstone Project

# Signing
## Ignore changes in keystore.properties to avoid improper commits
git update-index --assume-unchanged keystore.properties

## Signing
Put .jks file inside app

```
# file keystore.properties
storePassword=password
keyPassword=password
keyAlias=key
storeFile=apk_key\\capstone_key.jks
```

## run gradle task
./gradlew assembleRelease
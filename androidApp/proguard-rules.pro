# WifiWeakspot release rules. Libraries (Room, kotlinx.serialization, Koin, Compose) ship their own
# consumer rules; these cover what reflection reaches in our own code.

# kotlinx.serialization: keep generated serializers for our @Serializable models.
-keepclassmembers @kotlinx.serialization.Serializable class com.bernaferrari.wifiweakspot.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.bernaferrari.wifiweakspot.**$$serializer { *; }

# Stack traces in Play Console stay readable with the uploaded mapping file.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

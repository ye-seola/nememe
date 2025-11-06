-keep class io.nememe.shell.Main {
    public static void main(java.lang.String[]);
}

-dontwarn com.aayushatharva.brotli4j.**
-dontwarn com.fasterxml.jackson.databind.**
-dontwarn io.netty.channel.**
-dontwarn io.netty.internal.tcnative.**
-dontwarn io.netty.util.internal.**
-dontwarn io.netty.handler.codec.compression.**
-dontwarn io.vertx.codegen.**
-dontwarn org.slf4j.**
-dontwarn javax.**
-dontwarn jdk.**
-dontwarn org.apache.log4j.**
-dontwarn org.apache.logging.**
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.jetbrains.**
-dontwarn io.vertx.core.net.impl.**

-keep class io.vertx.codegen.**
-keep class org.slf4j.**

-repackageclasses
-allowaccessmodification
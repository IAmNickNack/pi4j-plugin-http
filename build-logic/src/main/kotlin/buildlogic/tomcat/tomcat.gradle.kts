package buildlogic.tomcat

plugins {
    java
}

dependencies {
    implementation("org.apache.tomcat.embed:tomcat-embed-core:11.0.11")
    implementation("org.apache.tomcat.embed:tomcat-embed-servlet-api:11.0.11")
}

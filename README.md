Jenkins Google Driver Uploader
===
This plugin allows you to upload artifacts to your google service account.

# Setup 

- JDK 1.8 required
- Make sure that you have `~/.m2/settings.xml` with:
```xml
<settings>
  <pluginGroups>
    <pluginGroup>org.jenkins-ci.tools</pluginGroup>
  </pluginGroups>

  <profiles>
    <!-- Give access to Jenkins plugins -->
    <profile>
      <id>jenkins</id>
      <activation>
        <activeByDefault>true</activeByDefault> <!-- change this to false, if you don't like to have it on per default -->
      </activation>
      <repositories>
        <repository>
          <id>repo.jenkins-ci.org</id>
          <url>https://repo.jenkins-ci.org/public/</url>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
          <id>repo.jenkins-ci.org</id>
          <url>https://repo.jenkins-ci.org/public/</url>
        </pluginRepository>
      </pluginRepositories>
    </profile>
  </profiles>
  <mirrors>
    <mirror>
      <id>repo.jenkins-ci.org</id>
      <url>https://repo.jenkins-ci.org/public/</url>
      <mirrorOf>m.g.o-public</mirrorOf>
    </mirror>
  </mirrors>
</settings>
```
- precompiled files is under [artifacts](jenkins-google-drive-uploader/tree/master/artifacts) directory

# Install

Create an HPI file to install in Jenkins (HPI file will be in
`target/google-drive-upload.hpi`).

    mvn clean package


# ScreenShot

![sample_image](assets/jenkins-drive-uploader.png)


# Thanks

[Marko Stipanov](https://github.com/mstipanov) for creating [Jenkins Google Driver Uploader](https://github.com/mstipanov/google-drive-upload-plugin). This helps us a lot to create this plugin.

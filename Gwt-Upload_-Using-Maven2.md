
GWTUpload up to 0.6 is published in central maven repositories.

To use latest gwtupload in a maven project just edit the pom.xml file and add the gwtupload dependency. 

**NOTE**: Check that you are using the latest gwtupload version.

     <project>
        <dependencies>
          <dependency>
            <groupId>com.googlecode.gwtupload</groupId>
            <artifactId>gwtupload</artifactId>
            <version>0.6.4</version>
          </dependency>
       </dependencies>
     </project>


Very old versions are published in the gwtupload svn structure, so you have to specify also the repository location, note that the groupId is different.
     <project>
       <repositories>
          <repository>
            <id>gwtupload</id>
            <url>http://gwtupload.googlecode.com/svn/mavenrepo/</url>
          </repository>
        </repositories>
        <dependencies>
          <dependency>
            <groupId>gwtupload</groupId>
            <artifactId>gwtupload</artifactId>
            <version>0.5.8</version>
          </dependency>
       </dependencies>
     </project>

If you want to use latest snapshot you have to include the sonatype snapshots repository in your maven file
{{{ 
  <repositories>
    <repository>
      <id>sonatype-snapshots</id>
      <url>http://oss.sonatype.org/content/repositories/snapshots</url>
      <snapshots>
        <enabled>true</enabled>
      </snapshots>
      <releases>
        <enabled>false</enabled>
      </releases>
    </repository>
  </repositories>
  <dependencies>
    <dependency>
      <groupId>com.googlecode.gwtupload</groupId>
      <artifactId>gwtupload</artifactId>
      <version>0.6.5-SNAPSHOT</version>
    </dependency>
 </dependencies>
}}}
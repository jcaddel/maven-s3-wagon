Maven S3 Wagon
-------

This wagon enables communication between Maven and Amazon S3.

pom's with a reference to this wagon can publish build artifacts (.jar's, .war's, etc) to S3.

When uploading the contents of a directory, API calls to S3 are multi-threaded.

This allows directories with a lot of content (eg when invoking mvn site-deploy) to be published very quickly

Check [Maven Central](http://search.maven.org/#search|ga|1|maven-s3-wagon) or the [Kuali Repository](http://s3browse.springsource.com/browse/maven.kuali.org/release/org/kuali/maven/wagons/maven-s3-wagon/) for the latest version


Usage
-------

Add this to the build section of a pom:

    <build>
     <extensions>
      <extension>
        <groupId>org.kuali.maven.wagons</groupId>
        <artifactId>maven-s3-wagon</artifactId>
        <version>[S3 Wagon Version]</version>
      </extension>
     </extensions>
    </build>


Add this to the distribution management section:

    <distributionManagement>
     <site>
      <id>s3.site</id>
      <url>s3://[AWS Bucket Name]/site</url>
     </site>
     <repository>
      <id>s3.release</id>
      <url>s3://[AWS Bucket Name]/release</url>
     </repository>
     <snapshotRepository>
      <id>s3.snapshot</id>
      <url>s3://[AWS Bucket Name]/snapshot</url>
     </snapshotRepository>
    </distributionManagement>
  

Add this to settings.xml

    <servers>
     <server>
      <id>s3.site</id>
      <username>[AWS Access Key ID]</username>
      <password>[AWS Secret Access Key]</password>
     </server>
     <server>
      <id>s3.release</id>
      <username>[AWS Access Key ID]</username>
      <password>[AWS Secret Access Key]</password>
     </server>
     <server>
      <id>s3.snapshot</id>
      <username>[AWS Access Key ID]</username>
      <password>[AWS Secret Access Key]</password>
     </server>
    </server>
  

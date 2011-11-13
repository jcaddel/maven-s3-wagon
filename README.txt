This wagon enables communication between Maven and Amazon S3.

A pom containing a reference to this wagon can publish a Maven site to S3 as well as deploy artifacts.

When uploading the contents of a directory the system property "maven.wagon.threads" 
controls the number of simultaneous uploads the wagon will attempt to use (default is 50)


Add this to the build section of a pom:

  <build>
    <extensions>
      <extension>
        <groupId>org.kuali.maven.wagons</groupId>
        <artifactId>maven-s3-wagon</artifactId>
        <version>1.0.33</version>
      </extension>
    </extensions>
  </build>


Add this to the distribution management section of a pom:

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
  

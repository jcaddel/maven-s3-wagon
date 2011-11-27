====
    Copyright 2004-2011 The Kuali Foundation

    Licensed under the Educational Community License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.opensource.org/licenses/ecl2.php

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
====

This wagon enables communication between Maven and Amazon S3.

pom's with a reference to this wagon can publish build artifacts (.jar's, .war's, etc) to S3.

When uploading the contents of a directory, API calls to S3 are multi-threaded.

This allows directories with a lot of content (eg when invoking mvn site-deploy) to be published very quickly

Check here for the latest version:
http://s3browse.springsource.com/browse/maven.kuali.org/release/org/kuali/maven/wagons/maven-s3-wagon/


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
  

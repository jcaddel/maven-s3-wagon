Maven S3 Wagon
-------

This wagon enables communication between Maven and Amazon S3.

pom's with a reference to this wagon can publish build artifacts (.jar's, .war's, etc) to S3.

When uploading the contents of a directory, API calls to S3 are multi-threaded.

This allows directories with a lot of content (eg when invoking mvn site-deploy) to be published very quickly

Check [Maven Central](http://search.maven.org/#search|ga|1|maven-s3-wagon) or the [Kuali Repository](http://shrub.appspot.com/maven.kuali.org/release/org/kuali/maven/wagons/maven-s3-wagon/) for the latest version


Documentation
-------

[Usage](https://github.com/jcaddel/maven-s3-wagon/wiki/Usage)

[Permissions](https://github.com/jcaddel/maven-s3-wagon/wiki/Permissions)

[Maven generated site](http://site.kuali.org/maven/wagons/maven-s3-wagon/latest/)


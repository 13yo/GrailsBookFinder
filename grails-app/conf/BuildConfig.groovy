grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.dependency.resolution = {
	// inherit Grails' default dependencies
	inherits("global") {
		// uncomment to disable ehcache
		// excludes 'ehcache'
	}
	log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
	repositories {
		grailsPlugins()
		grailsHome()
		grailsCentral()

		mavenLocal()
		mavenCentral()
		mavenRepo "http://escidoc.mis.mpg.de/maven/"

		//mavenRepo "http://snapshots.repository.codehaus.org"
		//mavenRepo "http://repository.codehaus.org"
		//mavenRepo "http://download.java.net/maven/2/"
		//mavenRepo "http://repository.jboss.com/maven2/"
	}
	dependencies {
		// specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

		// runtime 'mysql:mysql-connector-java:5.1.13'

		compile 'de.mpg.mis.neuesbibliotheksystem.utils:sybase:3'
		compile 'com.google.zxing:core:1.6-SNAPSHOT'
		compile 'com.google.zxing:javase:1.6-SNAPSHOT'

		compile ('de.mpg.mis.neuesbibliothekssystem:dbmaster-remoting:0.11.3.RC4'){
			excludes ([ group: "org.slf4j" ], [ group: "org.springframework"])
		}
	}
}

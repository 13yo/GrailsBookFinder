// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: [
		'text/html',
		'application/xhtml+xml'
	],
	xml: [
		'text/xml',
		'application/xml'
	],
	text: 'text/plain',
	js: 'text/javascript',
	rss: 'application/rss+xml',
	atom: 'application/atom+xml',
	css: 'text/css',
	csv: 'text/csv',
	all: '*/*',
	json: [
		'application/json',
		'text/json'
	],
	form: 'application/x-www-form-urlencoded',
	multipartForm: 'multipart/form-data'
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// whether to install the java.util.logging bridge for sl4j. Disable for AppEngine!
grails.logging.jul.usebridge = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// set per-environment serverURL stem for creating absolute links
environments {
	production { grails.serverURL = "http://www.changeme.com" }
	development { grails.serverURL = "http://localhost:8080/${appName}" }
	test { grails.serverURL = "http://localhost:8080/${appName}" }
}

// log4j configuration
log4j = {
	// Example of changing the log pattern for the default console
	// appender:
	//
	//appenders {
	//    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
	//}

	error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
			'org.codehaus.groovy.grails.web.pages', //  GSP
			'org.codehaus.groovy.grails.web.sitemesh', //  layouts
			'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
			'org.codehaus.groovy.grails.web.mapping', // URL mapping
			'org.codehaus.groovy.grails.commons', // core / classloading
			'org.codehaus.groovy.grails.plugins', // plugins
			'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
			'org.springframework',
			'org.hibernate',
			'net.sf.ehcache.hibernate'

	warn   'org.mortbay.log'
	//	debug	'de.mpg.mis', 'org.springframework'
	// info	'de.mpg.mis', 'org.springframework'
}

/**
 * AMQP
 */
DBMaster.amqp.brokerURL="194.95.184.184"
//DBMaster.amqp.brokerURL="escidoc3.nat"
//DBMaster.amqp.brokerURL=escidoc4.nat
//DBMaster.amqp.brokerURL=10.100.0.133
DBMaster.amqp.brokerUser="guest"
DBMaster.amqp.brokerPassword="guest"
DBMaster.amqp.doActualTopic="MIS.dbmasterEndpointTopic"
/**
 * Routing Keys für die Auswahl der zu empfangenden DTOs - alle erhalten mittels MIS.dbmasterEndpointTopic.* 
 */
DBMaster.amqp.doActualRoutingKey="MIS.dbmasterEndpointTopic.DBsDTO"
DBMaster.amqp.requestQueue="queue.request"
DBMaster.amqp.directRequestQueue="queue.drequest"

/**
 * File Access
 */
DBMaster.lists.sorterMaintain="file:/tmp/serialized/de.mpg.mis.neuesbibliothekssystem.stacks.StackSorterMaintainImplTriples.ser"
DBMaster.lists.sorterMaintainDate="file:/tmp/serialized/de.mpg.mis.neuesbibliothekssystem.stacks.StackSorterMaintainImplDate.ser"
DBMaster.lists.sorterMaintainEbab="file:/tmp/serialized/de.mpg.mis.neuesbibliothekssystem.stacks.StackSorterMaintainImplEbab.ser"
DBMaster.lists.sorterUse="file:/tmp/serialized/de.mpg.mis.neuesbibliothekssystem.stacks.StackSorterUseImplTriples.ser"
DBMaster.lists.sorterUseDate="file:/tmp/serialized/de.mpg.mis.neuesbibliothekssystem.stacks.StackSorterUseImplDate.ser"
DBMaster.lists.sorterUseEbab="file:/tmp/serialized/de.mpg.mis.neuesbibliothekssystem.stacks.StackSorterUseImplTriplesEbab.ser"
DBMaster.lists.base="/tmp/serialized/"
DBMaster.lastMaintained="file:/tmp/serialized/lastMaintained.ser"
DBMaster.files.E3_BOOKS="file:/tmp/serialized/e3_books.json"
DBMaster.files.E3_JOURNALS="file:/tmp/serialized/e3_journals.json"
DBMaster.files.E2_JOURNALS="file:/tmp/serialized/e2_journals.json"

DBMaster.files.imagesDirectory="file:/mnt/libwww/inhaltUmschlag/ruecken/"

//for development
//DBMaster.lists.sorterMaintain="file:Y:\\temp\\serialized\\de.mpg.mis.neuesbibliothekssystem.stacks.StackSorterMaintainImplTriples.ser"
//DBMaster.lists.sorterUse="file:Y:\\temp\\serialized\\de.mpg.mis.neuesbibliothekssystem.stacks.StackSorterUseImplTriples.ser"
//DBMaster.lists.sorterUseEbab="file:Y:\\temp\\serialized\\de.mpg.mis.neuesbibliothekssystem.stacks.StackSorterUseImplTriplesEbab.ser"
//DBMaster.queries="{'E3_BOOKS_QUERY':'select distinct regal#,xlo,ylo,xru,yru from training.regal_plan where ebene=3 and not regal# in (select distinct regal from training.regal_journal_item where ebene=3 and brett=1) order by regal#','E3_JOURNALS_QUERY':'select distinct regal#,xlo,ylo,xru,yru from training.regal_plan a,training.regal_journal_item b where a.ebene=3 and b.ebene=a.ebene and a.regal#=b.regal order by regal#','E2_JOURNALS_QUERY':'select distinct regal#,xlo,ylo,xru,yru from training.regal_plan where ebene=2 order by regal#'}"
//DBMaster.files.E3_BOOKS="file:Y:\\temp\\serialized\\e3_books.json"
//DBMaster.files.E3_JOURNALS="file:Y:\\temp\\serialized\\e3_journals.json"
//DBMaster.files.E2_JOURNALS="file:Y:\\temp\\serialized\\e2_journals.json"

//DBMaster.queries.E3_BOOKS_QUERY="select distinct regal#,xlo,ylo,xru,yru from training..regal_plan where ebene=3 and not regal# in (select distinct regal from training..regal_journal_item where ebene=3 and brett=1) order by regal#"
//DBMaster.queries.E3_JOURNALS_QUERY="select distinct regal#,xlo,ylo,xru,yru from training..regal_plan a,training..regal_journal_item b where a.ebene=3 and b.ebene=a.ebene and a.regal#=b.regal order by regal#"
//DBMaster.queries.E2_JOURNALS_QUERY="select distinct regal#,xlo,ylo,xru,yru from training..regal_plan where ebene=2 order by regal#"
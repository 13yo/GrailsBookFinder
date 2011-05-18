package grailsserver

import grails.test.*

class ListParserControllerTests extends ControllerUnitTestCase {
	protected void setUp() {
		super.setUp()
	}

	protected void tearDown() {
		super.tearDown()
	}

	void testSomething() {
		def l = ['a', 'b', 'c']
		if(l.findIndexOf { it == 'z' } == -1)
			println "-1"
		else if(l.findIndexOf { it == 'z' })
			println "true"
		else
			println "false"

		println "Erg="+l.findIndexOf { it == 'z' }
	}
}

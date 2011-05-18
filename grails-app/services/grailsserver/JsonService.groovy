package grailsserver

import grails.converters.*

class JsonService {

	static transactional = false

	def convertToMap(File jsonFile) {
		return JSON.parse(new FileInputStream(jsonFile), "UTF-8")
	}
}

class BootStrap {

	def doMaintain
	def listFactory

	def init = { servletContext ->
		doMaintain.init()
		listFactory.initSorter()
	}
	def destroy = {
	}
}

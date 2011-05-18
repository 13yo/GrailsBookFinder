package grailsserver

import java.io.File;

import grails.converters.*

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter

class ListParserController {

	def listFactory
	def shelfFactory

	def index = {

		def out = listFactory.useList.get(0).encodeAsJSON()
		render out
	}

	def ten = {
		def out = listFactory.useList.get(800).encodeAsJSON()
		render out
	}

	def listAll = {

		def out = listFactory.useList.encodeAsJSON()
		render out
	}

	def indexXml = {
		def out = listFactory.useList.get(0).encodeAsXML()
		render out
	}

	def maintainFirst = {
		def out = listFactory.maintainList.get(0).encodeAsJSON()
		render out
	}

	def ebabFirst = {
		def out = listFactory.maintainEbabList.get(0).encodeAsJSON()
		render out
	}

	def e3books = {
		def out = shelfFactory.getMap("E3_BOOKS").encodeAsJSON()
		render out
	}

	def map = {
		def out = shelfFactory.getMap(params.id).encodeAsJSON()
		render out
	}

	def find = {
		//		def l = listFactory.useList
		//		def item = l.findIndexOf{
		//			it.itemInfo?.get("ibarcode") == params.id
		//		}
		//		def lSub = l.subList(0,item+1).reverse()
		//		def firstItemIndex = lSub.findIndexOf{
		//			it.itemInfo?.get("erstesBuchAufBrett")
		//		}
		//		def firstItem = lSub.get(firstItemIndex)
		//
		//		def shelf = firstItem.itemInfo.get("erstesBuchAufBrett").get("regal") as Integer
		//		def level = firstItem.itemInfo.get("erstesBuchAufBrett").get("ebene") as Integer
		//		[level : level, coords : shelfFactory.getShelfCoords(level,shelf)]
		render(view:"findMobile")
	}


	def barcode = {
		//		render(view:"searchMobile",model:[data:getByID("ibarcode",params.id)])
		render getByID("ibarcode",params.id) as JSON
	}

	def bibnum = {
		//		render(view:"searchMobile",model:[data:getByID("bib",params.id)])
		render getByID("bib",params.id) as JSON
	}

	def itemnum = {
		//		render(view:"searchMobile",model:[data:getByID("item",params.id)])
		render getByID("item",params.id) as JSON
	}

	def qrcode = {
		def width = 200
		def height = 200
		def barcode = params.id
		def url = "http"+(request.isSecure()? "s://" : "://")+ request.getHeader("host")+request.getContextPath()+"/"+params.controller+"/find?s=barcode&q="+barcode;

		// (ImageIO.getWriterFormatNames() returns a list of supported formats)
		String imageFormat = "png"; // could be "gif", "tiff", "jpeg"

		BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, width, height);
		MatrixToImageWriter.writeToStream(bitMatrix, imageFormat, response.outputStream);
	}

	private def getByID(String idKey, String idValue){
		def l = listFactory.useList
		def item = l.findIndexOf{
			it.itemInfo?.get(idKey) == idValue
		}
		def lSub = l.subList(0,item+1).reverse()
		def firstItemIndex = lSub.findIndexOf{
			it.itemInfo?.get("erstesBuchAufBrett")
		}
		def lastItemIndex = l.subList(item+1,l.size()).findIndexOf {
			it.itemInfo?.get("erstesBuchAufBrett")
		}
		def firstItem = lSub.get(firstItemIndex)
		def lSubBoard = l.subList(item-firstItemIndex,lastItemIndex+item+1)
		def firstItemIndexIn = lSubBoard.findAll{it.itemInfo?.get("status") == "i"}.findIndexOf{it.itemInfo?.get("ibarcode") == l.get(item).itemInfo?.get("ibarcode")}
		//		def nextItemOnBoard = null
		//		try{
		//			nextItemOnBoard = lSubBoard.subList(lSubBoard.findIndexOf{it.itemInfo?.get("ibarcode") == l.get(item).itemInfo?.get("ibarcode")}+1,-1).find { it.itemInfo?.get("status")== "i" }
		//		} catch(e) {
		//			nextItemOnBoard = null
		//		}
		def nextItem
		def nextItemIndex
		try{
			def sublistL = l.subList(item+1, l.size())
			nextItemIndex = sublistL.findIndexOf { it.itemInfo?.get("status") == "i" }
			if(nextItemIndex != -1)
				nextItem = sublistL.get(nextItemIndex)
		}
		catch(e){
			println e
			nextItem = null
		}

		lSubBoard = lSubBoard.findAll{ it.itemInfo?.get("status") == "i" }
		def lSubIbar = []
		lSubBoard.each { lSubIbar.add(it.itemInfo?.get("ibarcode")) }

		def shelf = firstItem.itemInfo.get("erstesBuchAufBrett").get("regal") as Integer
		def level = firstItem.itemInfo.get("erstesBuchAufBrett").get("ebene") as Integer
		def options = ["shelf":shelf,
					"board":firstItem.itemInfo?.get("erstesBuchAufBrett")?.get("brett") as Integer,
					"level":level,
					"numberOnBoard":firstItemIndexIn != -1 ? firstItemIndexIn+1:nextItemIndex,
					"absoluteNumberOnBoard":firstItemIndex+1,
					"numberOfBooksOnBoard":lSubBoard.size(),
					"nextItemOnBoard":nextItem?.itemInfo?.get("ibarcode"),
					"coords":shelfFactory.getShelfCoords(level,shelf),
					"status":l.get(item).itemInfo?.get("status"),
					"boardList":lSubIbar,
					"barcode":l.get(item).itemInfo?.get("ibarcode"),
					"bibnum":l.get(item).itemInfo?.get("bib"),
					"itemnum":l.get(item).itemInfo?.get("item"),
					"qrcodeGoogleLink":buildQrUrl(l.get(item).itemInfo?.get("ibarcode"), 200, 200)]

		return options
	}

	private def buildQrUrl(barcode, sizeX, sizeY){
		StringBuilder result = new StringBuilder();
		result.append("http://chart.apis.google.com/chart?cht=qr&chs=");
		result.append(sizeX).append('x').append(sizeY).append("&chld=M");
		result.append("&chl=");
		result.append(URLEncoder.encode("http"+(request.isSecure()? "s://" : "://")+ request.getHeader("host")+request.getContextPath()+"/"+params.controller+"/find?s=barcode&q="+barcode,"UTF-8"));
		return result.toString();
	}
}

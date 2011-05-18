<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, minimum-scale=1, maximum-scale=1"> 
        <title>Find a book</title>
        <%--
        <meta name="layout" content="h5bp" />
		--%>
		<link rel="stylesheet" href="${resource(dir:'resources/jqueryMobile',file:'jquery.mobile-1.0a4.1.min.css') }" type="text/css" />
		<%--<link rel="stylesheet" href="http://code.jquery.com/mobile/1.0a4.1/jquery.mobile-1.0a4.1.min.css" />--%>
		<style>
			#mapCanvas {
				position: relative;
				top: 0;
				left: 0;
			}
        	#mapContainer, #yellowBox, #box{
        		position: absolute;
        		top: 0px;
        		left: 0px;
        	}
        	#yellowBox, #box {
        		position: absolue;
        		background-color: #ff0;
        		width: 0px;
        		height: 0px;
        		border: 1px solid #000;
        		z-index: 99;
        	}
        	#box {
        		background-color: #000;
        		z-index: 100;
        	}
        </style>
		<%--<script src="http://code.jquery.com/jquery-1.5.2.min.js"></script>--%>
		<g:javascript src="h5bp/libs/jquery-1.6.1.min.js" />
		<%--<script src="http://code.jquery.com/mobile/1.0a4.1/jquery.mobile-1.0a4.1.min.js"></script>--%>
		<script src="${resource(dir:'resources/jqueryMobile',file:'jquery.mobile-1.0a4.1.min.js') }"></script>
		<g:javascript src="h5bp/libs/jquery-ui-1.8.12.custom.min.js" />
		<script type="text/javascript" charset="utf-8">
			// Some basic values
			var jsonUrl='${createLink(controller:'listParser')}',
				mapWidth = 820, 
				mapHeight = 434,
				mapFactor = 1.0,
				boxCoords = [0,0,0,0];			
			
		    function calculateFactor(a, b){
				mapFactor = a > b ? 1 : a/b;
				return mapFactor; 
			}
			
			function scaleMap(containerID, mapID, width, height){
				var cWidth = $('body').innerWidth();
				var f = calculateFactor(cWidth, width);
				$('#'+containerID).height(Math.ceil(f*height));
				$('#'+mapID).height(Math.ceil(f*height)).width(Math.ceil(f*width));

				setBoxCoords(boxCoords);
			}

			function fillInfos(selector, data){
				<%-- var items = [];
				items.push('<li data-role="list-divider">Position</li>');
				items.push('<li>Level<span class="ui-li-count">' + data.level + '</span></li>');
				items.push('<li>Shelf<span class="ui-li-count">' + data.shelf + '</span></li>');
				items.push('<li>Board<span class="ui-li-count">' + data.board + '</span></li>');
				items.push('<li># on Board<span class="ui-li-count">' + data.numberOnBoard + '</span></li>');
				items.push('<li data-role="list-divider">Status</li>');
				items.push('<li>Status<span class="ui-li-count">' + (data.status == "i" ? 'available' : 'borrowed') + '</span></li>');
				items.push('<li data-role="list-divider">Data</li>');
				items.push('<li>Barcode <span class="ui-li-count">' + data.barcode + '</span></li>');
				items.push('<li>Bib# <span class="ui-li-count">' + data.bibnum + '</span></li>');
				items.push('<li>Item# <span class="ui-li-count">' + data.itemnum + '</span></li>'); --%>
				
				$("ul#info_list li#level span").html(data.level);
				$("ul#info_list li#level").hide();
				$("ul#info_list li#shelf span").html(data.shelf);
				$("ul#info_list li#shelf").hide();
				$("ul#info_list li#board span").html(data.board);
				$("ul#info_list li#numberOnBoard span").html(data.numberOnBoard);
				$("ul#info_list li#absoluteNumberOnBoard span").html(data.absoluteNumberOnBoard);
				$("ul#info_list li#absoluteNumberOnBoard").hide();
				$("ul#info_list li#numberOfBooksOnBoard span").html(data.numberOfBooksOnBoard);
				$("ul#info_list li#nextItemOnBoard span").html(data.nextItemOnBoard);
				$("ul#info_list li#status span").html((data.status == "i" ? 'available' : 'borrowed'));
				$("ul#info_list li#barcode span").html(data.barcode);
				$("ul#info_list li#bibnum span").html(data.bibnum);
				$("ul#info_list li#bibnum").hide();
				$("ul#info_list li#itemnum span").html(data.itemnum);
				$("ul#info_list li#itemnum").hide();
				
				$("ul#info_list li#data_devider").hide();

				<%-- var html = $('<ul/>').html(items.join('')).attr("data-role","listview").attr("data-theme","c").attr("data-dividertheme","b").html();
				html += '<h4>QR-Code</h4><img src="${createLink(controller:'listParser', action:'qrcode')}/'+data.barcode+'" />'; 
				$(selector).html(html);
				// $('<h4>QR-Code</h4><img src="${createLink(controller:'listParser', action:'qrcode')}/'+data.barcode+'" />').insertAfter(selector);
				 --%>
				 
				$("span#info_qrcode").html('<img src="${createLink(controller:'listParser', action:'qrcode')}/'+data.barcode+'" />');
			}

			function doSearch(searchKey, searchValue){
				var value = searchValue == null ? $('#'+searchKey+'_field').val() : searchValue;
				
				//$.mobile.changePage(resolveJsonUrl(searchKey,value), "slide", true, false);
				
				setBookInfos(resolveJsonUrl(searchKey,value));
				$.mobile.changePage("#map", "slide");
				return false;
			}
			
			function resolveJsonUrl(action, id){
				if(action == 'barcode')
					return '${createLink(controller:'listParser', action:'barcode')}/'+id;
				if(action == 'bibnum')
					return '${createLink(controller:'listParser', action:'bibnum')}/'+id;
				if(action == 'itemnum')
					return '${createLink(controller:'listParser', action:'itemnum')}/'+id;
			}

			function setBookInfos(url){
				$.mobile.pageLoading();	
				$.getJSON(url, function(data) {
					// mal für später - wenn mehrere Ebenen gebraucht werden
					// window.imageUrl=data.level == 3? '${resource(dir:'images/lageplan',file:'lageplan_copyarea_weiss_4.gif')}':'${resource(dir:'images/lageplan',file:'lageplan_ZS_ohneRegale.gif')}';
					fillInfos("div#info_content",data);
					setBoxCoords(data.coords);
					$.mobile.changePage("#map", "slide");
					$.mobile.pageLoading( true );	
				});
			}

			function setBoxCoords(coords){
				boxCoords = coords;
				var box = {"x":Math.floor(mapFactor*coords[0]), "y":Math.floor(mapFactor*coords[1]), "w":Math.ceil(mapFactor*(coords[2]-coords[0])), "h":Math.ceil(mapFactor*(coords[3]-coords[1])) };
				$("#yellowBox").stop(true, true);
				$("#box").css({'width':Math.floor(mapWidth/3),'height':Math.floor(mapHeight/3),'left':Math.floor((mapWidth/2)-(mapWidth/6)),'top':Math.floor((mapHeight/2)-(mapHeight/6))});
				$("#yellowBox").css({'width':box.w,'height':box.h,'left':box.x,'top':box.y});

				$("#box").animate({'width':box.w,'height':box.h,'left':box.x,'top':box.y},{queue: false, duration: 1000, complete: run});
				
				var expand = true;
				function run() {
					   if (expand)
						   $("#yellowBox").animate({
							    width: box.w*3,
							    height: box.h*3,
							    left: box.x-box.w,
							    top: box.y-box.h,
							    opacity: 0.5
							  }, {queue: false, duration: 1000, easing: 'linear', complete: run} );
					   else
						   $("#yellowBox").animate({
							    width: box.w,
							    height: box.h,
							    opacity: 1,
							    left: box.x,
							    top: box.y
							  }, {queue: false, duration: 1000, easing: 'linear', complete: run} );
					   expand = !expand;
				};
				
			}

			$(function(){
				$('body').bind('orientationchange', function(e, data){
					scaleMap("mapCanvas", "mapImg", mapWidth, mapHeight);
					$.mobile.silentScroll(0);
       			});
				${ (params.s != null && params.q != null) ?	"doSearch('"+params.s+"','"+params.q+"');" : ""}
       			scaleMap("mapCanvas", "mapImg", mapWidth, mapHeight);
			});
		   
		</script>
    </head>
    <body>
    	<div id="home" data-role="page" data-theme="b">
    		<div data-role="header">
    			<h1>Home</h1>
    		</div>
    		<div data-role="content">
    			<h2>Search Criteria</h2>
	    		<ul data-role="listview" data-inset="true" data-theme="c" data-dividertheme="b">
	    			<li data-role="list-divider">Please chose one search option</li> 
	                <li><a href="#s_barcode">Barcode</a></li>
	                <li><a href="#s_itemnum">Item Number</a></li>
	                <li><a href="#s_bibnum">BibNum</a></li>
	            </ul>
    		</div>
    		<div data-role="footer">
				<h4>&copy; MPI MIS 2011</h4>
			</div>
    	</div>
    	<div id="infos" data-role="page" data-theme="b">
			<div data-role="header">
    			<h1>Book Infos</h1>
    			<a href="#" data-rel="back" data-icon="arrow-l">Back</a>
    		</div>
    		<div data-role="content" id="info_content">
	    		
		    		<ul id="info_list" data-role="listview" data-theme="c" data-dividertheme="b">
		    			<li id="position_divider" data-role="list-divider">Position</li>
						<li id="level">Level<span class="ui-li-count"></span></li>
						<li id="shelf">Shelf<span class="ui-li-count"></span></li>
						<li id="board">Board<span class="ui-li-count"></span></li>
						<li id="numberOnBoard"># on Board<span class="ui-li-count"></span></li>
						<li id="absoluteNumberOnBoard"># on Board (absolute)<span class="ui-li-count"></span></li>
						<li id="nextItemOnBoard">Barcode of next item<span class="ui-li-count"></span></li>
						<li id="numberOfBooksOnBoard"># of Books on Board<span class="ui-li-count"></span></li>
						<li id="status_divider" data-role="list-divider">Status</li>
						<li id="status">Status<span class="ui-li-count"></span></li>
						<li id="data_divider" data-role="list-divider">Data</li>
						<li id="barcode">Barcode <span class="ui-li-count"></span></li>
						<li id="bibnum">Bib# <span class="ui-li-count"></span></li>
						<li id="itemnum">Item# <span class="ui-li-count"></span></li>
		    		</ul>
		    		<h4>QR-Code</h4><span id="info_qrcode"></span>
	    	</div>
    	</div>
    	<div id="map" data-role="page" data-theme="b">
    		<div data-role="header">
    			<h1>Map</h1>
    			<a href="#" data-rel="back" data-icon="arrow-l">Back</a>
    			<a id="infoButton" href="#infos" data-role="button" data-icon="info" data-transition="flip">Infos</a>
    		</div>
    		<div data-role="content">
	    		<div id="mapCanvas">
	    			<div id="box"></div><div id="yellowBox"></div>
	    			<div id="mapContainer">
	    				<img id="mapImg" src="${resource(dir:'images/lageplan',file:'lageplan_copyarea_weiss_4.gif')}" />
	    			</div>
	    		</div>
	    	</div>
    	</div>
    	<div id="s_barcode" data-role="page" data-theme="b">
    		<div data-role="header">
    			<h1>Search By Barcode</h1>
    			<a href="#" data-rel="back" data-icon="arrow-l">Back</a>
    		</div>
    		<div data-role="content">
                <form onSubmit="doSearch('barcode'); return false;" data-ajax="false">
    			<ul data-role="listview">
    				<li data-role="fieldcontain" >
    					<label for="barcode_field">Barcode</label><input type="text" name="code" placeholder="Barcode" id="barcode_field" /></li>
                	<li data-role="fieldcontain" > 
							<button type="submit" data-icon="search">Search</button>  
	            	</li>
                </ul>
                </form>
            </div>
            <div data-role="footer">
                <h4>Please type in the books barcode and hit the Search button.</h4>
            </div>
    	</div>
    	<div id="s_bibnum" data-role="page" data-theme="b">
    		<div data-role="header">
    			<h1>Search By BibNum</h1>
    			<a href="#" data-rel="back" data-icon="arrow-l">Back</a>
    		</div>
    		<div data-role="content">
                <form onSubmit="doSearch('bibnum'); return false;" data-ajax="false">
    			<ul data-role="listview">
    				<li data-role="fieldcontain" >
    					<label for="bibnum_field">BibNum</label><input type="text" name="code" placeholder="BibNum" id="bibnum_field" /></li>
                	<li data-role="fieldcontain" > 
							<button type="submit" data-icon="search">Search</button>  
	            	</li>
                </ul>
                </form>
            </div>
            <div data-role="footer">
                <h4>Please type in the books BibNum and hit the Search button.</h4>
            </div>
    	</div>
    	<div id="s_itemnum" data-role="page" data-theme="b">
    		<div data-role="header">
    			<h1>Search By Item Number</h1>
    			<a href="#" data-rel="back" data-icon="arrow-l">Back</a>
    		</div>
    		<div data-role="content">
    			<form onSubmit="doSearch('itemnum'); return false;" data-ajax="false">
    			<ul data-role="listview">
    				<li data-role="fieldcontain" >
    					<label for="itemnum_field">Item Number</label>
	    				<input type="text" name="code" placeholder="Item Number" id="itemnum_field" />
	    			</li>
	    			<li data-role="fieldcontain" > 
							<button type="submit" data-icon="search">Search</button>  
	            	</li>
    			</ul>
    			</form>
            </div>
            <div data-role="footer">
                <h4>Please type in the books Item Number and hit the Search button.</h4>
            </div>
    	</div>
    </body>
</html>

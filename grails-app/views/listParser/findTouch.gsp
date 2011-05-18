<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <title>Find a book</title>
        <%--
        <meta name="layout" content="h5bp" />
		--%>
		<g:javascript src="h5bp/libs/jquery-1.5.1.min.js" />
		<g:javascript src="h5bp/libs/jquery-ui-1.8.11.custom.min.js" />
		<g:javascript src="jqtouch/jqtouch.min.js" />
		<link rel="stylesheet" href="${resource(dir:'css/jqtouch',file:'jqtouch.min.css') }" type="text/css" />
		<link rel="stylesheet" href="${resource(dir:'css/jqtouch/themes/jqt',file:'theme.min.css') }" type="text/css" />
		<style>
        	#mapCanvas {
        		position: relative;
        	}
        	#mapContainer {
        		position: absolute;
        		top: 0px;
        		left: 0px;
        		z-index: 1;
        	}
        	#yellowBox, #box {
        		position: absolute;
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
		<script type="text/javascript" charset="utf-8">
			// Some basic values
			var jsonUrl='/GrailsServer/listParser/',
				mapWidth = 820, 
				mapHeight = 434,
				mapFactor = 1.0,
				boxCoords = [0,0,0,0];			
			
		   var jQT = new $.jQTouch({
		       icon: '${resource(dir:'images/jqtouch/themes/jqt/img',file:'jqtouch.png') }',
		       addGlossToIcon: false,
		       startupScreen: '${resource(dir:'images/jqtouch/themes/jqt/img',file:'jq_startup.png') }',
		       statusBar: 'black',
		       preloadImages: [
		           '${resource(dir:'images/jqtouch/themes/jqt/img',file:'back_button.png') }',
		           '${resource(dir:'images/jqtouch/themes/jqt/img',file:'back_button_clicked.png') }',
		           '${resource(dir:'images/jqtouch/themes/jqt/img',file:'button_clicked.png') }',
		           '${resource(dir:'images/jqtouch/themes/jqt/img',file:'grayButton.png') }',
		           '${resource(dir:'images/jqtouch/themes/jqt/img',file:'whiteButton.png') }',
		           '${resource(dir:'images/jqtouch/themes/jqt/img',file:'loading.gif') }'
		           ]
		   });

			function calculateFactor(a, b){
				mapFactor = a > b ? 1 : a/b;
				return mapFactor; 
			}
			
			function scaleMap(containerID, mapID, width, height){
				var cWidth = $('body').innerWidth();
				var f = calculateFactor(cWidth, width);
				$('#'+mapID).height(Math.ceil(f*height)).width(Math.ceil(f*width));

				setBoxCoords(boxCoords);
			}

			function fillInfos(selector, data){
				var items = [];
				items.push('<li>Level: <small>' + data.level + '</small></li>');
				items.push('<li>Shelf: <small>' + data.shelf + '</small></li>');
				items.push('<li>Board: <small>' + data.board + '</small></li>');
				items.push('<li># on Board: <small>' + data.numberOnBoard + '</small></li>');

				$(selector).html('');
				$('<ul/>', {
				  'class': 'rounded',
				  html: items.join('')
				}).appendTo(selector);

				$('<ul/>', {
				  'class': 'rounded',
				  html: "<li>Status: <small>" + (data.status == "i" ? "available" : "borrowed") + "</small></li>"
				}).appendTo(selector);

				items = [];
				items.push('<li>BC: <small>' + data.barcode + '</small></li>');
				items.push('<li>Bib#: <small>' + data.bibnum + '</small></li>');
				items.push('<li>Item#: <small>' + data.itemnum + '</small></li>');
				$('<ul/>', {
					  'class': 'rounded',
					  html: items.join('')
					}).appendTo(selector);
				$('<ul/>', {
					  'class': 'rounded',
					  html: '<li>QR-Code: <small><img src="' + data.qrcode + '" /></small></li>'
					}).appendTo(selector);
			}

			function doSearch(searchKey, searchValue){
				var value = searchValue == null ? $('#'+searchKey+'_field').val() : searchValue;
				setBookInfos(jsonUrl+searchKey+'/'+value);
				jQT.goTo('#map', 'slide');
				return false;
			}

			function setBookInfos(url){
				$.getJSON(url, function(data) {
					// mal für später - wenn mehrere Ebenen gebraucht werden
					// window.imageUrl=data.level == 3? '${resource(dir:'images/lageplan',file:'lageplan_copyarea_weiss_4.gif')}':'${resource(dir:'images/lageplan',file:'lageplan_ZS_ohneRegale.gif')}';
					fillInfos("#infos_orient",data);
					setBoxCoords(data.coords);
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
				$('body').bind('turn', function(e, data){
					scaleMap("mapCanvas", "mapImg", mapWidth, mapHeight);
					srollTo(0,0);
       			});
				${ (params.s != null && params.q != null) ?	"doSearch('"+params.s+"','"+params.q+"');" : ""}
       			scaleMap("mapCanvas", "mapImg", mapWidth, mapHeight);
			});
		   
		</script>
        
    </head>
    <body>
    	<div id="home" class="current">
    		<div class="toolbar">
    			<h1>Home</h1>
    		</div>
    		<h2>Search Criteria</h2>
    		<ul class="rounded">
                <li class="forward"><a href="#s_barcode">Barcode</a></li>
                <li class="forward"><a href="#s_itemnum">Item Number</a></li>
                <li class="forward"><a href="#s_bibnum">BibNum</a></li>
            </ul>
    	</div>
    	<div id="infos">
			<div class="toolbar">
    			<h1>Book Infos</h1>
    			<a href="#" class="back">Back</a>
    		</div>
    		<div id="infos_orient">
    		</div>
    	</div>
    	<div id="map">
    		<div class="toolbar">
    			<h1>Map</h1>
    			<a href="#" class="back">Back</a>
    			<a class="button flip" id="infoButton" href="#infos">Infos</a>
    		</div>
    		<div id="mapCanvas">
    			<div id="box"></div><div id="yellowBox"></div>
    			<div id="mapContainer">
    				<img id="mapImg" src="${resource(dir:'images/lageplan',file:'lageplan_copyarea_weiss_4.gif')}" />
    			</div>
    		</div>
    	</div>
    	<div id="s_barcode">
    		<div class="toolbar">
    			<h1>Search By Barcode</h1>
    			<a href="#" class="back">Back</a>
    		</div>
                <ul class="individual rounded edge edit">
                	<li><form onSubmit="doSearch('barcode'); return false;"><input type="text" name="code" placeholder="Barcode" id="barcode_field" /></form></li>
                	<li><a href="#map" class="submit">Search</a></li>
                </ul>
            <div class="info">
                Please type in the books barcode and hit the Search button.
            </div>
    	</div>
    	<div id="s_bibnum">
    		<div class="toolbar">
    			<h1>Search By BibNum</h1>
    			<a href="#" class="back">Back</a>
    		</div>
                <ul class="individual rounded edge  edit">
                	<li><form onSubmit="doSearch('bibnum'); return false;"><input type="text" name="code" placeholder="BibNum" id="bibnum_field" /></form></li>
                	<li><a href="#map">Search</a></li>
                </ul>
            <div class="info">
                Please type in the books BibNum and hit the Search button.
            </div>
    	</div>
    	<div id="s_itemnum">
    		<div class="toolbar">
    			<h1>Search By Item Number</h1>
    			<a href="#" class="back">Back</a>
    		</div>
                <ul class="individual rounded edge edit">
                	<li><form onSubmit="doSearch('itemnum'); return false;"><input type="text" name="code" placeholder="Item Number" id="itemnum_field" /></form></li>
                	<li><a href="#map">Search</a></li>
                </ul>
            <div class="info">
                Please type in the books Item Number and hit the Search button.
            </div>
    	</div>
    </body>
</html>

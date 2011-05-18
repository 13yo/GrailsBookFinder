<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <title>Find a book</title>
        <meta name="layout" content="h5bp" />
        <link rel="stylesheet" href="${resource(dir:'css/sencha',file:'sencha-touch.css') }" type="text/css">
        <style>
        	#canv {
        		position: relative;
        	}
        	#plan {
        		position: absolute;
        		top: 0px;
        		left: 0px;
        	}
        	#yellowBox, #box {
        		position: absolute;
        		background-color: #ff0;
        		width: 0px;
        		height: 0px;
        		border: 1px solid #000;
        	}
        	#box {
        		background-color: #000;
        	}
        </style>
		<g:javascript src="sencha/sencha-touch.js" />
		<g:javascript src="h5bp/libs/jquery-1.5.1.min.js" />
		<g:javascript src="h5bp/libs/jquery-ui-1.8.11.custom.min.js" />
        <script>
		var jsonUrl='/GrailsServer/listParser/',
		    imageUrl="${resource(dir:'images/lageplan',file:'lageplan_copyarea_weiss_4.gif')}",
		    planContainer='<div id="canv"><div id="yellowBox"></div><div id="box"></div><div class="plan"><img src="'+imageUrl+'" class="plan" /></div></div>',
		    imageDimension = {"w":820,"h":434},
	 		windowDimension = {"w":0,"h":0};

        Ext.setup({
			icon: '${resource(dir:'images/sencha',file:'icon.png')}',
			tabletStartupScreen: '${resource(dir:'images/sencha',file:'tablet_startup.png')}',
			phoneStartupScreen: '${resource(dir:'images/sencha',file:'phone_startup.png')}',
			glossOnIcon: false,
			onReady: function() {
				var box = {"x":1, "y":1, "w":1, "h":1 };
				var mapTab = new Ext.Panel({
					id: 'mapTab',
					title: 'Karte',
					html: planContainer,
					iconCls: 'maps',
					hidden: true,
					diabled: true,
				});
				
				var searchTab = new Ext.Panel({
					title: 'Suche',
					// label: 'Search',
					iconCls: 'search',
					items: [
					{
	                    xtype: 'fieldset',
	                    items: [{
	                        xtype: 'textfield',
	                        name : 'barcode',
	                        label: 'Barcode',
	                        id: 'barcode',
	                        useClearIcon: true,
	                    },{
		                    xtype: 'button',
	                        text: 'Search',
	                        ui: 'confirm',
	                        handler: function() {
	                        	search(Ext.getCmp('barcode'),null);
	                        }
	                    }]
					}, {
						xtype: 'fieldset',
	                    items: [{
	                        xtype: 'textfield',
	                        name : 'bibnum',
	                        label: 'BibNum',
	                        id: 'bibnum',
	                        useClearIcon: true,
	                    },{
		                    xtype: 'button',
	                        text: 'Search',
	                        ui: 'confirm',
	                        handler: function() {
	                        	search(Ext.getCmp('bibnum'),null);
	                        }
	                    }]
					}, {
						xtype: 'fieldset',
	                    items: [{
	                        xtype: 'textfield',
	                        name : 'itemnum',
		                    label: 'ItemNum',
		                    id: 'itemnum',
	                        useClearIcon: true,
	                    },{
		                    xtype: 'button',
	                        text: 'Search',
	                        ui: 'confirm',
	                        handler: function() {
	                            search(Ext.getCmp('itemnum'),null);
	                        }
	                    }]
					}]
				});

				var infosTab = new Ext.Panel({
					title: 'Infos',
					// label: 'Search',
					iconCls: 'info',
					items: [
					{
	                    xtype: 'fieldset',
	                    title: 'Position',
	                    items: [{
	                        xtype: 'textfield',
	                        name : 'Level',
	                        label: 'Level',
	                        id: 'level',
	                        disabled: true,
	                    },{
	                        xtype: 'textfield',
	                        name : 'Shelf',
	                        label: 'Shelf',
	                        id: 'shelf',
	                        disabled: true,
	                    },{
	                    	xtype: 'textfield',
	                        name : 'Board',
	                        label: 'Board',
	                        id: 'board',
	                        disabled: true,
	                    },{
	                        xtype: 'textfield',
	                        name : 'Position',
	                        label: 'Position',
	                        id: 'numberOnBoard',
	                        disabled: true,
	                    },{
	                        xtype: 'textfield',
	                        name : 'Coords',
	                        label: 'Coords',
	                        id: 'coords',
	                        disabled: true,
	                    }]
					}, {
						xtype: 'fieldset',
						title: 'Status',
	                    items: [{
	                        xtype: 'textfield',
	                        name : 'Status',
	                        id: 'status',
	                        disabled: true,
	                    }]
					}]
				});

				function fillInfosForm(data){
					Ext.getCmp('level').setValue(data.level);
					Ext.getCmp('shelf').setValue(data.shelf);
					Ext.getCmp('board').setValue(data.board);
					Ext.getCmp('numberOnBoard').setValue(data.numberOnBoard);
					Ext.getCmp('coords').setValue(data.coords);
					Ext.getCmp('status').setValue(data.status == "i" ? "available" : "borrowed");
				}

				
				var viewport = new Ext.TabPanel({
					id: 'mainView',
					fullscreen: true,
					scroll: 'both',
					frame: true,
					collapsible:true,
					ui: 'light',
					cardSwitchAnimation: {
						type: 'slide',
						cover: true
					},
					tabBar: {
		                dock: 'bottom',
		                scroll: 'horizontal',
		                layout: {
		                    pack: 'center'
		                }
		            },
					defaults: {
						scroll: 'vertical'
					},
					items: [searchTab, mapTab, infosTab],
					listeners: {
						orientationChange: orientation
					}
				});

				var titleBar = new Ext.Toolbar({
					id: 'titleToolbar',
					dock : 'top',
					xtype: 'toolbar',
					title: 'Bookfinder'	
				});

				var mainCmp = Ext.getCmp('mainView');
				orientation(null, null);
				
				function calculateFactor(a,b){
					return a/b;
				}

				function setDimension(){
					windowDimension.w = mainCmp.width;
					windowDimension.h = mainCmp.height;
				}
				
				function search(elem, event){
					setBookInfos(jsonUrl+elem.getName()+'/'+elem.getValue());
				}

				function setPlanSize(){
					if(windowDimension.w >= imageDimension.w){
						//$("#plan").width(imageDimension.w)
						drawPlan("canv", imageUrl, imageDimension, box);
					}
					else{
						var f = calculateFactor(windowDimension.w, imageDimension.w);
						var smallBox = {"x" : Math.ceil(box.x*f),
										"y" : Math.ceil(box.y*f),
										"w" : Math.ceil(box.w*f),
										"h" : Math.ceil(box.h*f)};
						var smallDimension = {"w" : windowDimension.w, "h" : Math.ceil(imageDimension.h*f)};
						drawPlan("canv", imageUrl, smallDimension, smallBox);
					}
				}

				function orientation(elem, event){
					setDimension();
					setPlanSize();
				}

				function drawPlan(id, url, planDimension, box){
					$('img.plan').attr('src',url);
					$("#yellowBox").stop(true, true);
					$(".plan").width(planDimension.w);
					$("#box").css({'width':box.w,'height':box.h,'left':box.x,'top':box.y});
					$("#yellowBox").css({'width':box.w,'height':box.h,'left':box.x,'top':box.y});
					

					var expand = true;
					$("#box").animate({width:box.w,height:box.h,left:box.x,top:box.y},1000, 'linear', run);
					
					function run() {
						   if (expand)
							   $("#yellowBox").animate({
								    width: box.w*3,
								    height: box.h*3,
								    left: box.x-box.w,
								    top: box.y-box.h,
								    opacity: 0.5
								  }, 1000, 'linear', function() {setTimeout(run)} );
						   else
							   $("#yellowBox").animate({
								    width: box.w,
								    height: box.h,
								    opacity: 1,
								    left: box.x,
								    top: box.y
								  }, 1000, 'linear', function() {setTimeout(run)} );
						   expand = !expand;
					};
				}
				
				function setBookInfos(url){
					Ext.getBody().mask('Loading...', 'x-mask-loading', false);
					$.getJSON(url, function(data) {
						window.imageUrl=data.level == 3? '${resource(dir:'images/lageplan',file:'lageplan_copyarea_weiss_4.gif')}':'${resource(dir:'images/lageplan',file:'lageplan_ZS_ohneRegale.gif')}'; 
						fillInfosForm(data);
						box = {"x":data.coords[0], "y":data.coords[1], "w":data.coords[2]-data.coords[0], "h":data.coords[3]-data.coords[1] };
					});
					Ext.getBody().unmask();
					setTimeout(goToMap,1000);
					orientation(null, null);
				}

				function goToMap(){
					viewport.setActiveItem(mapTab);
				}
			}
		});
        </script>
    </head>
    <body>
    </body>
</html>

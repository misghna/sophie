<!DOCTYPE html>
<html>

<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<title>Fire Ball</title>
	<link rel="stylesheet" href="../static/css/themes/default/jquery.mobile-1.4.5.min.css">
	<link href="https://www.jqueryscript.net/css/jquerysctipttop.css" rel="stylesheet" type="text/css">
<!-- 	<link rel="stylesheet" href="../static/_assets/css/jqm-demos.css"> -->
	<link rel="shortcut icon" href="fireball.ico">
	<script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.0.3/sockjs.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
	<script src="../static/js/jquery.js"></script>
<!-- 	<script src="../static/_assets/js/index.js"></script> -->
	<script src="../static/js/jquery.mobile-1.4.5.min.js"></script>
<!-- 	<script src="../static/js/Chart.min.js"></script> -->
<script src="../static/js/raphael.js"></script>
<script src="../static/js/justgage.js"></script>
	
	 <style id="textinput-controlgroup">
			 .marginRight { float:right;
			  font: normal 16px courier !important;
			  color:blue; }

			tr:hover {
			  background-color: lightblue;
			}
			
			.controlgroup-textinput{
				padding-top:.22em;
				padding-bottom:.22em;
			}
			table, td {
			    text-align: center;
			    font: normal 12px courier !important;
			    
			}
			
			table, th{
			    text-align: center;
			    font: bold 12px courier !important;
			    
			}
			tr:nth-child(even) {background-color: #f2f2f2}
			.blue{
				color: blue;
				font: normal 14px courier !important;
			}
			.timeTable{
				font: normal 10px courier !important;
			}
			[data-role=page]{height: 100% !important; position:relative !important;}
			[data-role=footer]{bottom:0; position:absolute !important; top: auto !important; width:100%;}  
			
			.jqcandlestick-container {
				position: relative;
				cursor: none;
				}
				.jqcandlestick-canvas {
				position: absolute;
				display: block;
				height: 100%;
				width: 100%;
				}

	    </style>


 <script type="text/javascript">
      // Generate data
      
      var data = [];
      
      var time = new Date('Dec 1, 2013 12:00').valueOf();
      
      var h = Math.floor(Math.random() * 100);
      var l = h - Math.floor(Math.random() * 20);
      var o = h - Math.floor(Math.random() * (h - l));
      var c = h - Math.floor(Math.random() * (h - l));

      var v = Math.floor(Math.random() * 1000);
      
      for (var i = 0; i < 30; i++) {
        data.push([time, o, h, l, c, v]);
        h += Math.floor(Math.random() * 10 - 5);
        l = h - Math.floor(Math.random() * 20);
        o = h - Math.floor(Math.random() * (h - l));
        c = h - Math.floor(Math.random() * (h - l));
        v += Math.floor(Math.random() * 100 - 50);
        time += 30 * 60000; // Add 30 minutes
      }
    </script>
    
	<script >
	
	
	$( document ).ready(function() {
		drawChart();
		    $("#stat").text("Loading ...");
			var totalRealized=0;
			var totalUnRealized=0;
	        var socket = new SockJS('/hello');
	        stompClient = Stomp.over(socket);
	        stompClient.connect({}, function(frame) {
	        /*     console.log('Connected: ' + frame); */ 
	            stompClient.subscribe('/topic/wsMessages', function(msg){
	             /*  console.log(greeting); */  
	                parseMessage(JSON.parse(msg.body).content);
	            });
	        },function(msg) {
	        	if(msg.indexOf("Lost connection")>-1){
	        		$("#stat").text('Offline, hit refresh to go back online!');
	        		$('#stat').prev().css("color", "red");
	        	}
	        /*  */	console.log("msg : " + message);
	        });
        
	        var real=0;var unreal=0;
	        function parseMessage(msg){
	        	console.log(msg);
	        	if(msg.indexOf("Portfolio_")>-1){	        		
	        		posMsg=JSON.parse(msg.split("dd-")[1]);
   	     	  		$("#" + posMsg.Ticker).remove(); 	     	  	 
	        		 var row = "<tr id='" + posMsg.Ticker +  "'><td>" + posMsg.Ticker + "</td><td>" + posMsg.Position +"</td><td>" + 
	        		 			Math.round(posMsg.Realized) + "</td><td>" + Math.round(posMsg.Unrealized) +"</td><td>" + Math.round(posMsg.maxUnreal) +"</td></tr>";
	        		 $("#tablePosition").append(row); 
	        	
	        	}else if(msg.indexOf("BrokerGW")>-1){
	        		$("#stat").text(msg);
	        		$('#stat').prev().css("color", "blue");
	        	
	        	}else if(msg.indexOf("realTotal-")>-1){
	        		real=Math.round(msg.split("realTotal-")[1]);
	        		$("#realized").text("Realized-Total : " + Math.round(msg.split("realTotal-")[1]));
	        		var total = real+unreal;
	        		drawGuage(total);
	        	}else if(msg.indexOf("unreal_Total-")>-1){
	        		unreal=Math.round(msg.split("unreal_Total-")[1])
	        		$("#unrealized").text("Unrealized-Total : " + Math.round(msg.split("unreal_Total-")[1]));
	        		var total = real+unreal;
	        		drawGuage(total);
	        	}else if(msg.indexOf("Total-Day-Change-")>-1){
	        		var dayChange = Math.round(msg.split("Total-Day-Change-")[1]);
	        		dayChange = dayChange.toFixed(2).replace(/(\d)(?=(\d{3})+\.)/g, '$1,');
	        		$("#dayChange").text("Total-Day-Change : " + dayChange);
	        	
	        	}else if(msg.indexOf("Net-Liquidity")>-1){
	        		var lq= Math.round(msg.split("Net-Liquidity-")[1]);
					lq= lq.toFixed(2).replace(/(\d)(?=(\d{3})+\.)/g, '$1,');
	        		$("#netLiquid").text("Net Liquidity : " + lq);
	        		
	        	}else if(msg.indexOf("TotalUsedAmount")>-1){
	        		var lq= Math.round(msg.split("TotalUsedAmount-")[1]);
					lq= lq.toFixed(2).replace(/(\d)(?=(\d{3})+\.)/g, '$1,');
	        		$("#amountUsed").text("Active Fund : " + lq);
	        		
	        	}else if(msg.indexOf("orderCandidate_")>-1){	
	        		
	        		oc=JSON.parse(msg.split("dd-")[1]);
   	     	  		
   	     	  		var res = oc.Reasonable?"YES":"NO";
   	     	  		var id= oc.Ticker + "-" + oc.Time.replace("/","-");
   	     	  	    $("#oc_" + id).remove();
	   	     	  	var fund= Math.round(oc.Investment);
					fund= fund.toFixed(2).replace(/(\d)(?=(\d{3})+\.)/g, '$1,');
	        		 var row = "<tr id='oc_" + id +  "'><td><a href='https://www.tradingview.com/chart/?symbol=" + oc.Ticker + "' target='_blank' onclick='test()'>" + oc.Ticker + "</a></td><td>" + oc.Time.replace(".", ":") +"</td><td>" + 
	        		 			oc.Action + "</td><td>" + oc.InPrice +"</td><td>" + oc.ExitPrice +"</td><td>" + oc.stopLose +"</td><td>" + oc.RiskReward +"</td><td>" + oc.shares +"</td><td>" + fund +"</td><td>" + oc.Strategy +"</td></tr>";
	        		 $("#orderCandidate").append(row); 
	        		 sortTable("orderCandidate",1);
	        	
	        	}else if(msg.indexOf("gapUpTicker_")>-1){	
	        		
	        		var gd=msg.split("dd-")[1].split(",");
   	     	  	    $("#gd_" + gd[1]).remove();
	        		var row = "<tr id='gd_" + gd[1] +  "'><td>" + gd[0] +"</td><td><a href='https://www.tradingview.com/chart/?symbol=" + gd[1] + "' target='_blank' >" + gd[1] + "</a></td><td>" + gd[2] +"</td><td>" + 
	        					gd[3] + "</td><td>" + gd[4] +"</td><td>" + gd[5] +"</td><td>" + gd[6] +"</td><td>" + gd[7] +"</td><td>" + gd[8] +"</td><td>" + gd[9] +"</td></tr>";
	        		 $("#gapUpScanner").append(row); 
	        		 sortTable("gapUpScanner",0);
	        	
	        	}else if(msg.indexOf("gapDownTicker_")>-1){	
	        		
	        		var gd=msg.split("dd-")[1].split(",");
   	     	  	    $("#gd_" + gd[1]).remove();
	        		var row = "<tr id='gd_" + gd[1] +  "'><td>" + gd[0] +"</td><td><a href='https://www.tradingview.com/chart/?symbol=" + gd[1] + "' target='_blank' >" + gd[1] + "</a></td><td>" + gd[2] +"</td><td>" + 
	        					gd[3] + "</td><td>" + gd[4] +"</td><td>" + gd[5] +"</td><td>" + gd[6] +"</td><td>" + gd[7] +"</td><td>" + gd[8] +"</td><td>" + gd[9] +"</td></tr>";
	        		 $("#gapDownScanner").append(row); 
	        		 sortTable("gapDownScanner",0);
	        	}
	        }
	        
	        $("#xxx").click(function(e){
	        	$(':mobile-pagecontainer').pagecontainer('change', '#three');
	        	loadChart();
	        });
	        	

	        
	        $.get("/initData", function(data, status){
	        	console.log(data);
	        	if(status=="success"){
	        		data.forEach(function(msg) {
	        			parseMessage(msg);
	        		});
	        		
	        	}
	        });

	        
/* 	        var host = document.location.host;
	        var pathname = document.location.pathname;
	    	var ws = new WebSocket("ws://" +host  + "/helloo");	    		
	    	
	    	ws.onmessage = function(event) {
	    	    
	    	        var message = JSON.parse(event.data);  
	    	        
	    	    }; */
	    

	   	function drawGuage(val){
   	    	var minv = 0;
   	    	var maxv = 100;
   	    	if(val>0){
   	    		maxv = val + 200;
   	    	}else if(val < 0){
   	    		maxv = 0;
   	    		minv = val - 200
   	    	}
   	    	$("#gauge").empty();
   	    	var g = new JustGage({
   	    	    id: "gauge",
   	    	    value: val,
   	    	    min: minv,
   	    	    max: maxv,
   	    	    title: "Day-Change",
   	    		customSectors : [{"lo":-10000,"hi":-1,"color":"#FF0000"},
                 {"lo":0,"hi":10000,"color":"#008000"}],
				levelColorsGradient: false
   	    	  });
	   	}
	    	    
	    function draw(label,data){
	    	 $("#myChart").remove();
	    	 $("#historicalChattPage").append('<canvas id="myChart" style="position: relative; height:50vh; width:80vw"></canvas>');
			 var ctx = $("#myChart");
			 var myChart = new Chart(ctx, {
				    type: 'line',
				    data: {
				        labels: label,
				        datasets: [{
				            label: '# Total PnL',
				            data: data,
				            borderWidth: 2,
				            borderColor: "#3e95cd",
				            fill: false
				        }]
				    },				    
				    options: {
				        scales: {
				            yAxes: [{
				                ticks: {
				                    beginAtZero:false
				                }
				            }],
				            xAxes: [{
				                ticks: {
				                    autoSkip: false,
				                    maxRotation: 90,
				                    minRotation: 90
				                }
				            }]
				        }
				    }
				});
	    }
	    
	});
	</script>
</head>

<body>

<!-- Start of first page: #two -->
<div data-role="page" id="one">

		<div data-role="header">
			<!-- <h1>FireBall Dashboard</h1>-->
			<h1>Fanus Dashboard</h1>
	
		</div><!-- /header -->

		<div role="main" class="ui-content">
		 <div id="example-8"></div>
	     	<a id="xxx" href='#' > go p 3</a>
	 		<span class="marginRight" id="netLiquid"></span></br>
	 		<span class="marginRight" id="amountUsed"></span>
			<table width="100%">
			<tr>
			<td>
				<label class="blue"  id="stat">Loading ...</label>
			</td></tr>
			</table>
			
			<table width="100%" >
				<tr><td>
					<label class="timeTable" id="refresh"></label>
				</td>
				<td>
					<label class="timeTable" id="update"></label>
				</td></tr>
			</table>
			
			<hr>
			
			<table width="100%" id="tablePosition">
				<tr><th>Ticker</th><th>Position</th><th>Realized</th><th>Unrealized</th><th>Max Unrealized</th></tr></h4>
			</table>
			<hr>
			<table width="100%">
				<tr><td><label class="blue" id="realized"></label></td>
				<td><label class="blue"  id="unrealized"></label></td>
				<td><label class="blue"  id="dayChange"></label></td></tr>			
			</table>
			 <div id="gauge" class="200x160px row" style="align:center"></div>

			
		</div><!-- /content -->
    
		<div data-role="footer" data-theme="a">
		 	 <h4>
		 	 		<a href="#two" style="text-decoration:none" data-direction="forward" class="ui-btn-b ui-corner-all">Order Candidate</a>
		 			 <a href="#three" style="text-decoration:none" data-direction="forward" class="ui-btn-b ui-corner-all">Gap Up Scanner</a>
		 			 <a href="#four" style="text-decoration:none" data-direction="forward" class="ui-btn-b ui-corner-all">Gap Down Scanner</a>
		 	 </h4> 	
		</div><!-- /footer -->
		
</div><!-- /page one -->

<!-- Start of second page: #two -->
<div data-role="page" id="two" data-theme="a">

	<div data-role="header">
		<h1>Fanus Dashboard</h1>
	</div><!-- /header -->

	<div role="main" class="ui-content">
		<table width="100%" id="orderCandidate">
				<tr><th>Ticker</th><th>Time</th><th>Action</th><th>In-Price</th><th>Out-Price</th><th>STP</th><th>PL</th><th>Shares</th><th>Fund</th><th>Strategy</th></tr>
			</table>
		
	</div><!-- /content -->

	<div data-role="footer">
		<h4><a href="#one" style="text-decoration:none" data-direction="reverse" class="ui-btn-b ui-corner-all">Main Page</a></h4>
	</div><!-- /footer -->
</div><!-- /page two -->


<!-- Start of second page: #two -->
<div data-role="page" id="three" data-theme="a">

	<div data-role="header">
		<h1>Fanus Dashboard</h1>
	</div><!-- /header -->

	<div role="main" class="ui-content">
		Gap Up
		<table width="100%" id="gapUpScanner">
				<tr><th>Rank</th><th>Ticker</th><th>Gap</th><th>Gap%</th><th>Float(M)</th><th>AvgVol(M)</th><th>Vol(M)</th><th>Max-Day-Change%</th><th>Price</th><th>MarketCap(M)</th></tr>
			</table>
		
	</div><!-- /content -->

	<div data-role="footer">
		<h4><a href="#one" style="text-decoration:none" data-direction="reverse" class="ui-btn-b ui-corner-all">Main Page</a></h4>
	</div><!-- /footer -->
</div>

<div data-role="page" id="four" data-theme="a">

	<div data-role="header">
		<h1>Fanus Dashboard</h1>
	</div><!-- /header -->

	<div role="main" class="ui-content">
		Gap Down
		<table width="100%" id="gapDownScanner">
				<tr><th>Rank</th><th>Ticker</th><th>Gap</th><th>Gap%</th><th>Float(M)</th><th>AvgVol(M)</th><th>Vol(M)</th><th>Max-Day-Change%</th><th>Price</th><th>MarketCap(M)</th></tr>
			</table>
		
	</div><!-- /content -->

	<div data-role="footer">
		<h4><a href="#one" style="text-decoration:none" data-direction="reverse" class="ui-btn-b ui-corner-all">Main Page</a></h4>
	</div><!-- /footer -->
</div>

<!-- 
<div data-role="page" id="three" data-theme="a">

	<div data-role="header">
		<h1>Fanus Dashboard</h1>
	</div>/header

	<div role="main" class="ui-content">
			<div id="demo"></div>

		
	</div>/content

	<div data-role="footer">
		<h4><a href="#one" style="text-decoration:none" data-direction="reverse" class="ui-btn-b ui-corner-all">Main Page</a></h4>
	</div>/footer
</div>  -->


</body>
</html>


    <script type="text/javascript">
      function drawChart() {
        $('#example-8').jqCandlestick({
          data: data,
          theme: 'light',
          yAxis: [{
            height: 7, // 7 / (7 + 3)
          }, {
            height: 3, // 3 / (7 + 3)
          }],
          series: [{
            type: 'candlestick',
            upStroke: '#0C0',
            downStroke: '#C00',
            downColor: 'rgba(255, 0, 0, 0.4)',
          }, {
            type: 'column',
            name: 'VOLUME',
            yAxis: 1,
            stroke: '#00C',
            color: 'rgba(0, 0, 255, 0.5)',
          }],
        });
      }

    </script>
    
    
    <script type="text/javascript">

  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', 'UA-36251023-1']);
  _gaq.push(['_setDomainName', 'jqueryscript.net']);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();

</script>

<script>

function sortTable(tableId,sortColIdx) {
	  var table, rows, switching, i, x, y, shouldSwitch;
	  table = document.getElementById(tableId);
	  switching = true;
	  /*Make a loop that will continue until
	  no switching has been done:*/
	  while (switching) {
	    //start by saying: no switching is done:
	    switching = false;
	    rows = table.getElementsByTagName("TR");
	    /*Loop through all table rows (except the
	    first, which contains table headers):*/
	    for (i = 1; i < (rows.length - 1); i++) {
	      //start by saying there should be no switching:
	      shouldSwitch = false;
	      /*Get the two elements you want to compare,
	      one from current row and one from the next:*/
	      x = rows[i].getElementsByTagName("TD")[sortColIdx];
	      y = rows[i + 1].getElementsByTagName("TD")[sortColIdx];
	      //check if the two rows should switch place:
	      if (x.innerHTML.toLowerCase() > y.innerHTML.toLowerCase()) {
	        //if so, mark as a switch and break the loop:
	        shouldSwitch= true;
	        break;
	      }
	    }
	    if (shouldSwitch) {
	      /*If a switch has been marked, make the switch
	      and mark that a switch has been done:*/
	      rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
	      switching = true;
	    }
	  }
	}
	


function loadChart(){

	var data = [];

	var time = new Date('2013-12-01 12:00').valueOf();

	var h = Math.floor(Math.random() * 100);
	var l = h - Math.floor(Math.random() * 20);
	var o = h - Math.floor(Math.random() * (h - l));
	var c = h - Math.floor(Math.random() * (h - l));

	var v = Math.floor(Math.random() * 1000);

	for (var i = 0; i < 30; i++) {
	  data.push([time, o, h, l, c, v]);
	  h += Math.floor(Math.random() * 10 - 5);
	  l = h - Math.floor(Math.random() * 20);
	  o = h - Math.floor(Math.random() * (h - l));
	  c = h - Math.floor(Math.random() * (h - l));
	  v += Math.floor(Math.random() * 100 - 50);
	  time += 30 * 60000; // Add 30 minutes
	}
	
	$('#demo').jqCandlestick({
		data: data,
		theme: 'light',
		series: [{
		type: 'candlestick',
		color: '#00C',
		}],
		});

	
}



</script>

<script type="text/javascript" src="../static/js/jquery.jqcandlestick.min.js"></script>


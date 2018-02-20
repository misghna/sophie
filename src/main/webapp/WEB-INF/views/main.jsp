<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>Login page</title>
		<link href="<c:url value='/static/css/bootstrap.css' />"  rel="stylesheet"></link>
		<link href="<c:url value='/static/css/app.css' />" rel="stylesheet"></link>
		<link rel="stylesheet" type="text/css" href="//cdnjs.cloudflare.com/ajax/libs/font-awesome/4.2.0/css/font-awesome.css" />
		<!-- Latest compiled and minified CSS -->
		<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
		
		<!-- jQuery library -->
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
		

		<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
		<link rel="stylesheet" href="http://cdn.datatables.net/1.10.16/css/jquery.dataTables.min.css">
		<script src="http://cdn.datatables.net/1.10.16/js/jquery.dataTables.min.js"></script>

<script >

$(document).ready(function() {
	$(document).ready(function() {
	    $('#example').DataTable( {
	        "columns": [
	            { "data": "id" },
	            { "data": "ticker" },
	            { "data": "action" },
	            { "data": "inPrice" },
	            { "data": "stopLoss" },
	            { "data": "maxRisk" },
	            { "data": "riskRewardRatio" },
	            { "data": "reqBy" },
	            { "data": "status" }
	        ]
	    } );
	    
	    $.get('/orderList', function(newDataArray) {
	    	var datatable = $('#example').dataTable().api();
	        datatable.clear();
	        datatable.rows.add(newDataArray);
	        datatable.draw();
	    });
	} );
} );

</script>

	</head>

	<body>

	<div class="container-fluid">
		<div class="row">
		  <div class="col-xs-12 col-md-3">
		  	Portofolio
		  </div>
		  <div class="col-xs-6 col-md-9">
			<table id="example" class="display" cellspacing="0" width="100%">
		        <thead>
		            <tr>
		                <th>Id</th>
		                <th>Ticker</th>
		                <th>Position</th>
		                <th>In Price</th>
		                <th>Stop Loss</th>
		                <th>Max Risk</th>
		                <th>PL Ratio</th>
		                <th>Req By</th>
		                <th>Status</th>
		            </tr>
		        </thead>
		    </table>
			</div>
		</div>
	</div>
	
	</body>
	
</html>
	
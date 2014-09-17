<!DOCTYPE html>
    	<html lang="en">
    	<head>
    	  <title>Search Page</title>
          <!-- Bootstrap core CSS -->
          <link href="css/bootstrap.min.css" rel="stylesheet">
          <!-- Bootstrap theme -->
          <link href="css/bootstrap-theme.min.css" rel="stylesheet">

          <!-- Custom styles for this template -->
          <link href="assets/theme.css" rel="stylesheet">

          <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
          <!--[if lt IE 9]>
          <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
          <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
          <![endif]-->
          
          <style = text/css>
             !decrease navbar size
             .navbar-nav > li > a {padding-top:5px !important; padding-bottom:5px !important;}
             .navbar {min-height:32px !important}
          </style>
          
          <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
          <script>
            $(document).ready(function(){
              $("p").click(function(){
                 $(this).hide();
              });
            });
        </script>
    	</head>
    	
    	<body role="document">
    	<!-- Fixed navbar -->
        <div class="navbar navbar-inverse navbar-fixed-top" role="navigation">
        </div>
    	
 <div class="row">
	  <div class="col-md-2">
	  </div>
	  <div class="col-md-8">
	     <form class="form-horizontal" role="form" method='GET' action="/search/results">
	        <div class="input-group">
	           <input name="search-terms" type="text" class="form-control">
	           <input type="hidden" name="from" value="0" />
	           <input type="hidden" name="size" value="${elem_per_page}" />
	           <input type="hidden" name="filter-terms" value=""/> 
	           <span class="input-group-btn">
	              <button class="btn btn-default" type="submit">Search</button>
	           </span>
	        </div><!-- /input-group -->
         </form>
	  </div>
	  <div class="col-md-2">
	  </div>
</div> <!-- row 1 -->   	
</body>
</html>

<!DOCTYPE html>
<html>
<head>
    	  <title>Search Page</title>
          <!-- Bootstrap core CSS -->
          <link href="../css/bootstrap.min.css" rel="stylesheet">
          <!-- Bootstrap theme -->
          <link href="../css/bootstrap-theme.min.css" rel="stylesheet">

          <!-- Custom styles for this template -->
          <link href="../assets/theme.css" rel="stylesheet">

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
</head>
<body>
      <!-- Fixed navbar -->
      <div class="navbar navbar-inverse navbar-fixed-top" role="navigation">
      </div>

  <div class="col-lg-6 col-md-offset-1"> <!-- offset half of the remaining size to center -->
    <form class="form-horizontal" role="form" method='POST' action="/search/results">
  
      <!--<div class="form-group">-->
      <div class="input-group">
         <input name="search-terms" type="text" value="${search_terms}" class="form-control">
           <span class="input-group-btn">
             <button class="btn btn-default" type="submit">Search</button>
           </span>
         </div><!-- /input-group -->
     </div><!-- /.form -->
 </div><!-- /.col-lg-6 -->
 <div class="col-lg-8 col-md-offset-1">
  </br>
  <p>
    <strong>Hits ${total_hits}</strong>
  </p>
  <hr>
  <p>
    <#list hits as hit>
       <b>${hit.title}</b>
       </br>
       ${hit.abstract} 
       </br>
       score ${hit.score}
       <hr>
    </#list>
  </p>
  </div>
</body>
</html>
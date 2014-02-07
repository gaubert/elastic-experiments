<!DOCTYPE html>
<html>
<head>
  <title>Welcome!</title>
</head>
<body>
  <h2>
    Hits ${total_hits}
  </h2>
  <p>
    <#list hits as hit>
       <b>${hit_index + 1}: ${hit.title}</b>
       </br>
       ${hit.abstract} 
       </br>
       score ${hit.score}
       <hr>
    </#list>
  </p>
</body>
</html>
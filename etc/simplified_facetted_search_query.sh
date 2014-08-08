#!/bin/bash
curl -XGET 'http://localhost:9200/_search?pretty=true' -d '{ "from" : 0, "size" : 30, 
  "highlight" : { 
                  "fields" : 
                     { "identificationInfo.title": {}, "identificationInfo.abstract": {} } 
                } ,  
  "facets" : 
                { 
                  "satellites":
                   {
                       "terms" : {
                                   "field" : "hierarchyNames.satellite"
                                 }
                   },
                  "instruments": 
                   { 
                       "terms" : { 
                                   "field" : "hierarchyNames.instrument" 
                                 }
                   }
                }, 
  "query" : { 
              "simple_query_string" : 
                  { "fields" : ["identificationInfo.title^10", "identificationInfo.abstract"], "query" : "iasi" } 
            } 
}
'


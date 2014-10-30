#!/bin/bash
curl -XGET 'http://localhost:9200/_search?pretty=true' -d '{ "from" : 0, "size" : 10,
  "highlight" : {
                  "fields" :
                     { "identificationInfo.title": {}, "identificationInfo.abstract": {} }
                }, 
  "query" : {
             "filtered" :
             {
               "query":
               {
                  "query_string" : { "fields" : ["identificationInfo.title^10", "identificationInfo.abstract"], "query" : "iasi" },
                  "filter": { "terms" : { "hierarchyNames.instrument" : [ "iasi"] }}
               }
             }
            }
}
'

#!/bin/bash
curl -XGET 'http://localhost:9200/_search?pretty=true' -d '{ "from" : 0, "size" : 10, 
  "highlight" : { 
                  "pre_tags" : ["<strong>"],
                  "post_tags" : ["</strong>"],
                  "fields" : 
                     { "identificationInfo.title": {"fragment_size" : 300, "number_of_fragments" : 1 }, "identificationInfo.abstract": {"fragment_size" : 8000, "number_of_fragments" : 1} } 
                } ,  
  "_source" : false,
  "query" : { 
              "simple_query_string" : 
                  { "fields" : ["identificationInfo.title^10", "identificationInfo.abstract"], "query" : "iasi" } 
            } 
}
'


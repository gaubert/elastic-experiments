#!/bin/bash
# create index and add mapping to avoid having to analyze the content of hierarchyNames 
#curl -XPUT 'http://localhost:9200/eumetsat-catalogue'

curl -XPUT 'http://localhost:9200/eumetsat-catalogue/product/_mapping' -d '
{
    "product" : {
        "properties" : {
            "hierarchyNames" : {
                "type" : "object",
                "properties" : {
                   "distribution" : { "type": "string" , "index" : "not_analyzed"}
                }
            }
        }
    }
}
'

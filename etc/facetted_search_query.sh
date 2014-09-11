#!/bin/bash
#facetted search with IASI, goes in pair with filtered_from_facet_search_query.sh
curl -XGET 'http://localhost:9200/_search?pretty=true' -d '{ "from" : 0, "size" : 10, 
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
                   },
                  "categories": 
                   { 
                       "terms" : { 
                                   "field" : "hierarchyNames.category" 
                                 }
                   },
                  "societalBenefitArea": 
                   { 
                       "terms" : { 
                                   "field" : "hierarchyNames.societalBenefitArea" 
                                 }
                   },
                  "distribution": 
                   { 
                       "terms" : { 
                                   "field" : "hierarchyNames.distribution" 
                                 }
                   }
                }, 
  "query" : { 
              "simple_query_string" : 
                  { "fields" : ["identificationInfo.title^10", "identificationInfo.abstract"], "query" : "iasi" } 
            } 
}
'


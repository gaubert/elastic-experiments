#!/bin/bash
# filtered query without global facets for clarity purposes
curl -XGET 'http://localhost:9200/_search?pretty=true' -d '
{ "from" : 0, "size" : 10,
  "highlight" : {
                  "fields" :
                     { "identificationInfo.title": {"fragment_size":150,"number_of_fragments":3}, "identificationInfo.abstract": {} }
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
              "filtered" :
              {
					 "query" :
					 {
						 "simple_query_string" :
						  {
							 "fields" : ["identificationInfo.title^10", "identificationInfo.abstract"],
							 "query" : "iasi"
						  },
						 "filter" :
						 {
							 "term" :
							 {
								 "hierarchyNames.category" : "temperature"
							 }
						 }
					 }
              }
            }
}
'

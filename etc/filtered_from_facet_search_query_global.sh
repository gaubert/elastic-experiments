#!/bin/bash
curl -XGET 'http://localhost:9200/_search?pretty=true' -d '
{ "from" : 0, "size" : 1,
  "highlight" : {
                  "fields" :
                     { "identificationInfo.title": {"fragment_size":150,"number_of_fragments":3}, "identificationInfo.abstract": {} }
                } ,
  "facets" : 
                { 
                  "satellites_global":
                   {
                       "terms" : {
                                   "field" : "hierarchyNames.satellite"
                                 },
                       "global" : true
                   },
                  "instruments_global": 
                   { 
                       "terms" : { 
                                   "field" : "hierarchyNames.instrument" 
                                 },
                       "global" : true 
                   },
                  "categories_global": 
                   { 
                       "terms" : { 
                                   "field" : "hierarchyNames.category" 
                                 },
                       "global" : true 
                   },
                  "societalBenefitArea_global": 
                   { 
                       "terms" : { 
                                   "field" : "hierarchyNames.societalBenefitArea" 
                                 },
                       "global" : true 
                   },
                  "distribution_global": 
                   { 
                       "terms" : { 
                                   "field" : "hierarchyNames.distribution" 
                                 },
                       "global" : true 
                   },
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
							 "terms" :
							 {
								 "hierarchyNames.instrument" : [ "iasi" ]
							 }
						 }
					 }
              }
            }
}
'

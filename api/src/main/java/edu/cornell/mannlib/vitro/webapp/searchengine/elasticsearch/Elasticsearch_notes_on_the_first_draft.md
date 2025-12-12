# What is this package?
* The first draft of a Elasticsearch driver for VIVO

# What has been done? 
* Implement the `SearchEngine` interface
	* Classes in `edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch`
* No attempt to add new functions.

# How to experiment with it?
* Install elasticsearch somewhere.
* Create a search index with the appropriate mapping (see below).
* Check out VIVO and this branch of Vitro (see below), and do the usual installation procedure.
* Modify `{vitro_home}/config/applicationSetup.n3` to use this driver (see below).
* Modify the `vitro.local.searchengine.url` configuration property to contain ES index base URL
* Start elasticsearch
* Start VIVO

# Not ready for production
* Documentation
	* Instructions on how to install and configure the driver.
	* Instructions on how to setup elasticsearch?
* Smoke test
	* Display a warning if the elasticsearch server is not responding.
* Functional testing
	* Are we getting the proper search results?
	* Are search results in the order that we would like?
* Improved snippets
	* Copy the technique used for Solr
* Code improvement
	* Rigorous closing of HTTP connections.
	* IOC for HTTP code, to help in unit testing
	* Consistent use of exceptions and logging
* Unit tests
* Automatic initialization of the index
	* If VIVO detects an empty index, apply the mapping.

# The next steps: adding functionality

## Stay within the framework
* Add fields that enhance the contents of the search index documents (see below).
* Add data distributors that run queries and format the output (see below).

## Go outside the framework
* Add functions to the Elasticsearch driver that the Solr driver will simply ignore.
	* Or remove Solr entirely
* Query Elasticsearch directly
	* Or write a data distributor that will run the query

# The details:

## Check out VIVO and Vitro
* For now, the Elasticsearch driver only lives in my fork of Vitro
* No changes to VIVO are required (yet).

```
git clone https://github.com/vivo-project/VIVO.git
git clone -b feature/elasticsearchExperiments https://github.com/j2blake/Vitro.git
```

## A mapping for the search index
* If the index uses the default mapping, it will not work correctly.
* Some fields must be declared as `keyword`, some as unstemmed, etc.

* Example mapping script:

```
curl -X PUT "localhost:9200/vivo?pretty" -H 'Content-Type: application/json' -d'
{
  "settings":{
    "index":{
      "analysis":{
        "tokenizer":{
          "keyword_tokenizer":{
            "type":"keyword"
          },
          "whitespace_tokenizer":{
            "type":"whitespace"
          }
        },
        "filter":{
          "lowercase_filter":{
            "type":"lowercase"
          },
          "edgengram_filter":{
            "type":"edge_ngram",
            "min_gram":2,
            "max_gram":25
          },
          "word_delimiter_filter":{
            "type":"word_delimiter",
            "generate_word_parts":true,
            "generate_number_parts":true,
            "catenate_words":false,
            "catenate_numbers":false,
            "catenate_all":false,
            "split_on_case_change":true
          },
          "porter_stem_filter":{
            "type":"snowball",
            "language":"English"
          }
        },
        "analyzer":{
          "default":{
            "type":"english"
          },
          "edgengram_untokenized":{
            "type":"custom",
            "tokenizer":"keyword_tokenizer",
            "filter":[
              "lowercase_filter",
              "edgengram_filter"
            ]
          },
          "edgengram_untokenized_query":{
            "type":"custom",
            "tokenizer":"keyword_tokenizer",
            "filter":[
              "lowercase_filter"
            ]
          },
          "edgengram_stemmed":{
            "type":"custom",
            "tokenizer":"whitespace_tokenizer",
            "filter":[
              "word_delimiter_filter",
              "lowercase_filter",
              "porter_stem_filter",
              "edgengram_filter"
            ]
          },
          "edgengram_stemmed_query":{
            "type":"custom",
            "tokenizer":"whitespace_tokenizer",
            "filter":[
              "word_delimiter_filter",
              "lowercase_filter",
              "porter_stem_filter"
            ]
          },
          "sort_field_analyzer":{
            "type":"custom",
            "tokenizer":"keyword",
            "filter":[
              "lowercase"
            ]
          }
        }
      }
    }
  },
  "mappings":{
    "dynamic_templates":[
      {
        "field_sort_template":{
          "match":"*_label_sort",
          "mapping":{
            "type":"text",
            "fields":{
              "keyword":{
                "type":"keyword"
              }
            },
            "fielddata":true,
            "analyzer":"sort_field_analyzer"
          }
        }
      },
      {
        "field_ss_template":{
          "match":"*_ss",
          "mapping":{
            "type":"text",
            "fields":{
              "keyword":{
                "type":"keyword",
                "ignore_above":256
              }
            },
            "fielddata":true
          }
        }
      },
      {
        "date_range_template":{
          "match":"*_drsim",
          "mapping":{
            "type":"date_range",
            "format":"strict_date_optional_time||epoch_millis"
          }
        }
      }
    ],
    "properties":{
      "ALLTEXT":{
        "type":"text",
        "analyzer":"english",
        "fields":{
          "keyword":{
            "type":"keyword",
            "ignore_above":256
          }
        }
      },
      "ALLTEXTUNSTEMMED":{
        "type":"text",
        "analyzer":"standard"
      },
      "DocId":{
        "type":"keyword"
      },
      "classgroup":{
        "type":"keyword"
      },
      "type":{
        "type":"keyword"
      },
      "mostSpecificTypeURIs":{
        "type":"keyword"
      },
      "indexedTime":{
        "type":"long"
      },
      "nameRaw":{
        "type":"keyword"
      },
      "URI":{
        "type":"keyword"
      },
      "THUMBNAIL":{
        "type":"integer"
      },
      "THUMBNAIL_URL":{
        "type":"keyword"
      },
      "nameLowercaseSingleValued":{
        "type":"text",
        "analyzer":"standard",
        "fielddata":true
      },
      "BETA":{
        "type":"float"
      },
      "acNameUntokenized":{
        "type":"text",
        "analyzer":"edgengram_untokenized",
        "search_analyzer":"edgengram_untokenized_query"
      },
      "acNameStemmed":{
        "type":"text",
        "analyzer":"edgengram_stemmed",
        "search_analyzer":"edgengram_stemmed_query"
      }
    }
  }
}
'
```
* __*Note:*__ The first line of the script specifies the name of the index as `vivo`. 
Any name may be used, but it must match the "base URL" that is specified in `applicationSetup.n3` (see below).
* __*Note:*__ The same first line specifies the location and port number of the elasticsearch server.
Again, any location and port may be used, but they must match the "base URL" in `applicationSetup.n3`.

## Modify `applicationSetup.n3`
* Change this:

```
# ----------------------------
#
# Search engine module: 
#    The Solr-based implementation is the only standard option, but it can be
#    wrapped in an "instrumented" wrapper, which provides additional logging 
#    and more rigorous life-cycle checking.
#

:instrumentedSearchEngineWrapper 
    a   <java:edu.cornell.mannlib.vitro.webapp.searchengine.InstrumentedSearchEngineWrapper> , 
        <java:edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine> ;
    :wraps :solrSearchEngine .

```

* To this:

```
# ----------------------------
#
# Search engine module: 
#    The Solr-based implementation is the only standard option, but it can be
#    wrapped in an "instrumented" wrapper, which provides additional logging 
#    and more rigorous life-cycle checking.
#

:instrumentedSearchEngineWrapper 
    a   <java:edu.cornell.mannlib.vitro.webapp.searchengine.InstrumentedSearchEngineWrapper> , 
        <java:edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine> ;
    :wraps :elasticSearchEngine .

:elasticSearchEngine
    a   <java:edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch.ElasticSearchEngine> ,
        <java:edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine> ;
    :hasBaseUrl "http://localhost:9200/vivo" .
```
Note that `hasBaseUrl "http://localhost:9200/vivo" .` can be omitted.

## Enhance the contents of the search index
### An example: Publication URIs in the author's search document
* Add a keyword field to the search index

```
        "publicationURI": { 
          "type": "keyword" 
        },
```

* Add a `DocumentModifier` to VIVO.

```
:documentModifier_publications
    a   <java:edu.cornell.mannlib.vitro.webapp.searchindex.documentBuilding.SelectQueryDocumentModifier> ,
        <java:edu.cornell.mannlib.vitro.webapp.searchindex.documentBuilding.DocumentModifier> ;
    rdfs:label "URIs of publications are added to publicationURI field." ;
    :hasTargetField "publicationURI" ;
    :hasSelectQuery """
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
        PREFIX vivo: <http://vivoweb.org/ontology/core#>
        PREFIX bibo: <http://purl.org/ontology/bibo/>
        SELECT ?publication 
		WHERE {
			?uri vivo:relatedBy ?authorship .
			?authorship a vivo:Authorship .
			?authorship vivo:relates ?publication .
			?publication a bibo:Document .
	    }
	    """ .
```

## Use data distributors to query the search index
* Install the Data Distribution API
* Add a distributor:

```
:drill_by_URI
    a   <java:edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor> ,
        <java:edu.cornell.library.scholars.webapp.controller.api.distribute.search.DrillDownSearchByUriDataDistributor> ;
    :actionName "searchAndDrill" .
```

* Run the query:

```
http://localhost:8080/vivo/api/dataRequest/searchAndDrill?uri=http://scholars.cornell.edu/individual/mj495
```
<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<#--Sets up script for custom form data. Including here instead of as separate javascript
file to ensure this script is always loaded first.  Can also make sure placed first in 
scripts list.-->

<script type="text/javascript">
    var customFormData = {
      menuAction:"${menuAction}",
      addMenuItem:"${addMenuItem}",
      dataGetterLabelToURI:{
      		//maps labels to URIs
      		"browseClassGroup": "java:edu.cornell.mannlib.vitro.webapp.utils.dataGetter.ClassGroupPageData",
      		"individualsForClasses": "java:edu.cornell.mannlib.vitro.webapp.utils.dataGetter.IndividualsForClassesDataGetter",
      		"sparqlQuery":"java:edu.cornell.mannlib.vitro.webapp.utils.dataGetter.SparqlQueryDataGetter",
      		"fixedHtml":"java:edu.cornell.mannlib.vitro.webapp.utils.dataGetter.FixedHTMLDataGetter",
      		"searchIndividuals":"java:edu.cornell.mannlib.vitro.webapp.utils.dataGetter.SearchIndividualsDataGetter"
      }
    };
</script>
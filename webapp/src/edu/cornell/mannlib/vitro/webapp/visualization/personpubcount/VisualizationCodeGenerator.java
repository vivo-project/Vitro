package edu.cornell.mannlib.vitro.webapp.visualization.personpubcount;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;

import edu.cornell.mannlib.vitro.webapp.controller.visualization.VisualizationController;
import edu.cornell.mannlib.vitro.webapp.controller.visualization.VisualizationFrameworkConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.constants.VOConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.BiboDocument;


public class VisualizationCodeGenerator {

	private final static Map<String, String> visDivNames = new HashMap<String, String>() {{

		put("SHORT_SPARK", "pub_count_short_sparkline_vis");
		put("FULL_SPARK", "pub_count_full_sparkline_vis");

	}};

	private static final String visualizationStyleClass = "sparkline_style";
	
	private static final String defaultVisContainerDivID = "vis_container";
	
	public static final String SHORT_SPARKLINE_MODE_URL_HANDLE = "short";
	
	public static final String FULL_SPARKLINE_MODE_URL_HANDLE = "full";
	
	private Map<String, Integer> yearToPublicationCount;

	private Log log;

	private VisVOContainer valueObjectContainer;

	public VisualizationCodeGenerator(String contextPath, 
									  String individualURIParam, 
									  String visMode, 
									  String visContainer, 
									  List<BiboDocument> authorDocuments, 
									  Map<String, Integer> yearToPublicationCount, 
									  VisVOContainer valueObjectContainer, 
									  Log log) {
		
		this.yearToPublicationCount = yearToPublicationCount;
		this.valueObjectContainer = valueObjectContainer;
		this.log = log;
		
		generateVisualizationCode(contextPath, 
				  individualURIParam, 
				  visMode, 
				  visContainer, 
				  authorDocuments);
		
		
	}
	
	private void generateVisualizationCode(String contextPath,
										   String individualURIParam, 
										   String visMode, 
										   String visContainer,
										   List<BiboDocument> authorDocuments) {
		
    	valueObjectContainer.setSparklineContent(getMainVisualizationCode(authorDocuments, 
    																	  visMode, 
    																	  visContainer));
    	
    	
    	valueObjectContainer.setSparklineContext(getVisualizationContextCode(contextPath, 
    																		 individualURIParam, 
    																		 visMode));
    	
	}

//	public VisVOContainer getValueObjectContainer() {
//		
//		
//		
//		return valueObjectContainer;
//	}
	
	private String getMainVisualizationCode(List<BiboDocument> authorDocuments,
										    String visMode, 
										    String providedVisContainerID) {

		int numOfYearsToBeRendered = 0;
		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		int shortSparkMinYear = currentYear - 10 + 1;
		
    	/*
    	 * This is required because when deciding the range of years over which the vis
    	 * was rendered we dont want to be influenced by the "DEFAULT_PUBLICATION_YEAR".
    	 * */
		Set<String> publishedYears = new HashSet(yearToPublicationCount.keySet());
    	publishedYears.remove(VOConstants.DEFAULT_PUBLICATION_YEAR);
		
		/*
		 * We are setting the default value of minPublishedYear to be 10 years before 
		 * the current year (which is suitably represented by the shortSparkMinYear),
		 * this in case we run into invalid set of published years.
		 * */
		int minPublishedYear = shortSparkMinYear;
		
		String visContainerID = null;
		
		StringBuilder visualizationCode = new StringBuilder();

//		System.out.println(yearToPublicationCount);
		if (yearToPublicationCount.size() > 0) {
			try {
				minPublishedYear = Integer.parseInt(Collections.min(publishedYears));
				System.out.println("min pub year - " + minPublishedYear);
			} catch (NoSuchElementException e1) {
				log.debug("vis: " + e1.getMessage() + " error occurred for " + yearToPublicationCount.toString());
			} catch (NumberFormatException e2) {
				log.debug("vis: " + e2.getMessage() + " error occurred for " + yearToPublicationCount.toString());
			}
		}
		
		int minPubYearConsidered = 0;
		
		/*
		 * There might be a case that the author has made his first publication within the 
		 * last 10 years but we want to make sure that the sparkline is representative of 
		 * at least the last 10 years, so we will set the minPubYearConsidered to 
		 * "currentYear - 10" which is also given by "shortSparkMinYear".
		 * */
		if (minPublishedYear > shortSparkMinYear) {
			minPubYearConsidered = shortSparkMinYear;
		} else {
			minPubYearConsidered = minPublishedYear;
		}
		
		numOfYearsToBeRendered = currentYear - minPubYearConsidered + 1;
		
		visualizationCode.append("<style type='text/css'>" +
										"." + visualizationStyleClass + " table{" +
										"		margin: 0;" +
										"  		padding: 0;" +
										"  		width: auto;" +
										"  		border-collapse: collapse;" +
										"    	border-spacing: 0;" +
										"    	vertical-align: inherit;" +
										"}" +
										/*"#sparkline_data_table {" +
												"width: auto;" +
										"}" +
										"#sparkline_data_table tfoot {" +
												"color: red;" +
												"font-size:0.9em;" +
										"}" +*/
										".sparkline_text {" +
												"margin-left:72px;" +
												"position:absolute;" +
										"}" +
										".sparkline_range {" +
												"color:#7BA69E;" +
												"font-size:0.9em;" +
												"font-style:italic;" +
										"}" +
								"</style>\n");
		
		
		

		visualizationCode.append("<script type=\"text/javascript\">\n" +
								"function drawVisualization() {\n" +
									"var data = new google.visualization.DataTable();\n" +
									"data.addColumn('string', 'Year');\n" +
									"data.addColumn('number', 'Publications');\n" +
									"data.addRows(" + numOfYearsToBeRendered + ");\n");

		int publicationCounter = 0;
		int totalPublications = 0;
		int renderedFullSparks = 0;

		
		System.out.println("min pub year v2 - " + minPublishedYear);
		for (int publicationYear = minPubYearConsidered; publicationYear <= currentYear; publicationYear++) {

				String stringPublishedYear = String.valueOf(publicationYear);
				Integer currentPublications = yearToPublicationCount.get(stringPublishedYear);

				if (currentPublications == null) {
					currentPublications = 0;
				}

				visualizationCode.append("data.setValue("
												+ publicationCounter
												+ ", 0, '"
												+ stringPublishedYear
												+ "');\n");

				visualizationCode.append("data.setValue("
												+ publicationCounter
												+ ", 1, "
												+ currentPublications
												+ ");\n");

				totalPublications += currentPublications;
				publicationCounter++;
		}

		/*
		 * Sparks that will be rendered in full mode will always be the one's which has any year
		 * associated with it. Hence.
		 * */
		renderedFullSparks = totalPublications;

		/*
		 * Total publications will also consider publications that have no year associated with
		 * it. Hence.
		 * */
		if (yearToPublicationCount.get(VOConstants.DEFAULT_PUBLICATION_YEAR) != null) {
			totalPublications += yearToPublicationCount.get(VOConstants.DEFAULT_PUBLICATION_YEAR);
		}

		String sparklineDisplayOptions = "{width: 63, height: 21, showAxisLines: false, " +
										  "showValueLabels: false, labelPosition: 'none'}";
		
		if (providedVisContainerID != null) {
			visContainerID = providedVisContainerID;
		} else {
			visContainerID = defaultVisContainerDivID;
		}
		
		
		/*
		 * By default these represents the range of the rendered sparks. Only in case of
		 * "short" sparkline mode we will set the Earliest RenderedPublication year to
		 * "currentYear - 10". 
		 * */
		valueObjectContainer.setEarliestRenderedPublicationYear(minPublishedYear);
		valueObjectContainer.setLatestRenderedPublicationYear(currentYear);
		
		/*
		 * The Full Sparkline will be rendered by default. Only if the url has specific mention of
		 * SHORT_SPARKLINE_MODE_URL_HANDLE then we render the short sparkline and not otherwise.
		 * */
		
		
		/*
		 * Since building StringBuilder objects (which is being used to store the vis code) is 
		 * essentially a side-effecting process, we have both the activators method as side-effecting.
		 * They both side-effect "visualizationCode" 
		 * */
		if (SHORT_SPARKLINE_MODE_URL_HANDLE.equalsIgnoreCase(visMode)) {
			
			valueObjectContainer.setEarliestRenderedPublicationYear(shortSparkMinYear);
			generateShortSparklineVisualizationContent(currentYear,
													   shortSparkMinYear, 
													   visContainerID, 
													   visualizationCode,
													   totalPublications, 
													   sparklineDisplayOptions);	
		} else {
			generateFullSparklineVisualizationContent(currentYear,
					   								  minPubYearConsidered,
					   								  visContainerID,
													  visualizationCode, 
													  totalPublications, 
													  renderedFullSparks,
													  sparklineDisplayOptions);
		}
		
		
		
		
		

//		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		log.debug(visualizationCode);
//		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

		return visualizationCode.toString();
	}
	
	private void generateShortSparklineVisualizationContent(int currentYear,
			int shortSparkMinYear, String visContainerID,
			StringBuilder visualizationCode, int totalPublications,
			String sparklineDisplayOptions) {
		
		/*
		 * Create a view of the data containing only the column pertaining to publication count.  
		 * */
		visualizationCode.append("var shortSparklineView = new google.visualization.DataView(data);\n" +
								 "shortSparklineView.setColumns([1]);\n");		

		/*
		 * For the short view we only want the last 10 year's view of publication count, 
		 * hence we filter the data we actually want to use for render.
		 * */
		visualizationCode.append("shortSparklineView.setRows(" +
									"data.getFilteredRows([{column: 0, " +
										"minValue: '" + shortSparkMinYear + "', " +
										"maxValue: '" + currentYear+ "'}])" +
								 ");\n");

		/*
		 * Create the vis object and draw it in the div pertaining to short-sparkline.
		 * */
		visualizationCode.append("var short_spark = new google.visualization.ImageSparkLine(" +
														"document.getElementById('" + visDivNames.get("SHORT_SPARK") + "')" +
								 ");\n" +
								 "short_spark.draw(shortSparklineView, " + sparklineDisplayOptions + ");\n");

		/*
		 * We want to display how many publication counts were considered, so this is used 
		 * to calculate this.
		 * */
		visualizationCode.append("var shortSparkRows = shortSparklineView.getViewRows();\n" +
								 "var renderedShortSparks = 0;\n" +
								 "$.each(shortSparkRows, function(index, value) {" +
								 		"renderedShortSparks += data.getValue(value, 1);" +
								 "});\n");

		
		
		/*
		 * Generate the text introducing the vis.
		 * */
		visualizationCode.append("var shortSparksText = '<p class=\"sparkline_text\">'" +
														"+ renderedShortSparks" +
														"+ ' Papers with year from '" +
														"+ ' " + totalPublications + " '" +
														"+ ' total " +
														"<span class=\"sparkline_range\">" +
														"(" + shortSparkMinYear + " - " + currentYear + ")" +
														"</span>'" +
														"+ '</p>';" +
								"$(shortSparksText).prependTo('#" + visDivNames.get("SHORT_SPARK") + "');");

		visualizationCode.append("}\n ");
		
		/*
		 * Generate the code that will activate the visualization. It takes care of creating div elements to hold 
		 * the actual sparkline image and then calling the drawVisualization function. 
		 * */
		visualizationCode.append(generateVisualizationActivator(visDivNames.get("SHORT_SPARK"), visContainerID));
		
	}
	private void generateFullSparklineVisualizationContent(
			int currentYear, int minPubYearConsidered, String visContainerID, StringBuilder visualizationCode,
			int totalPublications, int renderedFullSparks,
			String sparklineDisplayOptions) {
		visualizationCode.append("var fullSparklineView = new google.visualization.DataView(data);\n" +
								 "fullSparklineView.setColumns([1]);\n");
		
		visualizationCode.append("var full_spark = new google.visualization.ImageSparkLine(" +
												"document.getElementById('" + visDivNames.get("FULL_SPARK") + "')" +
								");\n" +
								"full_spark.draw(fullSparklineView, " + sparklineDisplayOptions + ");\n");
		
		visualizationCode.append("var allSparksText = '<p class=\"sparkline_text\"><b>Full Timeline</b> '" +
												 "+ " + renderedFullSparks + "" +
												 "+ ' papers with year from '" +
												 "+ ' " + totalPublications + " '" +
												 "+ ' total " +
												"<span class=\"sparkline_range\">" +
												"(" + minPubYearConsidered + " - " + currentYear + ")" +
												"</span>'" +
												"+ '</p>';" +
								"$(allSparksText).prependTo('#" + visDivNames.get("FULL_SPARK") +"');");
		
		visualizationCode.append("}\n ");
		
		visualizationCode.append(generateVisualizationActivator(visDivNames.get("FULL_SPARK"), visContainerID));
		
	}
	private String generateVisualizationActivator(String sparklineID, String visContainerID) {
		
		return "$(document).ready(function() {" +
								
								/*
								 * This is a nuclear option (creating the container in which everything goes)
								 * the only reason this will be ever used is the API user never submitted a 
								 * container ID in which everything goes. The alternative was to let the 
								 * vis not appear in the calling page at all. So now atleast vis appears but 
								 * appended at the bottom of the body.
								 * */
								"if ($('#" + visContainerID + "').length == 0) {" +
								"	$('<div/>', {'id': '" + visContainerID + "'" +
								"     }).appendTo('body');" +
								"}" +
								
								"if ($('#" + sparklineID + "').length == 0) {" +
								"$('<div/>', {'id': '" + sparklineID + "'," +
											 "'class': '" + visualizationStyleClass + "'" +
										"}).prependTo('#" + visContainerID + "');" +
								"}" +
								
								"drawVisualization();" +
								"});" +
								"</script>\n";
	}

	private String getVisualizationContextCode(String contextPath, String individualURI, String visMode) {

		String visualizationContextCode = "";
		if (SHORT_SPARKLINE_MODE_URL_HANDLE.equalsIgnoreCase(visMode)) {
			visualizationContextCode = generateShortVisContext(contextPath, individualURI);
		} else {
			visualizationContextCode = generateFullVisContext(contextPath, individualURI);
		}
		
		
		
		
		

//		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		log.debug(visualizationContextCode);
//		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

		return visualizationContextCode;
	}
	
	private String generateFullVisContext(String contextPath, 
										 String individualURI) {
		
		StringBuilder divContextCode = new StringBuilder();
		
		try {
			
			String downloadFileCode;
			if (yearToPublicationCount.size() > 0) {
				
				
				String downloadURL = contextPath
									 + "/admin/visQuery"
									 + "?" + VisualizationFrameworkConstants.INDIVIDUAL_URI_URL_HANDLE 
									 + "=" + URLEncoder.encode(individualURI, 
											 				   VisualizationController.URL_ENCODING_SCHEME).toString() 
									 + "&" + VisualizationFrameworkConstants.VIS_TYPE_URL_HANDLE 
									 + "=" + URLEncoder.encode(VisualizationController
											 						.PERSON_PUBLICATION_COUNT_VIS_URL_VALUE, 
											 				   VisualizationController.URL_ENCODING_SCHEME).toString() 
									 + "&" + VisualizationFrameworkConstants.RENDER_MODE_URL_HANDLE 
									 + "=" + URLEncoder.encode(VisualizationFrameworkConstants.DATA_RENDER_MODE_URL_VALUE, 
							 				 				   VisualizationController.URL_ENCODING_SCHEME).toString();
				downloadFileCode = "Download data as <a href='" + downloadURL + "'>.csv</a> file.<br />";
				
				valueObjectContainer.setDownloadDataLink(downloadURL);
			} else {
				downloadFileCode = "No data available to export.<br />";
			}
			
			String tableCode = generateDataTable();
			
			divContextCode.append("<p>" + tableCode +
								  downloadFileCode + "</p>");
			
			valueObjectContainer.setTable(tableCode);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return divContextCode.toString();
		
	}
	
	
	private String generateShortVisContext(String contextPath, 
			 String individualURI) {

		StringBuilder divContextCode = new StringBuilder();
		
		try {
		
		String fullTimelineLink;
		if (yearToPublicationCount.size() > 0) {
//			String fullTimelineNetworkURL = uri.toString() + "?" + 
//										VisualizationFrameworkConstants.INDIVIDUAL_URI_URL_HANDLE + 
//										 "=" + URLEncoder.encode(individualURI, 
//												 				 VisualizationController.URL_ENCODING_SCHEME).toString() +
//										 "&" +
//										 "vis" +
//										 "=" + URLEncoder.encode(VisualizationController
//												 						.PERSON_PUBLICATION_COUNT_VIS_URL_VALUE, 
//												 				 VisualizationController.URL_ENCODING_SCHEME).toString() +
//										 "&" +
//										 VisualizationFrameworkConstants.RENDER_MODE_URL_HANDLE + 
//										 "=" + URLEncoder.encode(VisualizationFrameworkConstants.STANDALONE_RENDER_MODE_URL_VALUE, 
//								 				 				 VisualizationController.URL_ENCODING_SCHEME).toString();
			
			
			String fullTimelineNetworkURL = contextPath
							+ "/admin/visQuery"
							+ "?" 
							+ VisualizationFrameworkConstants.INDIVIDUAL_URI_URL_HANDLE 
							+ "=" + URLEncoder.encode(individualURI, 
					 				 VisualizationController.URL_ENCODING_SCHEME).toString()
					 	    + "&"
		 				    + VisualizationFrameworkConstants.VIS_TYPE_URL_HANDLE 
							+ "=" + URLEncoder.encode("person_level", 
					 				 VisualizationController.URL_ENCODING_SCHEME).toString()
					 	    + "&"
		 				    + VisualizationFrameworkConstants.VIS_CONTAINER_URL_HANDLE 
							+ "=" + URLEncoder.encode("ego_sparkline", 
					 				 VisualizationController.URL_ENCODING_SCHEME).toString()						 				 
		 				    + "&"
		 				    + VisualizationFrameworkConstants.RENDER_MODE_URL_HANDLE
							+ "=" + URLEncoder.encode(VisualizationFrameworkConstants.STANDALONE_RENDER_MODE_URL_VALUE, 
					 				 VisualizationController.URL_ENCODING_SCHEME).toString();
			
			fullTimelineLink = "<a href='" + fullTimelineNetworkURL + "'>View full timeline and network.</a><br />";
			
			valueObjectContainer.setFullTimelineNetworkLink(fullTimelineNetworkURL);
			
		} else {
			
			fullTimelineLink = "No data available to render full timeline.<br />";
		
		}
		
		divContextCode.append("<p>" + fullTimelineLink + "</p>");
		
		} catch (UnsupportedEncodingException e) {
		e.printStackTrace();
		}
		
		return divContextCode.toString();
		
	}
	
	
	private String generateDataTable() {
		
		StringBuilder dataTable = new StringBuilder();
		
		dataTable.append("<table id='sparkline_data_table'>" +
								"<caption>Publications per year</caption>" +
								"<thead>" +
										"<tr>" +
											"<th>Year</th>" +
											"<th>Publications</th>" +
										"</tr>" +
								"</thead>" +
								"<tbody>");
		
		for (Entry<String, Integer> currentEntry : yearToPublicationCount.entrySet()) {
			dataTable.append("<tr>" +
								"<td>" + currentEntry.getKey() + "</td>" +
								"<td>" + currentEntry.getValue() + "</td>" +
							"</tr>");
		}
										
		dataTable.append("</tbody>\n" +
						"</tfoot>" +
//						"<tfoot>" +
//								"<tr><td colspan='2'>*DNA - Data not available</td></tr>" +
//						"</tfoot>\n" +
						"</table>\n");
		
		
		return dataTable.toString();
	}
	
	
	

}

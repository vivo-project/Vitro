/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.rdfservice.filter.LanguageFilteringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

import edu.cornell.mannlib.vitro.webapp.dao.MenuDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.menu.MainMenu;

import javax.servlet.ServletRequest;

public class MenuDaoJena extends JenaBaseDao implements MenuDao {

    private static final Log log = LogFactory.getLog(MenuDaoJena.class);

    static final String prefixes =
        "PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
        "PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#> \n" +
        "PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#> \n" +
        "PREFIX display: <" + VitroVocabulary.DISPLAY +"> \n";


    static final protected String menuQueryString =
        prefixes + "\n" +
        "SELECT ?menuItem ?linkText ?urlMapping  WHERE {\n" +
//        "  GRAPH ?g{\n"+
        "    ?menu rdf:type display:MainMenu .\n"+
        "    ?menu display:hasElement ?menuItem .  \n"+
        "    ?menuItem display:linkText ?linkText .\n"+
        "    OPTIONAL { ?menuItem display:menuPosition ?menuPosition }.\n"+
        "    OPTIONAL { ?menuItem display:toPage ?page . }\n"+
        "    OPTIONAL { ?page display:urlMapping ?urlMapping . }\n"+
//        "  }\n"+
        "} \n" +
        "ORDER BY ?menuPosition ?menuItemText \n";

    static{
        try{
            menuQuery=QueryFactory.create(menuQueryString);
        }catch(Throwable th){
            log.error("could not create SPARQL query for menuQueryString " + th.getMessage());
            log.error(menuQueryString);
        }
    }

    static protected Query menuQuery;

    public MenuDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }

    @Override
    public MainMenu getMainMenu( String url ) {
        return getMenu( getOntModelSelector().getDisplayModel(), url );
    }

    public MainMenu getMainMenu( ServletRequest req, String url ) {
        OntModel displayModel = LanguageFilteringUtils.wrapOntModelInALanguageFilter(getOntModelSelector().getDisplayModel(), req );
        return getMenu(displayModel,  url) ;
    }

    protected MainMenu getMenu(Model displayModel, String url){
        //setup query parameters
        QuerySolutionMap initialBindings = new QuerySolutionMap();

        //run SPARQL query to get menu and menu items
        QueryExecution qexec = QueryExecutionFactory.create(menuQuery, displayModel, initialBindings );
        try{
            // ryounes Seems suspicious that a dao is creating a template model object. What's this all about?
            MainMenu menu = new MainMenu();

            /* bdc34: currently there is no good way to decide which url to show
             * on the menu when a page has multiple urls. */
            Set <String>seenMenuItems = new HashSet<String>();

            ResultSet results =qexec.execSelect();
            for( ; results.hasNext();){
                QuerySolution soln = results.nextSolution();
                Literal itemText = soln.getLiteral("linkText");
                Literal itemLink = soln.getLiteral("urlMapping");
                RDFNode menuItem = soln.getResource("menuItem");

                String text = itemText != null ? itemText.getLexicalForm():"(undefined text)";
                String link = itemLink != null ? itemLink.getLexicalForm():"undefinedLink";
                String menuItemUri = PageDaoJena.nodeToString(menuItem);

                if( !seenMenuItems.contains(menuItemUri) ){
                    menu.addItem(text,link, isActive( url, link ));
                    seenMenuItems.add( menuItemUri );
                }
            }
            return menu;
        }catch(Throwable th){
            log.error(th,th);
            return new MainMenu();
        }
    }


    protected boolean isActive(String url, String link){
        if( "/".equals(link) )
            return "/".equals(url);
        else
            return url.startsWith(link);
    }
}

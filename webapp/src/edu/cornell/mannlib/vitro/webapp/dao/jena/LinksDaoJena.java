/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Link;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.dao.LinksDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class LinksDaoJena extends JenaBaseDao implements LinksDao {

    protected static final Log log = LogFactory.getLog(LinksDaoJena.class.getName());
    
    public LinksDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }
    
    @Override
    protected OntModel getOntModel() {
    	return getOntModelSelector().getABoxModel();
    }

    public void addLinksToIndividual(Individual individual) {
        List linksList = new ArrayList<Link>();
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            Resource entInd = ResourceFactory.createResource(individual.getURI());
            if (ADDITIONAL_LINK != null) {
                ClosableIterator links = getOntModel().listStatements(entInd,ADDITIONAL_LINK,(Resource)null);
                try {
                    while (links.hasNext()) {
                        try {
                            Resource linkRes = (Resource) ((Statement) links.next()).getObject();
                            linksList.add(linkFromLinkResource(linkRes, ADDITIONAL_LINK));
                        } catch (ClassCastException cce) {/*no thanks; we don't want any*/}
                    }
                } finally {
                    links.close();
                }
                
                if (linksList.size()>1) { // sort
                    Collections.sort(linksList,new Comparator<Link>() {
                        public int compare( Link first, Link second ) {
                            if (first==null) {
                                return 1;
                            }
                            if (second==null) {
                                return -1;
                            }
                            Collator collator = Collator.getInstance();
                            int compval = collator.compare(first.getDisplayRank(),second.getDisplayRank());
                            if (compval == 0) {
                                compval = collator.compare(first.getAnchor(),second.getAnchor());
                            }
                            return compval;
                        }
                    });
                }
                individual.setLinksList(linksList);
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }
    
    public void addPrimaryLinkToIndividual(Individual individual) {
        List linksList = new ArrayList<Link>();
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            com.hp.hpl.jena.ontology.Individual entInd = getOntModel().getIndividual(individual.getURI());
            if (PRIMARY_LINK != null) {
                ClosableIterator links = getOntModel().listStatements(entInd,PRIMARY_LINK,(Resource)null);
                try {
                    while (links.hasNext()) {
                        try {
                            Resource linkRes = (Resource) ((Statement) links.next()).getObject();
                            linksList.add(linkFromLinkResource(linkRes,PRIMARY_LINK));
                        } catch (ClassCastException cce) {/*no thanks; we don't want any*/}
                    }
                } finally {
                    links.close();
                }
                
                if (linksList.size()>1) { // sort
                    Collections.sort(linksList,new Comparator<Link>() {
                        public int compare( Link first, Link second ) {
                            if (first==null) {
                                return 1;
                            }
                            if (second==null) {
                                return -1;
                            }
                            Collator collator = Collator.getInstance();
                            int compval = collator.compare(first.getDisplayRank(),second.getDisplayRank());
                            if (compval == 0) {
                                compval = collator.compare(first.getAnchor(),second.getAnchor());
                            }
                            return compval;
                        }
                    });
                }
                
                if (linksList.size()>0) {
                    Iterator iter = linksList.iterator();
                    if (iter.hasNext()) { // take the first only
                        individual.setPrimaryLink((Link)iter.next());
                    }
                }
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }


    public void addLinksToIndividualsInObjectPropertyStatement(List objectPropertyStatements) {
        if (objectPropertyStatements != null) {
            Iterator objectPropertyStatementsIt = objectPropertyStatements.iterator();
            while (objectPropertyStatementsIt.hasNext()) {
                ObjectPropertyStatement ops = (ObjectPropertyStatement) objectPropertyStatementsIt.next();
                if (ops.getSubject() != null && ops.getSubject() instanceof Individual)
                    addLinksToIndividual((Individual)ops.getSubject());
                if (ops.getObject() != null && ops.getObject() instanceof Individual)
                    addLinksToIndividual((Individual)ops.getObject());
            }
        }
    }

    public void deleteLink(Link link) {
    	deleteLink(link,getOntModel());
    }

    public void deleteLink(Link link, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            Resource linkRes = ontModel.getResource(link.getURI());
            if (linkRes != null) {
                ontModel.removeAll(linkRes, null, null);
                ontModel.removeAll(null, null, linkRes);
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    public Link getLinkByURI(String URI) {
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            Resource linkRes = getOntModel().getResource(URI);
            if (linkRes != null) {
                return linkFromLinkResource(linkRes,ADDITIONAL_LINK);
            } else {
                return null;
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }

    public String insertNewLink(Link link) {
    	return insertNewLink(link,getOntModel());
    }

    public String insertNewLink(Link link, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            Resource indRes = ontModel.getResource(link.getEntityURI());
            if (indRes == null) {
                return null;
            }
            String linkUri = null;
            if (link.getURI() != null && link.getURI().length()>0) {
                linkUri = link.getURI();
            } else {
                linkUri = DEFAULT_NAMESPACE+"link_"+indRes.getLocalName()+link.getAnchor().replaceAll("\\W","");
            }
            Resource test = ontModel.getResource(linkUri);
            int count = 0;
            while (test != null) {
                ++count;
                linkUri+="_"+count;
                test = ontModel.getIndividual(linkUri);
            }
            Resource linkRes = ontModel.createResource(linkUri);
            linkRes.addProperty(RDF.type, LINK);
            if (link.getTypeURI() != null && link.getTypeURI().length()>0) {
                try {
                    linkRes.addProperty(RDF.type, ontModel.getResource(link.getTypeURI()));
                } catch (Exception e) {}
            }
            String urlString = link.getUrl();
            try {
                urlString = URLEncoder.encode(urlString, "UTF-8");
            } catch (UnsupportedEncodingException e) {}

            linkRes.addProperty(LINK_URL, urlString, XSDDatatype.XSDanyURI);
            linkRes.addProperty(LINK_ANCHOR, link.getAnchor(), XSDDatatype.XSDstring);
            linkRes.addProperty(LINK_DISPLAYRANK, ontModel.createTypedLiteral(link.getDisplayRank())); // String for now
            
            ontModel.add(indRes, ADDITIONAL_LINK, linkRes);
            return linkUri;
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    public void updateLink(Link link) {
    	updateLink(link,getOntModel());
    }

    public void updateLink(Link link, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            Resource linkRes = ontModel.getResource(link.getURI());
            Resource indRes = ontModel.getResource(link.getEntityURI());
            if (linkRes != null) {
                linkRes.removeAll(RDF.type);  // BJL note: a transactionless DL reasoner might choke on this approach
                linkRes.addProperty(RDF.type, LINK);
                if (link.getTypeURI() != null && link.getTypeURI().length()>0) {
                    try {
                        linkRes.addProperty(RDF.type, ontModel.getResource(link.getTypeURI()));
                    } catch (Exception e) {e.printStackTrace();}
                }
                linkRes.removeAll(LINK_URL);
                String urlString = link.getUrl();
                try {
                    urlString = URLEncoder.encode(urlString, "UTF-8");
                } catch (UnsupportedEncodingException e) {}
                linkRes.addProperty(LINK_URL, urlString, XSDDatatype.XSDanyURI);
                linkRes.removeAll(LINK_ANCHOR);
                linkRes.addProperty(LINK_ANCHOR, link.getAnchor(), XSDDatatype.XSDstring);
                if (indRes != null) {
                    ontModel.removeAll(null, ADDITIONAL_LINK, linkRes);
                    ontModel.add(indRes, ADDITIONAL_LINK, linkRes);
                }
                linkRes.removeAll(LINK_DISPLAYRANK);
                linkRes.addProperty(LINK_DISPLAYRANK, ontModel.createTypedLiteral(link.getDisplayRank())); // String for now
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    private Link linkFromLinkResource(Resource linkRes, com.hp.hpl.jena.ontology.ObjectProperty whichLinkProp) {
        Link link = new Link();
        link.setURI(linkRes.getURI());
        ClosableIterator typesIt = linkRes.listProperties(RDF.type);
        try {
            while (typesIt.hasNext()) {
                Statement st = (Statement) typesIt.next();
                try {
                    Resource typeRes = (Resource) st.getObject();
                    if (!typeRes.getURI().equalsIgnoreCase(LINK.getURI())) {  // TODO: remove IgnoreCase ; there because some serializations use "link" instead of "Link"
                        link.setTypeURI(typeRes.getURI());
                    }
                } catch (ClassCastException e) {}
            }
        } finally {
            typesIt.close();
        }
        if (LINK_ANCHOR != null) {
            try {
                ClosableIterator anchorStatements = getOntModel().listStatements(linkRes, LINK_ANCHOR, (Literal)null);
                try {
                    if (anchorStatements.hasNext()) {
                        Literal l = (Literal) ((Statement)anchorStatements.next()).getObject();
                        if (l != null) {
                            link.setAnchor(l.getString());
                        }
                    }
                } finally {
                    anchorStatements.close();
                }
            } catch (ClassCastException e) {}
        }
        if (LINK_URL != null) {
            try {
                ClosableIterator UrlStatements = getOntModel().listStatements(linkRes, LINK_URL, (Literal)null);
                try {
                    if (UrlStatements.hasNext()) {
                        Literal l = (Literal) ((Statement)UrlStatements.next()).getObject();
                        if (l != null) {
							if( (l.getDatatype() != null) && XSDDatatype.XSDanyURI.equals(l.getDatatype()) ) {
								try {
	                                link.setUrl(URLDecoder.decode(l.getLexicalForm(),"UTF-8"));
	                            } catch (UnsupportedEncodingException use) {}
							} else {
								link.setUrl(l.getLexicalForm());
							}
                            
                        }
                    }
                } finally {
                    UrlStatements.close();
                }
            } catch (ClassCastException e) {}
        }
        if (LINK_DISPLAYRANK != null) {
            try {
                ClosableIterator rankStatements = getOntModel().listStatements(linkRes, LINK_DISPLAYRANK, (Literal)null);
                try {
                    if (rankStatements.hasNext()) {
                        Literal l = (Literal) ((Statement)rankStatements.next()).getObject();
                        if (l != null) {
                            if (l.getDatatype()==XSDDatatype.XSDinteger) {
                                link.setDisplayRank(String.valueOf(l.getInt()));
                            } else if (l.getDatatype()==XSDDatatype.XSDstring) {
                                link.setDisplayRank(l.getString());
                            } else {
                                log.debug("unexpected literal datatype; saved as displayRank 10");
                                link.setDisplayRank("10");
                            }
                        }
                    }
                } finally {
                    rankStatements.close();
                }
            } catch (ClassCastException e) {}
        }

        ClosableIterator stmtIt = getOntModel().listStatements(null, (com.hp.hpl.jena.rdf.model.Property)whichLinkProp, linkRes); // jena Property, not vitro Property
        try {
            if (stmtIt.hasNext()) {
                Statement stmt = (Statement) stmtIt.next();
                Resource indRes = stmt.getSubject();
                link.setEntityURI(indRes.getURI());
                ObjectPropertyStatement op = new ObjectPropertyStatementImpl();
                op.setPropertyURI(whichLinkProp.getURI()); // "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#additionalLink");
                op.setSubjectURI(indRes.getURI());
                op.setObjectURI(linkRes.getURI());
                link.setObjectPropertyStatement(op);
            }
        } finally {
            stmtIt.close();
        }
        return link;
    }

}

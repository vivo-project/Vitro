package edu.cornell.mannlib.vitro.webapp.dao.jena;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.User;
import edu.cornell.mannlib.vitro.webapp.dao.UserDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class UserDaoJena extends JenaBaseDao implements UserDao {

    private static final String ROLE_PROTOCOL = "role:/";

    public UserDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }

    @Override
    protected OntModel getOntModel() {
    	return getOntModelSelector().getUserAccountsModel();
    }
    
    public List<User> getAllUsers() {
        List<User> allUsersList = new ArrayList<User>();
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            ClosableIterator userStmtIt = getOntModel().listStatements(null, RDF.type, USER);
            try {
                while (userStmtIt.hasNext()) {
                 Statement stmt = (Statement) userStmtIt.next();
                 OntResource subjRes = (OntResource) stmt.getSubject().as(OntResource.class);
                 allUsersList.add(userFromUserInd(subjRes));
                }
            } finally {
                userStmtIt.close();
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
        return allUsersList;
    }

    public User getUserByURI(String URI) {
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            return userFromUserInd(getOntModel().getOntResource(URI));
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }

    public String insertUser(User user) {
    	return insertUser(user,getOntModel());
    }

    public String insertUser(User user, OntModel ontModel) {
        String userURI = null;
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            userURI = (user.getURI()==null) ? DEFAULT_NAMESPACE+user.getUsername().replaceAll("\\W","") : user.getURI();
            com.hp.hpl.jena.ontology.Individual test = ontModel.getIndividual(userURI);
            int count = 0;
            while (test != null) {
                ++count;
                userURI+="_"+count;
                test = ontModel.getIndividual(userURI);
            }
            com.hp.hpl.jena.ontology.Individual userInd = ontModel.createIndividual(userURI, ontModel.getResource(USER.getURI()));
            addPropertyStringValue(userInd, ontModel.getProperty(VitroVocabulary.USER_USERNAME), user.getUsername(), ontModel);
            addPropertyStringValue(userInd, ontModel.getProperty(VitroVocabulary.USER_FIRSTNAME), user.getFirstName(), ontModel);
            addPropertyStringValue(userInd, ontModel.getProperty(VitroVocabulary.USER_LASTNAME), user.getLastName(), ontModel);
            addPropertyStringValue(userInd, ontModel.getProperty(VitroVocabulary.USER_MD5PASSWORD), user.getMd5password(), ontModel);
            addPropertyStringValue(userInd, ontModel.getProperty(VitroVocabulary.USER_ROLE), user.getRoleURI(), ontModel);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ontModel.leaveCriticalSection();
        }
        user.setURI(userURI);
        return userURI;
    }

    public void deleteUser(User user) {
    	deleteUser(user,getOntModel());
    }

    public void deleteUser(User user, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            OntResource userRes = ontModel.getOntResource(user.getURI());
            if (userRes != null) {
                userRes.remove();
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    public User getUserByUsername(String username) {
        getOntModel().enterCriticalSection(Lock.READ);
        try {    
	        Property usernameProp = getOntModel().getProperty(VitroVocabulary.USER_USERNAME);    
            Iterator stmtIt = getOntModel().listStatements(null, usernameProp, getOntModel().createTypedLiteral(username));
            if (stmtIt.hasNext()) {
                Statement stmt = (Statement) stmtIt.next();
                Individual userInd = getOntModel().getIndividual(stmt.getSubject().getURI());
                return userFromUserInd(userInd);
            } else {
            	stmtIt = getOntModel().listStatements(null, usernameProp, getOntModel().createLiteral(username));
            	if (stmtIt.hasNext()) {
                    Statement stmt = (Statement) stmtIt.next();
                    Individual userInd = getOntModel().getIndividual(stmt.getSubject().getURI());
                    return userFromUserInd(userInd);
            	} else {
            		return null;
            	}
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }

    private User userFromUserInd(OntResource userInd) {
        User user = new User();
        user.setURI(userInd.getURI());
        user.setNamespace(userInd.getNameSpace());
        user.setLocalName(userInd.getLocalName());
        try {
            user.setUsername(((Literal)userInd.getProperty(getOntModel().getProperty(VitroVocabulary.USER_USERNAME)).getObject()).getString());
        } catch (Exception e) {}
        try {
            user.setMd5password(((Literal)userInd.getProperty(getOntModel().getProperty(VitroVocabulary.USER_MD5PASSWORD)).getObject()).getString());
        } catch (Exception e) {}
        try {
            user.setOldPassword(((Literal)userInd.getProperty(getOntModel().getProperty(VitroVocabulary.USER_OLDPASSWORD)).getObject()).getString());
        } catch (Exception e) {}
        try {
            user.setLoginCount(getPropertyNonNegativeIntValue(userInd,ResourceFactory.createProperty(VitroVocabulary.USER_LOGINCOUNT)));
            if (user.getLoginCount()<0) {
            	user.setLoginCount(0);
            }
        } catch (Exception e) {e.printStackTrace();}
        try {
            user.setRoleURI(((Literal)userInd.getProperty(getOntModel().getProperty(VitroVocabulary.USER_ROLE)).getObject()).getString().substring(6));
        } catch (Exception e) {log.error("Unable to set user role\n");e.printStackTrace(); user.setRoleURI("1");}  // TODO: fix this
        try {
            user.setLastName(((Literal)userInd.getProperty(getOntModel().getProperty(VitroVocabulary.USER_LASTNAME)).getObject()).getString());
        } catch (Exception e) {}
        try {
            user.setFirstName(((Literal)userInd.getProperty(getOntModel().getProperty(VitroVocabulary.USER_FIRSTNAME)).getObject()).getString());
        } catch (Exception e) {}
        try {
            user.setFirstTime(getPropertyDateTimeValue(userInd, getOntModel().getProperty(VitroVocabulary.vitroURI+"firstTime")));
        } catch (Exception e) {}
        return user;
    }

    public void updateUser(User user) {
    	updateUser(user,getOntModel());
    }

    public void updateUser(User user, OntModel ontModel) {
       ontModel.enterCriticalSection(Lock.WRITE);
        try {
            OntResource userRes = ontModel.getOntResource(user.getURI());
            if (userRes != null) {
                updatePropertyStringValue(userRes, ontModel.getProperty(VitroVocabulary.USER_USERNAME), user.getUsername(), ontModel);
                updatePropertyStringValue(userRes, ontModel.getProperty(VitroVocabulary.USER_FIRSTNAME), user.getFirstName(), ontModel);
                updatePropertyStringValue(userRes, ontModel.getProperty(VitroVocabulary.USER_LASTNAME), user.getLastName(), ontModel);
                if (user.getRoleURI() != null && user.getRoleURI().indexOf(ROLE_PROTOCOL) != 0) {
                    user.setRoleURI(ROLE_PROTOCOL+user.getRoleURI());
                }
                updatePropertyStringValue(userRes, ontModel.getProperty(VitroVocabulary.USER_ROLE), user.getRoleURI(), ontModel);
                updatePropertyStringValue(userRes, ontModel.getProperty(VitroVocabulary.USER_MD5PASSWORD), user.getMd5password(), ontModel);
                updatePropertyStringValue(userRes, ontModel.getProperty(VitroVocabulary.USER_OLDPASSWORD), user.getOldPassword(), ontModel);
                updatePropertyDateTimeValue(userRes, ontModel.getProperty(VitroVocabulary.USER_FIRSTTIME), user.getFirstTime(), ontModel);
                updatePropertyNonNegativeIntValue(userRes, ResourceFactory.createProperty(VitroVocabulary.USER_LOGINCOUNT), user.getLoginCount(), ontModel);
            } else {
                log.error("DEBUG UserDaoJena - "+user.getURI()+" not found");
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    public List<String> getIndividualsUserMayEditAs(String userURI) {
        List<String> uris = new ArrayList<String>();
        OntModel ontModel = getOntModel();
        ontModel.enterCriticalSection(Lock.READ);
        try{
             StmtIterator it = ontModel.listStatements(
                    ontModel.createResource(userURI),
                    ontModel.getProperty(VitroVocabulary.MAY_EDIT_AS),
                    (RDFNode)null);
            while(it.hasNext()){
                try{
                    Statement stmt = (Statement) it.next();
                    if( stmt != null && stmt.getObject()!= null 
                            && stmt.getObject().asNode() != null 
                            && stmt.getObject().asNode().getURI() != null )
                        uris.add(stmt.getObject().asNode().getURI());
                }catch(Exception ex){
                    log.debug("error in getIndividualsUserMayEditAs()",ex);
                }
            }
        }finally{
            ontModel.leaveCriticalSection();
        }
        return uris;
    }

}

package edu.cornell.mannlib.vitro.webapp.utils;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Link;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * makes a cornell library openUrl for
 * an entity using info found in DataProps.
 *
 * @author bdc34
 *
 */
public class OpenUrlMaker {
    //These are datapropIds that are acceptable values for the given field
    private int atitle[] = {36};
    private int spage[]  = {39};
    private int aulast[] = {};
    private int issue[]  = {53};
    private int volume[] = {54};
    private int auint[]  = {};
    private int doi[]    = {55};
    private int aufull[] = {44};

    private Pattern spage_regex = Pattern.compile("[^0-9]*([0-9]+)[^0-9]*.*");
    //private Pattern spage_regex = Pattern.compile("([0-9]*)[^0-9]*([0-9]*)[^0-9]*.*");
    private Pattern aulast_regex = Pattern.compile("([^\\s]*)[\\s]*.*");
    private Pattern firstNumer_regex = Pattern.compile("[^0-9]*([0-9]+)[^0-9]*.*");
    /**
     * Uses the DatatypeProperty objects for the Entity
     * to make a OpenURL for the Cornell library resolver.
     *
     * @param ent
     * @return returns Link object if one can be made, null otherwise.
     */
    public Link makeOpenUrlForEntity(Individual ent){
        Link oUrl = null;

        String atitle = null;
        String spage = null;
        String aulast = null;
        String issue = null;
        String volume = null;
        String auint = null;
        String doi = null;
        String aufull = null;

        List <DataPropertyStatement> dataPropertyStmts = ent.getDataPropertyStatements();
        for( DataPropertyStatement dataPropertyStmt : dataPropertyStmts){
            if( found( this.atitle, dataPropertyStmt ) ){
                atitle = escape(dataPropertyStmt.getData());
            } else
                if( found( this.spage, dataPropertyStmt ) ){
                Matcher m = this.spage_regex.matcher(dataPropertyStmt.getData());
                if( m.matches() && m.groupCount() > 0 )
                    spage = m.group(1);
            } else
                if( found( this.aulast, dataPropertyStmt ) ){
                    Matcher m = this.aulast_regex.matcher(dataPropertyStmt.getData());
                    if( m.matches() && m.groupCount() > 0 )
                        aulast = m.group(1);
            } else
                if( found( this.issue, dataPropertyStmt ) ){
                    Matcher m = this.firstNumer_regex.matcher(dataPropertyStmt.getData());
                    if( m.matches() && m.groupCount() > 0 )
                        issue = m.group(1);
            } else
                if( found( this.volume, dataPropertyStmt ) ){

            } else if( found( this.auint, dataPropertyStmt ) ){
            } else if( found( this.doi, dataPropertyStmt ) ){
            } else if( found( this.aufull, dataPropertyStmt ) ){
            }
        }



        return oUrl;
    }

    private boolean found( int[] ids, DataPropertyStatement dataPropertyStmt){
        return true;
    }

    private String escape(String in){
        return in;
    }
}

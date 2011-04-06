/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering.filters;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import net.sf.jga.fn.UnaryFunctor;
import net.sf.jga.fn.adaptor.AdaptorFunctors;
import net.sf.jga.fn.adaptor.ChainUnary;
import net.sf.jga.fn.property.GetProperty;
import net.sf.jga.fn.string.Match;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
/**
 * Static methods to help create commonly used filters.
 *
 * @author bdc34
 *
 */
public class VitroFilterUtils {
    protected static UnaryFunctor t = AdaptorFunctors.constantUnary(Boolean.TRUE);
    
    private static final Log log = LogFactory.getLog(VitroFilterUtils.class.getName());

    /**
     * Gets a filter that filters out any resource
     * that has a annotation of hiddenFromDisplayBelowRoleLevel higher than current user's role level
     */
    public static VitroFilters getDisplayFilterByRoleLevel(RoleLevel role, WebappDaoFactory wdf){        
        return new HiddenFromDisplayBelowRoleLevelFilter(role, wdf);
    }     

    /** Gets a VitroFilters that permits all objects */
    protected static VitroFiltersImpl getNoOpFilter(){
        return new VitroFiltersImpl();
    }

    /** Filter that only returns individuals with labels that start with the letter 'g' */
    public static VitroFilters getTestFilter(){
        UnaryFunctor<Individual,String> getName = new GetProperty<Individual,String>(Individual.class,"getName");
        UnaryFunctor<String,Boolean> startsWithG = new Match("^[gG].*");
        UnaryFunctor<Individual,Boolean> onlyNamesThatStartWithG =
                new ChainUnary<Individual,String,Boolean>(startsWithG,getName);
        return getNoOpFilter().setIndividualFilter(onlyNamesThatStartWithG);
    }  

    public static UnaryFunctor<Individual,String> FirstLetterOfIndividuals(){
        return new UnaryFunctor<Individual,String>(){
            public String fn(Individual arg){
                return arg.getName().substring(0,1);
            }
            public String toString(){ return "filter: FirstLetterOfIndividuals"; }
        };
    }

    public static class EntitySortTransform extends UnaryFunctor<List<Individual>,List<Individual>>{
        private final String fieldName;
        private final boolean ascending;
        private final Comparator<? super Individual> comparator;

        public EntitySortTransform( String fieldName, boolean ascending){
            this.fieldName = fieldName;
            this.ascending = ascending;
            if ( "timekey".equalsIgnoreCase( fieldName )){
                if( ascending )
                    comparator = timekeyComp;
                else
                    comparator = timekeyCompDesc;

            } else if ( "sunset".equalsIgnoreCase(  fieldName )){
                if( ascending )
                    comparator = sunsetComp;
                else
                    comparator = sunsetCompDesc;

            } else if ( "sunrise".equalsIgnoreCase( fieldName )){
                if( ascending )
                    comparator = sunriseComp;
                else
                    comparator = sunriseCompDesc;
            } else {
                if( ascending )
                    comparator = nameComp;
                else
                    comparator = nameCompDesc;
            }
        }

        public List<Individual> fn(List<Individual> individuals) {
            Collections.sort(individuals,comparator);
            return individuals;
        }

        private static Comparator<? super Individual> timekeyComp =
                new Comparator<Individual>(){
                    public int compare(Individual o1, Individual o2) {
                        Date time1 = o1.getTimekey();
                        Date time2 = o2.getTimekey();
                        if( time1 == null && time2 == null )
                            return 0;
                        if( time1 == null )
                            return 1;
                        if( time2 == null)
                            return -1;
                        return time1.compareTo(time2);
                    }
                    public String toString(){ return "timekeyComp"; }
                };
        private static Comparator<? super Individual> sunsetComp =
                new Comparator<Individual>(){
                    public int compare(Individual o1, Individual o2) {
                        Date time1 = o1.getSunset();
                        Date time2 = o2.getSunset();
                        if( time1 == null && time2 == null )
                            return 0;
                        if( time1 == null )
                            return 1;
                        if( time2 == null)
                            return -1;
                        return time1.compareTo(time2);
                    }
                    public String toString(){ return "sunsetComp"; }
                };
        private static Comparator<? super Individual> sunriseComp =
                new Comparator<Individual>(){
                    public int compare(Individual o1, Individual o2) {
                        Date time1 = o1.getSunrise();
                        Date time2 = o2.getSunrise();
                        if( time1 == null && time2 == null )
                            return 0;
                        if( time1 == null )
                            return 1;
                        if( time2 == null)
                            return -1;
                        return time1.compareTo(time2);
                    }
                    public String toString(){ return "sunriseComp"; }
                };
        private static Comparator<? super Individual> nameComp =
                new Comparator<Individual>(){
            // return ((Individual)o1).getName().compareTo(((Individual)o2).getName());

                    public int compare(Individual o1, Individual o2) {
                        String name1 = o1.getName();
                        String name2 = o2.getName();
                        if( name1 == null && name2 == null )
                            return 0;
                        if( name1 == null )
                            return 1;
                        if( name2 == null)
                            return -1;
                        Collator collator = Collator.getInstance();
                        return collator.compare(name1,name2);
                        //return name1.compareTo(name2);
                    }
                    public String toString(){ return "nameComp"; }
                };

               private static Comparator<? super Individual> timekeyCompDesc =
                new Comparator<Individual>(){
                    public int compare(Individual o1, Individual o2) {
                        Date time1 = o1.getTimekey();
                        Date time2 = o2.getTimekey();
                        if( time1 == null && time2 == null )
                            return 0;
                        if( time1 == null )
                            return -1;
                        if( time2 == null)
                            return 1;
                        return time2.compareTo(time1);
                    }
                    public String toString(){ return "timkeyCompDesc"; }
                };
        private static Comparator<? super Individual> sunsetCompDesc =
                new Comparator<Individual>(){
                    public int compare(Individual o1, Individual o2) {
                        Date time1 = o1.getSunset();
                        Date time2 = o2.getSunset();
                        if( time1 == null && time2 == null )
                            return 0;
                        if( time1 == null )
                            return -1;
                        if( time2 == null)
                            return 1;
                        return time2.compareTo(time1);
                    }
                    public String toString(){ return "sunsetCompDesc"; }
                };
        private static Comparator<? super Individual> sunriseCompDesc =
                new Comparator<Individual>(){
                    public int compare(Individual o1, Individual o2) {
                        Date time1 = o1.getSunrise();
                        Date time2 = o2.getSunrise();
                        if( time1 == null && time2 == null )
                            return 0;
                        if( time1 == null )
                            return -1;
                        if( time2 == null)
                            return 1;
                        return time2.compareTo(time1);
                    }
                    public String toString(){ return "sunriseCompDesc"; }
                };
        private static Comparator<? super Individual> nameCompDesc =
                new Comparator<Individual>(){
                    public int compare(Individual o1, Individual o2) {
                        String name1 = o1.getName();
                        String name2 = o2.getName();
                        if( name1 == null && name2 == null )
                            return 0;
                        if( name1 == null )
                            return -1;
                        if( name2 == null)
                            return 1;
                        Collator collator = Collator.getInstance();
                        return collator.compare(name1,name2);
                        //return name2.compareTo(name1);
                    }
                    public String toString(){ return "nameCompDesc"; }
                };

    }   

}

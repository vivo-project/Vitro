package edu.cornell.mannlib.vitro.webapp.dao.filtering.filters;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import net.sf.jga.algorithms.Sort;
import net.sf.jga.algorithms.Transform;
import net.sf.jga.algorithms.Unique;
import net.sf.jga.fn.UnaryFunctor;
import net.sf.jga.fn.adaptor.AdaptorFunctors;
import net.sf.jga.fn.logical.LogicalFunctors;

import org.joda.time.DateTime;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.utils.FlagMathUtils;

/**
 * Static methods to help create commonly used filters.
 *
 * User: bdc34
 * Date: Oct 19, 2007
 * Time: 11:56:18 AM
 */
public class FiltersForTabs {
    public static final UnaryFunctor<Individual,String> firstLetterOfName = new FirstLetterOfEnt();
    public static UnaryFunctor<Individual,Boolean> getFilterForTab( final Tab tab, final Portal portalThatTabIsIn ){

        DateTime now = new DateTime();
        UnaryFunctor<Individual,Boolean> entFilter = getTimeFilter(tab, now);
        UnaryFunctor<Individual,Boolean> tabPortalFilter = getPortalFilter(tab);
        
        if( tabPortalFilter != null   &&
            portalThatTabIsIn != null && 
            portalThatTabIsIn.isFlag1Filtering() )
                entFilter = AdaptorFunctors.and( entFilter, tabPortalFilter );

        String flag2set = tab.getFlag2Set();
        if( flag2set != null && flag2set.trim().length() > 0 ){
            String[] flags = flag2set.split(",");
            if( "OMIT".equalsIgnoreCase(tab.getFlag2Mode()) ){
                UnaryFunctor<Individual,Boolean> negate = LogicalFunctors.unaryNegate( getCollegeFilter(flags));
                entFilter = AdaptorFunctors.and( entFilter, negate);
            }   else {
                entFilter = AdaptorFunctors.and( entFilter, getCollegeFilter(flags));
            }

        }

        /* need more?
        entFilter = AdaptorFunctors.and( getSomeFilter(tab), entFilter);
        entFilter = AdaptorFunctors.and( getOtherFilter(tab), entFilter);
         */
        return entFilter;
    }

    /**
     * Create a filter for the entities based on the specifications of the tab.
     */
    public static UnaryFunctor<Individual,Boolean> getTimeFilter(final Tab tab, final DateTime now){
        UnaryFunctor<Individual,Boolean> out = null;

        if( tab.getDayLimit() == 0){
          out = VitroFilterUtils.getSunsetWindowFilter( now.toDate() ).getIndividualFilter();

        } else if( tab.getDayLimit() > 0 ) {
            out = new UnaryFunctor<Individual,Boolean>(){
                public Boolean fn(Individual arg){
                    if( arg.getTimekey() == null ) return Boolean.FALSE;

                    DateTime timekey = new DateTime(arg.getTimekey());
                    DateTime startShowingDate = timekey.minusDays( tab.getDayLimit() );

                    /* This is the filtering for events in the future */
                    return now.isAfter( startShowingDate ) && now.isBefore( timekey );
                }
                public String toString(){ return "DayLimit Filter: is timekey after now (" + now.toString()
                        + ") and timekey + daylimit is before now?";
                }
            };

        } else if ( tab.getDayLimit() < 0){
            out = new UnaryFunctor<Individual,Boolean>(){
                public Boolean fn(Individual arg){
                    if( arg.getSunrise() == null ) return Boolean.FALSE;

                    DateTime sunrise = new DateTime(arg.getSunrise());
                    DateTime stopShowingDate = sunrise.plusDays( Math.abs( tab.getDayLimit() ) );

                    /* This is the filtering for press releases */
                    return ( sunrise.isBefore( now ) || sunrise.isEqual( now ) )
                            && now.isBefore( stopShowingDate );
                }
                public String toString(){ return "Sunrise Filter: is sunrise before now and" +
                        " days between now and sunrise greater than or eq to daylimit?";
                }
            };
        }

        return out;
    }

    /**
     * Filter the entities based on the specifications of the tab.
     */
    protected static UnaryFunctor<Individual,Boolean> getPortalFilter(Tab tab){
        ApplicationBean appBean = new ApplicationBean();
        if( tab.getPortalId()  == appBean.getSharedPortalFlagNumeric()){
            return VitroFilterUtils.getCalsPortalFilter().getIndividualFilter();
        //} else if (tab.getPortalId() == appBean.getAllPortalFlagNumeric()){
         } else if (tab.getPortalId() == 65535){
            return VitroFilterUtils.t;
        }else {
            return VitroFilterUtils.getFlag1ExclusiveFilter( FlagMathUtils.portalId2Numeric( tab.getPortalId() ) );
        }
    }

    public static UnaryFunctor<Individual,Boolean>getCollegeFilter(final String[] flags){

        return new UnaryFunctor<Individual,Boolean> (){

            final String [] accetableColleges = flags;

            public Boolean fn(Individual individual) {
                String flags =  individual.getFlag2Set() ;
                if( flags == null || flags.trim().length() ==0 )
                    return Boolean.FALSE;

                String[] collegesForInd = flags.split(",");
                for( String accetable : accetableColleges){
                    for( String forInd : collegesForInd){
                        if( accetable.equalsIgnoreCase(forInd)) {
                            return Boolean.TRUE;
                        }
                    }
                }
                return Boolean.FALSE;
            }
        };
    }


    /**
     * Create a filter to pass only entities with labels that start with the given letter.
     */
    public static UnaryFunctor<Individual,Boolean> getAlphaFilter(final String letter){
        return new UnaryFunctor<Individual,Boolean>(){
            public Boolean fn(Individual individual) {
                return new Boolean( letter.equalsIgnoreCase( individual.getName() ) );
            }
            public String toString(){ return "AlphaFilter on '" + letter
                    + "' for individual.name";}
        };
    }
//
//    /**
//     * Sorts the entities in the way the tab specifies.
//     * @param tab
//     * @return
//     */
//    public static UnaryFunctor<List<Individual>, List<Individual>>
//        getTabOrderTransform(Tab tab){
//        boolean desc = "desc".equalsIgnoreCase( tab.getEntitySortDirection() );
//        return new VitroFilterFactory.EntitySortTransform( tab.getEntitySortField(),!desc);
//    }

    /**
     * Gets a transform that will return take a list of entities and return a
     * list of first letters.
     * @param tab
     * @return
     */
    public static UnaryFunctor<Collection<Individual>,Collection<String>>
        getLetersOfEntsTransform(){
        return new UnaryFunctor<Collection<Individual>,Collection<String>>(){
            public Collection<String> fn(Collection<Individual> individuals) {
                Iterable<String>i =
                        Sort.sort(
                                Unique.unique(
                                        Transform.transform( individuals,
                                                VitroFilterUtils.FirstLetterOfIndividuals() )
                                )
                        );

                ArrayList<String> out = new ArrayList<String>(26);
                for( String str : i){
                    out.add(str);
                }
                return out;
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static List getLettersOfEnts(List<Individual> ents) {
        Comparator<String> comp = new Comparator<String>(){
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            };
        };
        Iterable<String>i =
                Unique.unique(
                        Sort.sort(
                                Transform.transform( ents ,firstLetterOfName ),
                                comp
                        )
                );

        ArrayList<String> out = new ArrayList<String>(26);
        for( String str : i){
            out.add(str);
        }
        return out;
     }

    @SuppressWarnings("serial")
    class FirstLetterFilter extends UnaryFunctor<Individual,Boolean>{
        String firstLetter;
        public FirstLetterFilter(String alpha){
           firstLetter = alpha;
        }
        @Override
        public Boolean fn(Individual arg){
            if( arg.getName() == null )
                return Boolean.FALSE;
            else
                return new Boolean( firstLetter.equalsIgnoreCase( arg.getName().substring(0,1) ) );
        }
    }

    @SuppressWarnings("serial")
    private static class FirstLetterOfEnt extends UnaryFunctor<Individual,String>{
        @Override
        public String fn(Individual arg) {
            if( arg != null && arg.getName() != null && arg.getName().length() > 0 ){
                return arg.getName().substring(0,1).toUpperCase();
            } else {
                return "?";
            }
        }
    }
}

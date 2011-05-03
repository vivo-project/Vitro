/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import net.sf.jga.algorithms.Sort;
import net.sf.jga.algorithms.Transform;
import net.sf.jga.algorithms.Unique;
import net.sf.jga.fn.UnaryFunctor;

import org.joda.time.DateTime;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;

/**
 * Static methods to help create commonly used filters.
 *
 * User: bdc34
 * Date: Oct 19, 2007
 * Time: 11:56:18 AM
 */
public class FiltersForTabs {
    public static final UnaryFunctor<Individual,String> firstLetterOfName = new FirstLetterOfEnt();
    public static UnaryFunctor<Individual,Boolean> getFilterForTab( final Tab tab, final boolean isFlag1Filtering ){

        DateTime now = new DateTime();
        UnaryFunctor<Individual,Boolean> entFilter = getTimeFilter(tab, now);

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
          out = getSunsetWindowFilter( now.toDate() ).getIndividualFilter();

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
    
    /** this filter accepts only objects which have sunset dates of the given date or
     * earlier and sunset dates after the given date.  sunrise <= givenDate < sunset.
     * 
     * It is no longer in general use.  It is only used by FitlersForTabs.
     * @param givenDate - if null, use current date.
     * */
    @SuppressWarnings("unchecked")
    public static VitroFilters getSunsetWindowFilter(final  Date givenDate ){              
        
        UnaryFunctor<Individual,Boolean> fn = 
            new UnaryFunctor<Individual, Boolean>(){
            Date given = givenDate;            
            //@Override
            public Boolean fn(Individual arg) {
                if( arg == null) return true;                
                return checkSunriseSunset(givenDate, arg.getSunrise(), arg.getSunset());
            }
            public String toString(){ return "Individual time window filter " + given; }; 
        };
        
        UnaryFunctor<ObjectPropertyStatement,Boolean> objPropfn = 
            new UnaryFunctor<ObjectPropertyStatement, Boolean>(){
            Date given = givenDate;
            
            //@Override
            public Boolean fn(ObjectPropertyStatement arg) {                
                if( arg == null) return true;
                if( checkSunriseSunset(givenDate, arg.getSunrise(), arg.getSunset()) == false)
                    return false;
                
                if( arg.getObject() != null ) {                    
                    Individual obj = arg.getObject();
                    if( checkSunriseSunset(givenDate, obj.getSunrise(), obj.getSunset()) == false)
                        return false;
                }
                if( arg.getSubject() != null ){
                    Individual sub = arg.getSubject();                 
                    if( checkSunriseSunset(givenDate, sub.getSunrise(), sub.getSunset()) == false )
                        return false;   
                }
                return true;
            }
            public String toString(){ return "ObjectPropertyStatement time window filter " + given; };  
        };

        //we need these casts because of javac
        VitroFiltersImpl vfilter = VitroFilterUtils.getNoOpFilter();
        vfilter.setIndividualFilter( fn );                      
        vfilter.setObjectPropertyStatementFilter(objPropfn);
        return vfilter;
    }


    private static boolean checkSunriseSunset( Date now, Date sunrise, Date sunset){
        if( sunrise == null && sunset == null ) 
            return true;
        
        DateTime nowDt = (now!=null?new DateTime(now):new DateTime());        
        DateTime sunriseDt = (sunrise != null ? new DateTime(sunrise): nowDt.minusDays(356));
        DateTime sunsetDt = (sunset   != null ? new DateTime(sunset) : nowDt.plusDays(356));
        
        if( ( nowDt.isBefore( sunsetDt ) )
                &&
            ( nowDt.isAfter( sunriseDt ) 
              || nowDt.toDateMidnight().isEqual( sunriseDt.toDateMidnight()))
           )   
            return true;
        else
            return false;        
    }    
}

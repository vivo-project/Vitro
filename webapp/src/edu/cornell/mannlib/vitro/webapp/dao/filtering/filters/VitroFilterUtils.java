package edu.cornell.mannlib.vitro.webapp.dao.filtering.filters;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.ArrayList;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import net.sf.jga.fn.UnaryFunctor;
import net.sf.jga.fn.adaptor.AdaptorFunctors;
import net.sf.jga.fn.adaptor.ChainUnary;
import net.sf.jga.fn.logical.All;
import net.sf.jga.fn.logical.UnaryNegate;
import net.sf.jga.fn.property.GetProperty;
import net.sf.jga.fn.string.Match;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.flags.PortalFlag;
import edu.cornell.mannlib.vitro.webapp.utils.FlagMathUtils;
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
        log.warn("initializing HiddenFromDisplayBelowRoleLevelFilter");
        return new HiddenFromDisplayBelowRoleLevelFilter(role, wdf);
    }
    
    /* bdc34: Currently, this is not called from anywhere in the code. */
    public static VitroFilters getUpdateFilterByRoleLevel(RoleLevel role, WebappDaoFactory wdf){
        log.warn("initializing ProhibitedFromUpdateBelowRoleLevelFilter");
        return new ProhibitedFromUpdateBelowRoleLevelFilter(role, wdf);
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
        VitroFiltersImpl vfilter = getNoOpFilter();
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

    /**
     * Gets a set of VitroFilters for a given portal flag.
     * This method may return null. */
    public static VitroFilters getFilterFromPortalFlag(PortalFlag flag){
        if( ! flag.isFilteringActive() )
            return getNoOpFilter();


        ApplicationBean appBean = new ApplicationBean();
        if( flag.getFlag1Numeric()  == appBean.getSharedPortalFlagNumeric()){
            return VitroFilterUtils.getCalsPortalFilter();
        } else if (flag.getFlag1Numeric() == appBean.getAllPortalFlagNumeric() ){            
                return VitroFilterUtils.getAllPortalFilter(); 
        }

        //common, single portal configuration with only flag1 active
        if(PortalFlag.SHOW_ALL_PORTALS != flag.getFlag1DisplayStatus() &&
           flag.flag1Active &&
           ! flag.flag2Active && ! flag.flag3Active)
        {
            return new VitroFiltersImpl().setIndividualFilter(
                    getFlag1ExclusiveFilter( flag.getFlag1Numeric() ));            
        }


        ArrayList<UnaryFunctor<Individual,Boolean>> functors =
            new ArrayList<UnaryFunctor<Individual,Boolean>> (3);

        UnaryFunctor<Individual,Boolean> flag1Fn = null;
        if( PortalFlag.SHOW_ALL_PORTALS == flag.getFlag1DisplayStatus() ){
            functors.add( t );
        }else if( flag.flag1Active && flag.getFlag1Numeric() > 0 ){
            if( flag.isFlag1Exclusive() )
                flag1Fn =getFlag1ExclusiveFilter( flag.getFlag1Numeric() );
            else
                flag1Fn = getFlag1OmitFilter( flag.getFlag1Numeric() );
            functors.add( flag1Fn );
        }

        UnaryFunctor<Individual,Boolean> flag2Fn = null;
        if( flag.flag2Active && flag.getFlag2Numeric() > 0 ){
            if( flag.isFlag2Exclusive() )
                flag2Fn =getFlag2ExclusiveFilter( flag.getFlag2Numeric() );
            else
                flag2Fn = getFlag2OmitFilter( flag.getFlag2Numeric() );
            functors.add( flag2Fn );
        }

        UnaryFunctor<Individual,Boolean> flag3Fn = null;
        if( flag.flag3Active && flag.getFlag3Numeric() > 0 ){
            if( flag.isFlag2Exclusive() )
                flag3Fn =getFlag3ExclusiveFilter( flag.getFlag3Numeric() );
            else
                flag3Fn = getFlag3OmitFilter( flag.getFlag3Numeric() );
            functors.add( flag3Fn );
        }

        VitroFiltersImpl vf = new VitroFiltersImpl();                   
        if( functors.size() == 1 )
            vf.setIndividualFilter(functors.get(0));
        else if( functors.size() > 0 ){
            //this is a filter where all of the conditions in the collection are true
            UnaryFunctor<Individual,Boolean> indFilter
                = new All<Individual>( functors );
            vf.setIndividualFilter(indFilter);
        }

        return vf;
    }

    /**
     * All portal no longer does any portal flag based 
     * filtering.  If a Individual needs to be hidden it
     * could be hidden using the hiddenFromDisplayBelowRoleLevel setting,
     * although this would re-introduce the performance hit of a-box filtering .
     * @return returns null
     */
    public static VitroFilters getAllPortalFilter(){
//        VitroFiltersImpl vf = new VitroFiltersImpl();
//        vf.setIndividualFilter(ALL_PORTAL_FILTER);
//        return vf;
        return null;  
    }

    private static UnaryFunctor<Individual,Boolean> ALL_PORTAL_FILTER =
            new UnaryFunctor<Individual,Boolean>(){
              public Boolean fn(Individual ind){
                if (ind == null ) return false;
                return ind.getFlag1Numeric() != 0 ;
              }
              public String toString(){ return "allow any non-zero flag1 Individuals";}
            };

    /**
     * get a filter that will only pass entities that are checked into
     * the portal with the given id.  All non-Individual objects will be
     * passed.
     *
     * @return
     */
    @SuppressWarnings("serial")
        public static UnaryFunctor<Individual,Boolean> getFlag1ExclusiveFilter(final long portalIdNumeric) {
        return
            new UnaryFunctor<Individual,Boolean>(){
                int portalMask = (int)portalIdNumeric;

                @Override
                public Boolean fn(Individual arg) {
                    if( arg == null ) return Boolean.FALSE;
                    return  arg.doesFlag1Match( portalMask );
                }
                public String toString(){ return "Flag1Exclusive Filter on " +
                        "individual.portal & " + portalMask;
                }
            };
    }

    protected static UnaryFunctor<Individual, Boolean> getFlag1OmitFilter(
            int flag1Numeric) {
        return new UnaryNegate<Individual>( getFlag1ExclusiveFilter( flag1Numeric ));
    }

    @SuppressWarnings("serial")
    protected static UnaryFunctor<Individual,Boolean> getFlag3ExclusiveFilter(long flag3Numeric) {
        final long _numeric = flag3Numeric;
        return
            new UnaryFunctor<Individual,Boolean>(){
                long mask = _numeric ;

                @Override
                public Boolean fn(Individual arg) {
                    if( arg == null ) return Boolean.FALSE;

                    int entFlag3 = arg.getFlag3Numeric();
                    boolean isEntInPortal = (mask & entFlag3) != 0;
                    return new Boolean( isEntInPortal );
                }
            };
    }

    protected static UnaryFunctor<Individual, Boolean> getFlag3OmitFilter(
            int flag3Numeric) {
        return new UnaryNegate<Individual>( getFlag3ExclusiveFilter( flag3Numeric ));
    }

    @SuppressWarnings("serial")
    protected static UnaryFunctor<Individual,Boolean> getFlag2ExclusiveFilter(long flag2Numeric) {
        final long _numeric = flag2Numeric;
        return
            new UnaryFunctor<Individual,Boolean>(){
                long mask = _numeric;

                @Override
                public Boolean fn(Individual arg) {
                    if( arg == null ) return Boolean.FALSE;
                    int entflag2 = ((Individual)arg).getFlag2Numeric();
                    boolean isEntInPortal = (mask & entflag2) != 0;
                    return new Boolean( isEntInPortal );
                }
            };
    }

    protected static UnaryFunctor<Individual, Boolean> getFlag2OmitFilter(
            int flag2Numeric) {
        return new UnaryNegate<Individual>( getFlag2ExclusiveFilter( flag2Numeric ));
    }

    /**
     * Get a filter that will emulate the 'all portal' behavior.
     * Notice that the 'all portal' does not have all of the
     * entities that exist in the system.  This fillter will
     * pass anything checked into portals 2,3,4,5 aka
     * life sci, env sci, lang grant, applied soc sci.
     *
     * That's the bit mask 001111
     *
     * All non-Individual objects will be passed.
     * @return
     */
    public static VitroFilters getCalsPortalFilter() {
        final long portalIdNumeric =
            FlagMathUtils.portalId2Numeric( 2 ) +
            FlagMathUtils.portalId2Numeric( 3 ) +
            FlagMathUtils.portalId2Numeric( 4 ) +
            FlagMathUtils.portalId2Numeric( 5 );

        UnaryFunctor<Individual,Boolean> indFn
            = new UnaryFunctor<Individual,Boolean>(){
                long portalMask = portalIdNumeric;

                @Override
                public Boolean fn(Individual arg) {
                    if( arg == null ) return Boolean.FALSE;
                    int entPortalNum = ((Individual)arg).getFlag1Numeric();
                    boolean isEntInPortal = (portalMask & entPortalNum) != 0;
                    return new Boolean( isEntInPortal );
                }

                public String toString(){ return "CalsPortal Filter, is individual in " +
                         "portal 2,3,4 or 5?";
                 }
            };

        VitroFiltersImpl vf = new VitroFiltersImpl();
        vf.setIndividualFilter(indFn);
        return vf;
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

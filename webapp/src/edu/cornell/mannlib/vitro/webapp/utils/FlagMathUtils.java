package edu.cornell.mannlib.vitro.webapp.utils;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.Collection;
import java.util.LinkedList;

    /**
      * Brian Caruso's flag notes -
      * Where in he makes an attempt to keep sane about flags.
      * 2006-06-07
      * There are currently three 'flags' on the entities.flag1set, flag2set and flag3set.
      *
      *
      *  they are defined in mysql as:
   `flag2Set` set('AAP','A&S','CALS','Engineering','Hotel','Human Ecology','ILR','Johnson','Law','Vet','Weill Medical','not Cornell') default NULL,
   `flag1Set` set('0','1','2','3','4','5','6','7','8','9','10','11') default NULL,
   `flag3Set` set('Geneva','Ithaca','New York City','Qatar','Cornell off campus') default NULL,
      *
      * Notice that in the SQL DDS you use 'flag','flag2' and in DMS you use 'flag,flag2'
      *
      * So in mysql you can request these are 'numeric' or as string values:
      * a) select flag2set from entities where id=666 ->       'AAP,A&S,CALS'
      * b) select flag2set+0 from entities where id=666 -> 7 =  1  +2  +4
      *
      * the string in a) gets called the 'flag values' for 'flag names'
      *  and the int in b) gets called the 'numeric value'
      *
      * But notice that flag1 has numbers as the flag names.
      * So '2,3,4' = 2+8+16 = 26 = 11010 = 'life sci','env sci','land grant'
      *
      * If you have an integer which represents a single flag and want to know which bit positon
      * it is you want to know how many times it is divisable by 2:
      *
      * number in question  4096 = x
      * 2 raised to the y = x what is y?
      * y = log2(x)
      *
      * Oh java Math doesn't have logx(y) great your math teacher would suggest:
      * ln(x)/ln(2) = y  or log10(x)/log10(2) = y
      * But in java: Math.log(x)/Math.log(2) is double float, which is slow.
      *
      * So try this:
      * 10000000000 = 00000000000000000000010000000000 (that's 32 bits)
      * 31 - numberOfLeadingZeros(x) = y
      *
      * java 1.5 now has a bunch of bit operations in the class Integer.
      * Also check out BitSet.
      *
      */
public class FlagMathUtils {
    /**
     * Converts a vector of booleans to a numeric representation.
     *
     * @param in
     * @return numeric representation of flag
     */
    public static int bits2Numeric (boolean[] in ){
        int numeric=0;
        for( int i=0; i< in.length; i++){
            if( in[i] )
                numeric += 1 << i;
        }
        return numeric;
    }

    /**
     * Converts a numeric flag to an array of Long numeric flags
     * 7 -> { 1, 2 , 4 }
     */
    public static Long[] numeric2numerics(int in){
        Collection<Long> results = new LinkedList<Long>();
        String base2 = Integer.toBinaryString(in);
        //System.out.println(base2);
        int flagsAdded = 0;
        for( int i = 0; i < base2.length() ; i++){
            //notice that we work at the back of the string here
            if( base2.charAt(base2.length()-1-i) == '1' ){
                results.add(  Math.round(Math.pow(2, i))) ;
                flagsAdded++;
            }
        }
        return (Long[])results.toArray(new Long[flagsAdded] );
    }

    /**
     * converts an single numeric 2power portal id to the portal id.
     * ex: 32 -> 5
     * Basicly floor(log2(in))
     * Notice that java 1.5 Integer has a bunch of bit operations.
     *
     * Notice this function will return meaningless results if your
     * int is not a power of 2.  You have been warned.  This will throw
     * errors if you do something silly.
     *
     * This works up to about pow(2,Long.SIZE-1), that's 63 bits.
     *
     * Look at PortalFLagTest for an example.
     * @param in
     * @return
     */
    public static int numeric2Portalid(long in ){
        if( Long.bitCount(in) > 1 ) throw new Error("numeric2PortalId is intended to " +
                "convert a int with a single portal flag set to the portal id. Your input " +
                "has more than one bit set. in: " + Long.toBinaryString(in));
        if( Long.bitCount(in)==0 ) throw new Error("numeric2PortalId is intended to " +
                "convert a int with a single portal flag set to the portal id. Your input " +
                "has no bits set. ");

        //java Maht doesn't have logx(y), yah go java.
        //this would work: Math.log10(x)/Math.log(2)  as double floats
        //that could be slugish.
        //lets try this:
        return Long.SIZE - Long.numberOfLeadingZeros(in) - 1;
    }

    /**
     * converts from a portalid such as the one in 'localhost:8080/vivo?home=1'
     * to the mysql numeric. Basicly pow(2,portalId).
     */
    public static long portalId2Numeric(long n){
        /*
         * When tested these two take about the same time but it
         * seems that the bit shift should be faster.
         */
        return 1L << n; // stick a one in the ones place and shift over n places.
        //return  (long)Math.pow(2.0, n);
    }
}

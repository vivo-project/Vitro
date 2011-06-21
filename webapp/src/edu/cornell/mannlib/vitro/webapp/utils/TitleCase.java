/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import java.util.StringTokenizer;

/**
 *
 *
following text from http://search.cpan.org/dist/Text-Capitalize/Capitalize.pm

This web page:
  http://www.continentallocating.com/World.Literature/General2/LiteraryTitles2.htm

presents some admirably clear rules for capitalizing titles:
  ALL words in EVERY title are capitalized except
  (1) a, an, and the,
  (2) two and three letter conjunctions (and, or, nor, for, but, so, yet),
  (3) prepositions.
  Exceptions:  The first word is always capitalized even
  if it is among the above three groups. For the last word, we can specify the
  option to always capitalize it or to treat it like a medial word. The original
  method always capitalized the final word, so to support the old method calls
  we have defined another method to provide this as default behavior.
  

But consider the case:
  "It Waits Underneath the Sea"

Should the word "underneath" be downcased because it's a preposition? Most English speakers
would be surprised to see it that way. Consequently, the default list of exceptions to
capitalization in this module only includes the shortest of the common prepositions
(to of by at for but in).

The default entries on the exception list are:
     a an the
     and or nor for but so yet
     to of by at for but in with has
     de von
The observant may note that the last row is not composed of English words. The honorary
"de" has been included in honor of "Honore' de Balzac". And "von" was added for the sake
of equal time.
 */
public class TitleCase {
    static String ignore[] = {"a","an","the","and","or","nor","for","but","so","yet",
            "to","of","by","at","for","but","in","with","has","de","von"};
        
    public static String toTitleCase(String in) {
        // Support old behavior without modifying method calls
        return toTitleCase(in, true);
    }

    public static String toTitleCase(String in, boolean alwaysCapitalizeLast){
        if( in == null || in.length() ==0 )
            return in;

        in = in.toLowerCase();
        StringTokenizer st = new StringTokenizer(in);
        StringBuilder out = new StringBuilder();

        int count = 1;
        int last = st.countTokens();

        while(st.hasMoreTokens()){
            String token = st.nextToken();

            // always capitalize first 
            if ( count == 1 || 
                    // always capitalize last, unless we've asked not to
                    ( alwaysCapitalizeLast && count == last ) ) {
                out.append(capitalizeWord(token));

            } else {

                //check if on ignored list
                boolean ignoreToken = false;
                for(String ign:ignore){
                    if( token.equals(ign) )
                        ignoreToken = true;
                }
                if( ignoreToken )
                    out.append(token);
                else
                    out.append(capitalizeWord(token));
            }

            if(st.hasMoreTokens())
                out.append(' ');
            count++;
        }
        return out.toString();
    }

    private static String capitalizeWord(String in){
        if( in == null || in.length() == 0 )
            return in;
        if( in.length() == 1 )
            return in.toUpperCase();

        //not trying too hard to deal with dashes.
        int dash = in.indexOf('-') ;
        if(dash > 0 && in.length() > dash+2 )
            in = in.substring(0, dash)
                + '-'
                + in.substring(dash+1,dash+2).toUpperCase()
                + in.substring(dash+2);

        return in.substring(0, 1).toUpperCase() + in.substring(1);
    }
}

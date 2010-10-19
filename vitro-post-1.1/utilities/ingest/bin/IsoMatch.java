



public class IsoMatch{
    public static boolean iso(String fn1, String mn1, String ln1,
			      String fn2, String mn2, String ln2){
	if(fn1 == null){
	    fn1 = "";
	}
	if(fn2 == null){
	    fn2 = "";
	}
	if(mn1 == null){
	    mn1 = "";
	}
	if(mn2 == null){
	    mn2 = "";
	}
	if(ln1 == null){
	    ln1 = "";
	}
	if(ln2 == null){
	    ln2 = "";
	}
	if(!ln1.equals(ln2)){
	    return false;
	}
	if((clean(fn1)+"|"+clean(mn1)).equals(clean(fn2)+"|"+clean(mn2))){
	    return true;
	}
	double fnw = weigh(fn1.toUpperCase(),fn2.toUpperCase());
	double mnw = weigh(mn1.toUpperCase(),mn2.toUpperCase());
	if((2.0*fnw + mnw)/3.0 < .5){
	    return false;
	}
	return true;
    }
    public static double weigh(String p1, String p2){
	if(p1 == null){
	    p1 = "";
	}
	if(p2 == null){
	    p2 = "";
	}
	int len1 = p1.length();
	int len2 = p2.length();
	if(len1>=1 && len2>=1){
	    if(len1>1 && len2>1){
		if(p1.equals(p2)){
		    return 1.0; // full vs full match
		} else {
		    return -1.0; // full vs full mismatch
		}
	    } else {
		if(p1.substring(0,1).equals(p2.substring(0,1))){
		    return .5; // partial vs non-empty match
		} else {
		    return -.55; // partial vs non-empty mis-match
		}
	    }
	} else if(len1 > 0 && len2 == 0){
	    return -.75; // Empty vs NonEmpty MisMatch
	} else if(len1 == 0 && len2 > 0){
	    return -.75; // Empty vs NonEmpty MisMatch
	}
	return 1.0; // empty vs empty match
    }
    public static double score(String name1,String name2){
	String[] n1 = name1.split(",");
	String[] n2 = name2.split(",");
	double sc = score(n1[0],n1[1],n1[2],n2[0],n2[1],n2[2]);
	return sc;
    }
    public static double score(String fn1, String mn1, String ln1,
			      String fn2, String mn2, String ln2){

	if(fn1 == null){
	    fn1 = "";
	}
	if(fn2 == null){
	    fn2 = "";
	}
	if(mn1 == null){
	    mn1 = "";
	}
	if(mn2 == null){
	    mn2 = "";
	}
	if(ln1 == null){
	    ln1 = "";
	}
	if(ln2 == null){
	    ln2 = "";
	}
	if(!ln1.equals(ln2)){
	    return -1.0;
	}
	if((clean(fn1)+"|"+clean(mn1)).equals(clean(fn2)+"|"+clean(mn2))){
	    return 1.0;
	}
	double fnw = weigh(fn1,fn2);
	double mnw = weigh(mn1,mn2);
	return (2.0*fnw + mnw)/3.0;
    }
    public static String clean(String s){
	if(s == null)
	    return "";
	s = s.trim();
	s = s.replaceAll("[\\-.,;\"]"," ");
	s = s.replaceAll("\\'"," ");
	s = s.replaceAll("\\s+"," ");
	return s;
    }
    public static void main(String[] args){
	String[] n1 = args[0].split(",");
	String[] n2 = args[1].split(",");
	double sc = score(n1[0],n1[1],n1[2],n2[0],n2[1],n2[2]);
	System.out.println(sc + "\n" );
    }
}

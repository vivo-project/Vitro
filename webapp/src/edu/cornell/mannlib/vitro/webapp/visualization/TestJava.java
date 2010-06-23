package edu.cornell.mannlib.vitro.webapp.visualization;

import java.util.Map;
import java.util.TreeMap;

import com.hp.hpl.jena.iri.IRIFactory;

public class TestJava {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String uriStr = "http%3A%2F%2Fvivo.library.cornell.edu%2Fns%2F0.1%23individual8588";
		IRIFactory factory = IRIFactory.jenaImplementation();
		
		String[] sampleBlurns = {"Gold, J R [Reprint Author, Author]; Perkins, G A; Erb, H N; Ainsworth, D M | Journal of Veterinary Internal Medicine, 2006; 20(3): 720",
								 "Goldstein, Richard E; Lin, Rebecca C; Langston, Catherine E; Scrivani, Peter V; Erb, Hollis N; Barr, Stephen C | Journal of Veterinary Internal Medicine, 2006; 20(3): 489-494",
								 "McClellan, Jennifer M; Goldstein, Richard E; Erb, Hollis N; Dykes, Ned L; Cowgill, Larry D | American Journal of Veterinary Research, 2006; 67(4): 715-722",
								 "Cook, Christopher P; Scott, Danny W; Erb, Hollis N; Miller, William H | Vet Dermatol. 2005; 16(1): 47-51",
								 "Estrada, Amara; Moise, N Sydney; Erb, Hollis N; McDonough, Sean P; Renaud, Farrell, Shari | Journal of Veterinary Internal Medicine, 2006; 20(4): 862-872",
								 "Aguirre, A L [Reprint Author, Author]; Center, S A; Yeager, A E; Randolph, J F; Erb, H N | Journal of Veterinary Internal Medicine, 2006; 20(3): 787-788",
								 "MacLeod, K D; Scott, D W; Erb, H N | Journal of Veterinary Medicine Series A, 2004; 51(9-10): 400-404",
								 "Bailey, Dennis B; Rassnick, Kenneth M; Erb, Hollis N; Dykes, Nathan L; Hoopes, P Jack; Page, Rodney L | American Journal of Veterinary Research, 2004; 65(11): 1502-1507",
								 "Ainsworth, Dorothy M; Wagner, Bettina; Franchini, Marco; Grunig, Gabriele; Erb, Hollis N; Tan, Jean Yin | American Journal of Veterinary Research, 2006; 67(4): 669-677",
								 "Page, Richard B; Scrivani, Peter V; Dykes, Nathan L; Erb, Hollis N; Hobbs, Jeff M | Veterinary Radiology and Ultrasound, 2006; 47(2): 206-211",
								 "Scrivani, Peter V; Dykes, Nathan L; Erb, Hollis N | Veterinary Radiology and Ultrasound, 2004; 45(5): 419-423",
								 "Sepesy, Lisa M; Center, Sharon A; Randolph, John F; Warner, Karen L; Erb, Hollis N | Journal of the American Veterinary Medical Association, 2006; 229(2): 246-252",
								 "Dereszynski, D [Reprint Author, Author]; Center, S; Hadden, A; Randolph, J; Brooks, M; Palyada, K; McDonough, S; Messick, J; Bischoff, K; Erb, H; Gluckman, S; Sanders, S | Journal of Veterinary Internal Medicine, 2006; 20(3): 751-752",
								 "Kroll, Tracy L; Houpt, Katherine A; Erb, Hollis N | Journal of the American Animal Hospital Association, 2004; 40(1): 13-19",
								 "Fortier, Lisa A; Gregg, Abigail J; Erb, Hollis N; Fubini, Susan L | Vet Surg. 33(6): 661-7",
								 "Wagner, Bettina; Flaminio, Julia B F; Hillegas, Julie; Leibold, Wolfgang; Erb, Hollis N; Antczak, Douglas F | Veterinary Immunology and Immunopathology, 2006; 110(3-4): 269-278",
								 "Chalmers, H J; Scrivani, Peter V; Dykes, Nathan L; Erb, Hollis N; Hobbs, J M; Hubble, Lorna J | Veterinary Radiology and Ultrasound, 2006; 47(5): 507-509",
								 "Center, Sharon A; Warner, Karen L; McCabe, Jennifer; Foureman, Polly; Hoffmann, Walter E; Erb, Hollis N | Am J Vet Res. 2005; 66(2): 330-41"};

		System.out.println(sampleBlurns);
		for (String blurb : sampleBlurns) {
			
			System.out.println(blurb.substring(0, 10));
		}
		Map<String, Integer> yearToPublicationCount = new TreeMap<String, Integer>();

		yearToPublicationCount.put("2003", 5);
		yearToPublicationCount.put("2001", 5);
		yearToPublicationCount.put("2002", 5);
		yearToPublicationCount.put("2090", 7);
		yearToPublicationCount.put("2087", 6);
		
		String emptyString = "";
		System.out.println(emptyString.isEmpty());

//		System.out.println(Collections.max(yearToPublicationCount.keySet()));
//		System.out.println(Collections.min(yearToPublicationCount.keySet()));

//		yearToPublicationCount.put("2087", testIt(5, yearToPublicationCount.get("2087")));

//		try {
//			factory.setEncoding("HEX");
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		
		/*
		IRI iri = factory.create(uriStr);



        if (iri.hasViolation(false) ) {
            boolean validURI = false;
            String errorMsg = ((Violation)iri.violations(false).next()).getShortMessage()+" ";
            System.out.println("MURDER!!!!!" + errorMsg);

        } else {
        	System.out.println(iri.hasViolation(true));

        }*/


	}

	private static Integer testIt(int i, Integer integer) {
//		System.out.println("testit - " + i + " --- " + integer);
		
		return 10;
	}

}


############################################
#
# Extract IntellCont, Journal Articles add author index, summarize 
# authorships etc on a per netid basis
#
if($Phases{'IC'}>0 || $g_all){
    print "\nPhase = IC ================================\n";
    $g_curPhase = 'IC';
#
# depends on $g_xmls_raw e,r and $g_xmls_out e,r,w
# $g_xslts/$g_chain and $g_log_path
# xslt/aiic-Filter.xsl, xslt/aiic-addIndex.xsl
# xslt/aiic-addAuthSummary.xsl, xslt/aiic-addIntellContPromulgators.xsl

    

    print "\nBegin integration process using $g_xslts/$g_chain ...\n";
    my $prefix = $g_aiic . "_";
    my $cmd = "";
    $cmd .= "$g_bin/xsltseq -I $g_xmls_raw -O $g_xmls_out ";
    $cmd .= " -X $g_xslts/$g_chain -x $g_xslts -p $prefix ";
    $cmd .= " -T $g_icd -t 'IC' ";
    $cmd .= " -L $g_log_path " if $g_log_path ne '';
    my $r;
    $r = doit($cmd, $g_exef);
    exit(1) if($r);


# depends on $g_xmls_raw e,r and $g_xmls_out e,r,w
# $g_xslts/empty.xml, $g_xslts/aiid2netid.xsl and prior GX, IC

    print "\nConstruct a list of xmls by netids in $g_ic/aiiclist.xml ...\n";
    my $cmd = "";
    $cmd .= "$g_bin/mklist -d $g_xmls_out > $g_ic/aiiclist.xml";
    my $r;
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nConstruct a mapping of activity insight id to";
    print " netid (for cornell authors) ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts -jar $g_xslts/saxon9he.jar $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/aiid2netid.xsl ";
    $cmd .= " listxml=$g_ic/aiiclist.xml > $g_ic/aiid-netid.xml ";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);
}
############################################
#
# Accumulate all articles by author by combining data
# from all xmls
#
if($Phases{'CA'}>0 || $g_all){
    print "\nPhase = CA ================================\n";
    $g_curPhase = 'CA';
#
#depends on $g_xslts/empty.xml, $g_ic/aiiclist.xml, $g_ic/aiid-netid.xml
# $g_xslts/collectByAuthor.xsl, $g_xslts/groupAuthorsArticles.xsl
# $g_xslts/aiicsort.xsl    prior GX, IC 
#
    print "\nCollect all articles for each author from all $g_aiic xmls ...\n";
    my $cmd = "";
    $cmd .= "java $g_javaopts -jar $g_xslts/saxon9he.jar $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/collectByAuthor.xsl listxml=$g_ic/aiiclist.xml ";
    $cmd .= " aiid2netid=$g_ic/aiid-netid.xml > $g_ic/ca.xml ";
    my $r;
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nIdentify articles mentioned in multiple AIIC xmls ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts -jar $g_xslts/saxon9he.jar $g_ic/ca.xml ";
    $cmd .= " $g_xslts/groupAuthorsArticles.xsl > $g_ic/cad.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    $cmd = "";
    print "\nSort an author's articles by Activity Insight id ...\n";
    $cmd .= "java $g_javaopts -jar $g_xslts/saxon9he.jar $g_ic/cad.xml ";
    $cmd .= " $g_xslts/aiicsort.xsl > $g_ic/cads.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);
}


############################################
#
# Accumulate and group all articles by journal by combining data
# from all xmls
#
if($Phases{'CJ'}>0 || $g_all){
    print "\nPhase = CJ ================================\n";
    $g_curPhase = 'CJ';
#
#depends on $g_ic/aiiclist.xml  $g_xslts/empty.xml $g_ic/aiiclist.xml
# $g_xslts/collectByJournals.xsl $g_xslts/groupJournalsArticles.xsl
# $g_xslts/aiicsort.xsl
# prior GX, IC
#
    print "\nCollect all articles for each journal from all $g_aiic xmls ...\n";
    my $cmd = "";
    $cmd .= "java $g_javaopts -jar $g_xslts/saxon9he.jar $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/collectByJournals.xsl listxml=$g_ic/aiiclist.xml ";
    $cmd .= " > $g_ic/cj.xml ";
    my $r;
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nIdentify articles mentioned in multiple AIIC xmls ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts -jar $g_xslts/saxon9he.jar $g_ic/cj.xml ";
    $cmd .= " $g_xslts/groupJournalsArticles.xsl ";
    $cmd .= " > $g_ic/cjd.xml";
    $r = doit($cmd, $g_exef);

    exit(1) if($r);
    
    print "\nSort each journal's articles by Activity Insight id ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts -jar $g_xslts/saxon9he.jar $g_ic/cjd.xml ";
    $cmd .= " $g_xslts/aiicsort.xsl > $g_ic/cjds.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);
}
############################################
#
# Construct rdf for journal article authors
#
if($Phases{'AR'}>0 || $g_all){
    print "\nPhase = AR ================================\n";
    print "\nConstruct a (sufficiently) large number of unique ";
    print "strings for journal article author rdf process\n";
    $g_curPhase = 'AR';
#
# depends on $g_ic/cad.xml, $g_ic/cads.xml, $g_ic/cjds.xml
# $g_ic/unomap.xml $g_xslts/mkrdf.xsl $g_xslts/rdfsort.xsl
# $g_fb/Per0.xml
# prior GX, IC, CA, !!-> CJ <-!!
# 
#
    mkUnoFile("$g_ic/cad.xml","counter","$g_ic/unomap.xml");

    initFeedbackFile('Per','P-AR');

    print "\nConstruct the rdf for authors and articles\n";

    my $cmd = "";
    $cmd .= "java $g_javaopts -jar $g_xslts/saxon9he.jar ";
    $cmd .= " -o $g_rdf/icjabya.rdf $g_ic/cads.xml ";
    $cmd .= " $g_xslts/mkrdf.xsl abyjFile=$g_ic/cjds.xml ";
    $cmd .= " unoMapFile=$g_ic/unomap.xml ";
    $cmd .= " aiicXmlPath=$g_xmls_out aiicPrefix=$g_aiic\_ ";
    $cmd .= " extPerIn=$g_fb/Per0.xml ";
    $cmd .= " extPerOut=$g_fb/Per1.xml ";

    my $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nSort results ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts -jar $g_xslts/saxon9he.jar $g_rdf/icjabya.rdf ";
    $cmd .= " $g_xslts/rdfsort.xsl > $g_rdf/sicjabya.rdf ";

    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    updateFeedbackFile('Per','AR');

}
############################################
#
# Construct rdf for journals
#
if($Phases{'JR'}>0 || $g_all){
    print "\nPhase = JR ================================\n";
    print "\nConstruct a (sufficiently) large number of unique";
    print " strings for journal rdf process\n";
    $g_curPhase = 'JR';
#
#depends on $g_ic/cjd.xml $g_ic/cjds.xml $g_xslts/mkjrdf.xsl
# prior GX, IC, CJ
#
    mkUnoFile("$g_ic/cjd.xml","JOURNAL_NAME","$g_ic/ajnunos.xml");

    print "\nConstruct the rdf for journals and articles\n";

    my $cmd = "";
    $cmd .= "java $g_javaopts -jar $g_xslts/saxon9he.jar ";
    $cmd .= " -o $g_rdf/cj.rdf $g_ic/cjds.xml ";
    $cmd .= "  $g_xslts/mkjrdf.xsl unoMapFile=$g_ic/ajnunos.xml ";
    $cmd .= " extJournalIn=$g_fb/Journal0.xml ";
    $cmd .= " extJournalOut=$g_fb/Journal1.xml ";
    my $r = doit($cmd, $g_exef);
    exit(1) if($r);
}

############################################
#
# Accumulate and group all IntellCont Authors
#
if($Phases{'CIA'}>0 || $g_all){
    print "\nPhase = CIA ================================\n";
    $g_curPhase = 'CIA';
#
#depends on $g_xslts/empty.xml $g_ic/aiiclist.xml $g_ic/aiid-netid.xml
# $g_xslts/collectByIntellcontAuthors.xsl $g_xslts/groupAuthorsIntellconts.xsl
# prior GX, IC 

    print "\nCollect all intellcont for each author, from all $g_aiic xmls ...\n";
    my $cmd = "";
    $cmd .= "java $g_javaopts -jar $g_xslts/saxon9he.jar $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/collectByIntellcontAuthor.xsl listxml=$g_ic/aiiclist.xml ";
    $cmd .= " aiid2netid=$g_ic/aiid-netid.xml > $g_ic/cica.xml ";
    my $r;
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nIdentify Intellconts mentioned in multiple AIIC xmls ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts -jar $g_xslts/saxon9he.jar $g_ic/cica.xml ";
    $cmd .= " $g_xslts/groupAuthorsIntellconts.xsl > $g_ic/cicad.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);
}
############################################
#
# Accumulate and group IntellCont Promulgators
#
if($Phases{'CIP'}>0 || $g_all){
    print "\nPhase = CIP ================================\n";
    print "\nCollect all intellcont for each promulgator, from all $g_aiic xmls ...\n";
    $g_curPhase = 'CIP';
#
# depends on  $g_xslts/empty.xml, $g_ic/aiiclist.xml
# $g_xslts/collectByIntellcontPromulgators.xsl
# $g_xslts/groupPromulgatorsIntellconts.xsl
# prior GX, IC 

    $cmd = "";
    $cmd .= "java $g_javaopts -jar $g_xslts/saxon9he.jar $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/collectByIntellcontPromulgators.xsl listxml=$g_ic/aiiclist.xml ";
    $cmd .= " >  $g_ic/cicp.xml ";
    my $r;
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nIdentify Intellconts mentioned in multiple AIIC xmls ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts -jar $g_xslts/saxon9he.jar $g_ic/cicp.xml ";
    $cmd .= " $g_xslts/groupPromulgatorsIntellconts.xsl > $g_ic/cicpd.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

}


############################################
#
# Construct rdf for IntellCont Authors
#
if($Phases{'ICAR'}>0 || $g_all){
    print "\nPhase = ICAR ================================\n";
    print "\nConstruct a (sufficiently) large number of unique ";
    print "strings for intellcont author rdf process\n";
    $g_curPhase = 'ICAR';
#
# depends on  $g_ic/cicad.xml, $g_ic/cicpd.xml, $g_xslts/mkicardf.xsl
# $g_xslts/rdfsort.xsl
# prior GX, IC, CIA, -> CIP <-

    mkUnoFile("$g_ic/cicad.xml","counter","$g_ic/ica_unomap.xml");

    initFeedbackFile('Per','P-ICAR');


    print "\nConstruct the rdf for authors and intellconts\n";

    my $cmd = "";
    $cmd .= "java $g_javaopts -jar $g_xslts/saxon9he.jar ";
    $cmd .= " -o $g_rdf/icbya.rdf $g_ic/cicad.xml ";
    $cmd .= " $g_xslts/mkicardf.xsl abypFile=$g_ic/cicpd.xml ";
    $cmd .= " unoMapFile=$g_ic/ica_unomap.xml ";
    $cmd .= " aiicXmlPath=$g_xmls_out aiicPrefix=$g_aiic\_ ";
    $cmd .= " extPerIn=$g_fb/Per0.xml ";
    $cmd .= " extPerOut=$g_fb/Per1.xml ";
    my $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nSort results ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts -jar $g_xslts/saxon9he.jar $g_rdf/icbya.rdf ";
    $cmd .= " $g_xslts/rdfsort.xsl > $g_rdf/sicbya.rdf ";

    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    updateFeedbackFile('Per','ICAR');
}

############################################
#
# Construct rdf for IntellCont Promulgators
#
if($Phases{'ICPR'}>0 || $g_all){
#
# depends on $g_ic/cicpd.xml $g_xslts/mkprdf.xsl
# prior GX, IC, CIP
#
    print "\nPhase = ICPR ================================\n";
    print "\nConstruct a (sufficiently) large number of unique";
    print " strings for promulgator rdf process\n";
    $g_curPhase = 'ICPR';
    mkUnoFile("$g_ic/cicpd.xml",
	      "INTELLCONT_PROMULGATOR_NAME",
	      "$g_ic/icp_nunos.xml");

    print "\nConstruct the rdf for promulgators and intellconts\n";

    my $cmd = "";
    $cmd .= "java $g_javaopts -jar $g_xslts/saxon9he.jar $g_ic/cicpd.xml ";
    $cmd .= "  $g_xslts/mkprdf.xsl unoMapFile=$g_ic/icp_nunos.xml";
    $cmd .= " > $g_rdf/icbyp.rdf ";

    my $r = doit($cmd, $g_exef);
    exit(1) if($r);
}

1;


############################################
#
# Extract IntellCont, Journal Articles add author index, summarize 
# authorships etc on a per netid basis
# formerly IC
if($Phases{'ICIC'}>0 || $g_all){
    print "\nPhase = ICIC ================================\n";
    $g_curPhase = 'ICIC';
#
# depends on $g_xmls_raw e,r and $g_xmls_out e,r,w
# $g_xslts/$g_chain and $g_log_path
# xslt/aiic-Filter.xsl, xslt/aiic-addIndex.xsl
# xslt/aiic-addAuthSummary.xsl, xslt/aiic-addIntellContPromulgators.xsl

    
    my $ext = ($op_jext ne '')?" -e $op_jext ": "" ;
    print "\nBegin integration process using $g_xslts/$g_chain ...\n";
    my $prefix = $g_aiic . "_";
    my $cmd = "";
    $cmd .= "$g_bin/xsltseq -f -I $g_xmls_raw -O $g_xmls_out ";
    $cmd .= " -X $g_xslts/$g_chain -x $g_xslts -p $prefix ";
    $cmd .= " -T $g_icd -t 'IC' -s $g_saxonJar $ext ";
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
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/aiid2netid.xsl ";
    $cmd .= " listxml=$g_ic/aiiclist.xml > $g_ic/aiid-netid.xml ";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);
}
############################################
#
# Accumulate all articles by author by combining data
# from all xmls
# formerly CA
if($Phases{'ICAC'}>0 || $g_all){
    print "\nPhase = ICAC ================================\n";
    $g_curPhase = 'ICAC';
#
#depends on $g_xslts/empty.xml, $g_ic/aiiclist.xml, $g_ic/aiid-netid.xml
# $g_xslts/collectByAuthor.xsl, $g_xslts/groupAuthorsArticles.xsl
# $g_xslts/aiicsort.xsl    prior GX, IC 
#
    print "\nCollect all articles for each author from all $g_aiic xmls ...\n";
    my $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/collectByAuthor.xsl listxml=$g_ic/aiiclist.xml ";
    $cmd .= " aiid2netid=$g_store/raw-aiid-netid.xml > $g_ic/ca.xml ";
    my $r;
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nIdentify articles mentioned in multiple AIIC xmls ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_ic/ca.xml ";
    $cmd .= " $g_xslts/groupAuthorsArticles.xsl > $g_ic/cad.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    $cmd = "";
    print "\nSort an author's articles by Activity Insight id ...\n";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_ic/cad.xml ";
    $cmd .= " $g_xslts/aiicsort.xsl > $g_ic/cads.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);
}


############################################
#
# Accumulate and group all articles by journal by combining data
# from all xmls
# formerly CJ
if($Phases{'ICJC'}>0 || $g_all){
    print "\nPhase = ICJC ================================\n";
    $g_curPhase = 'ICJC';
#
#depends on $g_ic/aiiclist.xml  $g_xslts/empty.xml $g_ic/aiiclist.xml
# $g_xslts/collectByJournals.xsl $g_xslts/groupJournalsArticles.xsl
# $g_xslts/aiicsort.xsl
# prior GX, IC
#
    print "\nCollect all articles for each journal from all $g_aiic xmls ...\n";
    my $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/collectByJournals.xsl listxml=$g_ic/aiiclist.xml ";
    $cmd .= " > $g_ic/cj.xml ";
    my $r;
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nIdentify articles mentioned in multiple AIIC xmls ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_ic/cj.xml ";
    $cmd .= " $g_xslts/groupJournalsArticles.xsl ";
    $cmd .= " > $g_ic/cjd.xml";
    $r = doit($cmd, $g_exef);

    exit(1) if($r);
    
    print "\nSort each journal's articles by Activity Insight id ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_ic/cjd.xml ";
    $cmd .= " $g_xslts/aiicsort.xsl > $g_ic/cjds.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);
}
############################################

if($Phases{'PEEPS'}>0 || $g_all){
    print "\nPhase = PEEPS ================================\n";
    print "\nConstruct a (sufficiently) large number of unique ";
    print "strings for academic article author rdf process\n";
    $g_curPhase = 'PEEPS';
    mkUnoFile("$g_ic/cad.xml","counter",
	      "$g_ic/peeps-unomap.xml","AI-PICAR-","$g_store/.Person");

    my $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence ";
    $cmd .= " -o $g_icjapf $g_ic/cads.xml ";
    $cmd .= " $g_xslts/extractAcademicArticleAuthors.xsl ";
    $cmd .= "  abyjFile=$g_ic/cjds.xml ";
    $cmd .= " unoMapFile=$g_ic/peeps-unomap.xml ";
    $cmd .= " aiicXmlPath=$g_xmls_out aiicPrefix=$g_aiic\_ ";
    $cmd .= " extPerIn=$g_fb/personsAtTimeZero.xml ";
    my $r = doit($cmd, $g_exef);
    exit(1) if($r);


#java  -Xmx8192m -Xms8192m -XX:MaxPermSize=100m   -cp extensions:/home/jrm424/aiw/test/xslt/saxon9ee.jar com.saxonica.Transform   -o /home/jrm424/aiw/test/store/feedback/NewPer.xml /home/jrm424/aiw/test/store/ic-aggregated/cads.xml  /home/jrm424/aiw/test/xslt/addPeeps.xsl abyjFile=/home/jrm424/aiw/test/store/ic-aggregated/cjds.xml  unoMapFile=/home/jrm424/aiw/test/store/ic-aggregated/unomap.xml  extPerIn=/home/jrm424/aiw/test/store/feedback/personsAtTimeZero.xml
}

############################################
#
# Construct rdf for journal article authors
# formerly AR
if($Phases{'ICAR'}>0 || $g_all){
    print "\nPhase = ICAR ================================\n";
    print "\nConstruct a (sufficiently) large number of unique ";
    print "strings for journal article author rdf process\n";
    $g_curPhase = 'ICAR';
#
# depends on $g_ic/cad.xml, $g_ic/cads.xml, $g_ic/cjds.xml
# $g_ic/unomap.xml $g_xslts/mkrdf.xsl $g_xslts/rdfsort.xsl
# $g_fb/Per0.xml
# prior GX, IC, CA, !!-> CJ <-!!
# 
#
    mkUnoFile("$g_ic/cad.xml","counter",
	      "$g_ic/icar-unomap.xml","AI-ICAR-","$g_store/.Person");

    initFeedbackFile('Per','ICAR');

    print "\nConstruct the rdf for authors and articles\n";

    my $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence ";
    $cmd .= " -o $g_rdf/icjabya.rdf $g_ic/cads.xml ";
    $cmd .= " $g_xslts/mkrdf.xsl abyjFile=$g_ic/cjds.xml ";
    $cmd .= " unoMapFile=$g_ic/icar-unomap.xml ";
    $cmd .= " aiicXmlPath=$g_xmls_out aiicPrefix=$g_aiic\_ ";
    $cmd .= " extPerIn=$g_fb/Per0.xml ";
    $cmd .= " extPerOut=$g_fb/Per1.xml ";

    my $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nSort results ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_rdf/icjabya.rdf ";
    $cmd .= " $g_xslts/rdfsort.xsl > $g_rdf/sicjabya.rdf ";

    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    updateFeedbackFile('Per','ICAR');

}
############################################
#
# Construct rdf for journals
# formerly JR
if($Phases{'ICJR'}>0 || $g_all){
    print "\nPhase = ICJR ================================\n";
    print "\nConstruct a (sufficiently) large number of unique";
    print " strings for journal rdf process\n";
    $g_curPhase = 'ICJR';
#
#depends on $g_ic/cjd.xml $g_ic/cjds.xml $g_xslts/mkjrdf.xsl
# prior GX, IC, CJ
#
    mkUnoFile("$g_ic/cjd.xml","JOURNAL_NAME",
	      "$g_ic/icjr-unomap.xml","AI-ICJR-","$g_store/.Journal");
    initFeedbackFile('Jour','ICJR');

    print "\nConstruct the rdf for journals and articles\n";

    my $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence ";
    $cmd .= " -o $g_rdf/cj.rdf $g_ic/cjds.xml ";
    $cmd .= "  $g_xslts/mkjrdf.xsl unoMapFile=$g_ic/icjr-unomap.xml ";
    $cmd .= " extJournalIn=$g_fb/Jour0.xml ";
    $cmd .= " extJournalOut=$g_fb/Jour1.xml ";
    my $r = doit($cmd, $g_exef);
    exit(1) if($r);
    updateFeedbackFile('Jour','ICJR');

}

############################################
#
# Accumulate and group all IntellCont Authors
# formerly CIA
if($Phases{'ICIAC'}>0 || $g_all){
    print "\nPhase = ICIAC ================================\n";
    $g_curPhase = 'ICIAC';
#
#depends on $g_xslts/empty.xml $g_ic/aiiclist.xml $g_ic/aiid-netid.xml
# $g_xslts/collectByIntellcontAuthors.xsl $g_xslts/groupAuthorsIntellconts.xsl
# prior GX, IC 

    print "\nCollect all intellcont for each author, from all $g_aiic xmls ...\n";
    my $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/collectByIntellcontAuthor.xsl listxml=$g_ic/aiiclist.xml ";
    $cmd .= " aiid2netid=$g_store/raw-aiid-netid.xml > $g_ic/cica.xml ";
    my $r;
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nIdentify Intellconts mentioned in multiple AIIC xmls ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_ic/cica.xml ";
    $cmd .= " $g_xslts/groupAuthorsIntellconts.xsl > $g_ic/cicad.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);
}
############################################
#
# Accumulate and group IntellCont Promulgators
# formerly CIP
if($Phases{'ICIPC'}>0 || $g_all){
    print "\nPhase = ICIPC ================================\n";
    print "\nCollect all intellcont for each promulgator, from all $g_aiic xmls ...\n";
    $g_curPhase = 'ICIPC';
#
# depends on  $g_xslts/empty.xml, $g_ic/aiiclist.xml
# $g_xslts/collectByIntellcontPromulgators.xsl
# $g_xslts/groupPromulgatorsIntellconts.xsl
# prior GX, IC 

    $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/collectByIntellcontPromulgators.xsl listxml=$g_ic/aiiclist.xml ";
    $cmd .= " >  $g_ic/cicp.xml ";
    my $r;
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nIdentify Intellconts mentioned in multiple AIIC xmls ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_ic/cicp.xml ";
    $cmd .= " $g_xslts/groupPromulgatorsIntellconts.xsl > $g_ic/cicpd.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

}


############################################
#
# Construct rdf for IntellCont Authors
# formerly ICAR
if($Phases{'ICIAR'}>0 || $g_all){
    print "\nPhase = ICIAR ================================\n";
    print "\nConstruct a (sufficiently) large number of unique ";
    print "strings for intellcont author rdf process\n";
    $g_curPhase = 'ICIAR';
#
# depends on  $g_ic/cicad.xml, $g_ic/cicpd.xml, $g_xslts/mkicardf.xsl
# $g_xslts/rdfsort.xsl
# prior GX, IC, CIA, -> CIP <-

    mkUnoFile("$g_ic/cicad.xml","counter",
	      "$g_ic/iciar_unomap.xml","AI-ICIAR-","$g_store/.Person");

    initFeedbackFile('Per','ICIAR');


    print "\nConstruct the rdf for authors and intellconts\n";

    my $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence ";
    $cmd .= " -o $g_rdf/icbya.rdf $g_ic/cicad.xml ";
    $cmd .= " $g_xslts/mkicardf.xsl abypFile=$g_ic/cicpd.xml ";
    $cmd .= " unoMapFile=$g_ic/iciar_unomap.xml ";
    $cmd .= " aiicXmlPath=$g_xmls_out aiicPrefix=$g_aiic\_ ";
    $cmd .= " extPerIn=$g_fb/Per0.xml ";
    $cmd .= " extPerOut=$g_fb/Per1.xml ";
    my $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nSort results ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_rdf/icbya.rdf ";
    $cmd .= " $g_xslts/rdfsort.xsl > $g_rdf/sicbya.rdf ";

    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    updateFeedbackFile('Per','ICIAR');
}

############################################
#
# Construct rdf for IntellCont Promulgators
# formerly ICPR
if($Phases{'ICIPR'}>0 || $g_all){
#
# depends on $g_ic/cicpd.xml $g_xslts/mkprdf.xsl
# prior GX, IC, CIP
#
    print "\nPhase = ICIPR ================================\n";
    print "\nConstruct a (sufficiently) large number of unique";
    print " strings for promulgator rdf process\n";
    $g_curPhase = 'ICIPR';
    mkUnoFile("$g_ic/cicpd.xml",
	      "INTELLCONT_PROMULGATOR_NAME",
	      "$g_ic/icipr_nunos.xml",
	      "AI-ICIPR-",$op_uno);

    print "\nConstruct the rdf for promulgators and intellconts\n";

    my $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_ic/cicpd.xml ";
    $cmd .= "  $g_xslts/mkprdf.xsl unoMapFile=$g_ic/icipr_nunos.xml";
    $cmd .= " > $g_rdf/icbyp.rdf ";

    my $r = doit($cmd, $g_exef);
    exit(1) if($r);
}

1;

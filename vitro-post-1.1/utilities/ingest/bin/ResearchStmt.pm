
############################################
#
# Accumulate and group all research stmt data by name (parts)
# from all xmls
# formerly RESRCH
if($Phases{'RSC'}>0 || $g_all){
    print "\nPhase = RSC ================================\n";
    $g_curPhase = 'RSC';
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/researchStmtPerson.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_restmt/cresearchStmtPerson.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
}
############################################
# make rdf
# formerly MRESRCH
if($Phases{'RSR'}>0 || $g_all){
    print "\nPhase = RSR ================================\n";
    $g_curPhase = 'RSR';
    mkUnoFile("$g_restmt/cresearchStmtPerson.xml", 
	      "counter", "$g_restmt/rsr-unomap.xml");
    initFeedbackFile('Per','PRSR');
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence ";
    $cmd .= " -o $g_rdf/researchStmt.rdf ";
    $cmd .= " $g_restmt/cresearchStmtPerson.xml ";
    $cmd .= " $g_xslts/mkResearchStmtRdf.xsl ";
    $cmd .= " unoMapFile=$g_restmt/rsr-unomap.xml ";
    $cmd .= " concentrationIn=$g_restmt/ConcentrationArea.xml ";
    $cmd .= " rawXmlPath=$g_xmls_raw ";
    $cmd .= " extPerIn=$g_fb/Per0.xml ";
    $cmd .= " extPerOut=$g_fb/Per1.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

   updateFeedbackFile('Per', 'RSR');
}




1;

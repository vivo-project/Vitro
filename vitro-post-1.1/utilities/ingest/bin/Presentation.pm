############################################
#
# Accumulate and group all PRESENTation data by ai id
# from all xmls

if($Phases{'PRPRC'}>0 || $g_all){
    print "\nPhase = PRPRC ================================\n";
    $g_curPhase = 'PRPRC';
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/cPresentation.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_pres/cpresentation.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
}
############################################
if($Phases{'PROC'}>0 || $g_all){
    print "\nPhase = PROC ================================\n";
    $g_curPhase = 'PROC';
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/cPresentationOrg.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_pres/cpresentationOrg.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
}
############################################
if($Phases{'PRCC'}>0 || $g_all){
    print "\nPhase = PRCC ================================\n";
    $g_curPhase = 'PRCC';
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/cPresentationConf.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_pres/cpresentationConf.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
}
############################################
if($Phases{'PRPC'}>0 || $g_all){
    print "\nPhase = PRPC ================================\n";
    $g_curPhase = 'PRPC';
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/cPresentationPerson.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " aiid2netid=$g_store/all-raw-aiid-netid.xml ";
    $cmd .= " > $g_pres/cpresentationPerson.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
}





############################################
# make rdf

if($Phases{'PRR'}>0 || $g_all){
    print "\nPhase = PRR ================================\n";
    $g_curPhase = 'PRR';
    mkUnoFile("$g_pres/cpresentation.xml", "index", 
	      "$g_pres/prr-unomap.xml",
	      "AI-PRR-",$op_uno);
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence $g_pres/cpresentation.xml ";
    $cmd .= "$g_xslts/mkPresentation.xsl unoMapFile=$g_pres/prr-unomap.xml ";
    $cmd .= "rawXmlPath=$g_xmls_raw > $g_rdf/presentation.rdf";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

}




############################################
# make rdf

if($Phases{'PRPR'}>0 || $g_all){
    print "\nPhase = PRPR ================================\n";
    $g_curPhase = 'PRPR';
    mkUnoFile("$g_pres/cpresentationperson.xml", 
	      "index", "$g_pres/prpr-unomap.xml",
	      "AI-PRPR-","$g_store/.Person");
    initFeedbackFile('Per','PRPR');
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence ";
    $cmd .= " -o $g_rdf/presentationPerson.rdf ";
    $cmd .= " $g_pres/cpresentationperson.xml ";
    $cmd .= " $g_xslts/mkPresentationPerson.xsl ";
    $cmd .= " unoMapFile=$g_pres/prpr-unomap.xml ";
    $cmd .= " rawXmlPath=$g_xmls_raw ";
    $cmd .= " extPerIn=$g_fb/Per0.xml ";
    $cmd .= " extPerOut=$g_fb/Per1.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

    updateFeedbackFile('Per', 'MCPR');
}



############################################
# make rdf

if($Phases{'PROR'}>0 || $g_all){
    print "\nPhase = PROR ================================\n";
    $g_curPhase = 'PROR';
    mkUnoFile("$g_pres/cpresentationorg.xml", 
	      "index", "$g_pres/pror-unomap.xml",
	      "AI-PROR-","$g_store/.Org");
    initFeedbackFile('Org','PROR');
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence ";
    $cmd .= " -o $g_rdf/presentationOrg.rdf ";
    $cmd .= " $g_pres/cpresentationorg.xml ";
    $cmd .= " $g_xslts/mkPresentationOrg.xsl ";
    $cmd .= " unoMapFile=$g_edu/pror-unomap.xml ";
    $cmd .= " rawXmlPath=$g_xmls_raw ";
    $cmd .= " extOrgIn=$g_fb/Org0.xml ";
    $cmd .= " extOrgOut=$g_fb/Org1.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

    updateFeedbackFile('Org', 'PROR');
}










1;


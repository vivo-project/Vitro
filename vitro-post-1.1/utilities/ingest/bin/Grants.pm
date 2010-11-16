############################################
# formerly GRNT
if($Phases{'GRC'}>0 || $g_all){
    print "\nPhase = GRC ================================\n";
    $g_curPhase = 'GRC';
    print "Collect all grant\n";
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/grant.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_gr/cgrants.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

}
############################################
# formerly MGRNT
if($Phases{'GRR'}>0 || $g_all){
    print "\nPhase = GRR ================================\n";
    $g_curPhase = 'GRR';
    mkUnoFile("$g_gr/cgrants.xml", "index", 
	      "$g_gr/grr-unomap.xml","AI-GRR-",$op_uno);
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence $g_gr/cgrants.xml ";
    $cmd .= "$g_xslts/mkGrantRdf.xsl unoMapFile=$g_gr/grr-unomap.xml ";
    $cmd .= "rawXmlPath=$g_xmls_raw > $g_rdf/grants.rdf";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

}
############################################
# formerly GRNT_INV
if($Phases{'GRIC'}>0 || $g_all){
    print "\nPhase = GRIC ================================\n";
    $g_curPhase = 'GRIC';
    print "Collect all grant investigators\n";
    mkListAllRaw();
    mkGrAIid2NetidMap();
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/grantInvestigator.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " aiid2netid=$g_gr/aiid-netid.xml ";
    $cmd .= " > $g_gr/cgrantInvs.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

}
############################################
# formerly MGRNT_INV
if($Phases{'GRIR'}>0 || $g_all){
    print "\nPhase = GRIR ================================\n";
    $g_curPhase = 'GRIR';
    mkUnoFile("$g_gr/cgrantInvs.xml", "index", 
	      "$g_gr/grir-unomap.xml","AI-GRIR-","$g_store/.Person");
    initFeedbackFile('Per','PGRIR');

    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence ";
    $cmd .= " -o $g_rdf/grantInv.rdf ";
    $cmd .= " $g_gr/cgrantInvs.xml ";
    $cmd .= " $g_xslts/mkGrantInvestRdf.xsl ";
    $cmd .= " unoMapFile=$g_gr/grir-unomap.xml ";
    $cmd .= " rawXmlPath=$g_xmls_raw ";
    $cmd .= " extPerIn=$g_fb/Per0.xml ";
    $cmd .= " extPerOut=$g_fb/Per1.xml ";

    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

    updateFeedbackFile('Per', 'GRIR');

}
############################################
# formerly GRNT_SPON
if($Phases{'GRSPC'}>0 || $g_all){
    print "\nPhase = GRSPC ================================\n";
    $g_curPhase = 'GRSPC';
    print "Collect all grant sponsor orgs\n";

    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/grantSponsorOrg.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_gr/cgrantSponsorOrg.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

}
############################################
# formerly MGRNT_SPON
if($Phases{'GRSPR'}>0 || $g_all){
    print "\nPhase = GRSPR ================================\n";
    $g_curPhase = 'GRSPR';
    mkUnoFile("$g_gr/cgrantSponsorOrg.xml", "index", 
	      "$g_gr/grspr-unomap.xml","AI-GRSPR-","$g_store/.Org");
    initFeedbackFile('Org','OGRSPR');

    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence ";
    $cmd .= " -o $g_rdf/grantSponsorOrg.rdf";
    $cmd .= " $g_gr/cgrantSponsorOrg.xml ";
    $cmd .= " $g_xslts/mkGrantSponsorOrgRdf.xsl ";
    $cmd .= " unoMapFile=$g_gr/grspr-unomap.xml ";
    $cmd .= " rawXmlPath=$g_xmls_raw ";
    $cmd .= " extOrgIn=$g_fb/Org0.xml ";
    $cmd .= " extOrgOut=$g_fb/Org1.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
    updateFeedbackFile('Org', 'GRSPR');
}


sub mkGrAIid2NetidMap {
    if(! -e "$g_gr/aiid-netid.xml"){
	print "\nConstruct a list mapping of netids to/from ai ids in $g_gr/aiid-netid.xml ...\n";
	my $cmd = "";
	$cmd .= "java $g_javaopts $g_saxonCmdSequence $g_store/Users.xml ";
	$cmd .= " $g_xslts/uidmap.xsl | $g_bin/nzsxml -d $g_xmls_raw ";
	$cmd .= " > $g_gr/aiid-netid.xml ";
	my$r = doit($cmd, $g_exef, $g_pw);
	exit(1) if($r);
    }
}


1;

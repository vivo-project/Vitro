############################################
#
if($Phases{'GRNT'}>0 || $g_all){
    print "\nPhase = GRNT ================================\n";
    $g_curPhase = 'GRNT';
    print "Collect all grant\n";
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/grant.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_gr/cgrants.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

}
############################################
#
if($Phases{'MGRNT'}>0 || $g_all){
    print "\nPhase = MGRNT ================================\n";
    $g_curPhase = 'MGRNT';
    mkUnoFile("$g_gr/cgrants.xml", "index", "$g_gr/gr-unomap.xml");
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_gr/cgrants.xml ";
    $cmd .= "$g_xslts/mkGrantRdf.xsl unoMapFile=$g_gr/gr-unomap.xml ";
    $cmd .= "rawXmlPath=$g_xmls_raw > $g_rdf/grants.rdf";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

}
############################################
#
if($Phases{'GRNT_INV'}>0 || $g_all){
    print "\nPhase = GRNT_INV ================================\n";
    $g_curPhase = 'GRNT_INV';
    print "Collect all grant investigators\n";
    mkListAllRaw();
    mkGrAIid2NetidMap();
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/grantInvestigator.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " aiid2netid=$g_gr/aiid-netid.xml ";
    $cmd .= " > $g_gr/cgrantInvs.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

}
############################################
#
if($Phases{'MGRNT_INV'}>0 || $g_all){
    print "\nPhase = MGRNT_INV ================================\n";
    $g_curPhase = 'MGRNT_INV';
    mkUnoFile("$g_gr/cgrantInvs.xml", "index", "$g_gr/grinv-unomap.xml");
    initFeedbackFile('Per','P-MGRNT_INV');

    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar ";
    $cmd .= " -o $g_rdf/grantInv.rdf ";
    $cmd .= " $g_gr/cgrantInvs.xml ";
    $cmd .= " $g_xslts/mkGrantInvestRdf.xsl ";
    $cmd .= " unoMapFile=$g_gr/grinv-unomap.xml ";
    $cmd .= " rawXmlPath=$g_xmls_raw ";
    $cmd .= " extPerIn=$g_fb/Per0.xml ";
    $cmd .= " extPerOut=$g_fb/Per1.xml ";

    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

    updateFeedbackFile('Per', 'MGRNT_INV');

}
############################################
#
if($Phases{'GRNT_SPON'}>0 || $g_all){
    print "\nPhase = GRNT_SPON ================================\n";
    $g_curPhase = 'GRNT_SPON';
    print "Collect all grant sponsor orgs\n";

    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/grantSponsorOrg.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_gr/cgrantSponsorOrg.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

}
############################################
#
if($Phases{'MGRNT_SPON'}>0 || $g_all){
    print "\nPhase = MGRNT_SPON ================================\n";
    $g_curPhase = 'MGRNT_SPON';
    mkUnoFile("$g_gr/cgrantSponsorOrg.xml", "index", 
	      "$g_gr/grspon-unomap.xml");
    initFeedbackFile('Org','O-MGRNT_SPON');

    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar ";
    $cmd .= " -o $g_rdf/grantSponsorOrg.rdf";
    $cmd .= " $g_gr/cgrantSponsorOrg.xml ";
    $cmd .= " $g_xslts/mkGrantSponsorOrgRdf.xsl ";
    $cmd .= " unoMapFile=$g_gr/grspon-unomap.xml ";
    $cmd .= " rawXmlPath=$g_xmls_raw ";
    $cmd .= " extOrgIn=$g_fb/Org0.xml ";
    $cmd .= " extOrgOut=$g_fb/Org1.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
    updateFeedbackFile('Org', 'MGRNT_SPON');
}

############################################
#
if($Phases{'GRNT_ARS'}>0 || $g_all){
    print "\nPhase = GRNT_ARS ================================\n";
    $g_curPhase = 'GRNT_ARS';
    print "Collect all grant agency recv submissions \n";
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/grantSubAgency.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_gr/cgrantArs.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

}

############################################
#
if($Phases{'MGRNT_ARS'}>0 || $g_all){
    print "\nPhase = MGRNT_ARS ================================\n";
    $g_curPhase = 'MGRNT_ARS';
    mkUnoFile("$g_gr/cgrantArs.xml", "index", "$g_gr/grars-unomap.xml");
    initFeedbackFile('Org','O-MGRNT_ARS');

    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar ";
    $cmd .= " -o $g_rdf/grantArs.rdf";
    $cmd .= " $g_gr/cgrantArs.xml ";
    $cmd .= " $g_xslts/mkGrantAgencyRecvSubRdf.xsl ";
    $cmd .= " unoMapFile=$g_gr/grars-unomap.xml ";
    $cmd .= " rawXmlPath=$g_xmls_raw ";
    $cmd .= " extOrgIn=$g_fb/Org0.xml ";
    $cmd .= " extOrgOut=$g_fb/Org1.xml ";

    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
    updateFeedbackFile('Org', 'MGRNT_ARS');
}
############################################
#
if($Phases{'GRNT_PFA'}>0 || $g_all){
    print "\nPhase = GRNT_PFA ================================\n";
    $g_curPhase = 'GRNT_PFA';
    print "Collect all grant Previous Funding Agencies \n";
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/grantPreviousAgency.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_gr/cgrantPfa.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

}

############################################
#
if($Phases{'MGRNT_PFA'}>0 || $g_all){
    print "\nPhase = MGRNT_FA ================================\n";
    $g_curPhase = 'MGRNT_PFA';
    mkUnoFile("$g_gr/cgrantPfa.xml", "index", "$g_gr/grpfa-unomap.xml");
    initFeedbackFile('Org','O-MGRNT_PFA');

    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar ";
    $cmd .= " -o $g_rdf/grantPfa.rdf";
    $cmd .= " $g_gr/cgrantPfa.xml ";
    $cmd .= " $g_xslts/mkGrantPrevAgencyRdf.xsl ";
    $cmd .= " unoMapFile=$g_gr/grpfa-unomap.xml ";
    $cmd .= " rawXmlPath=$g_xmls_raw ";
    $cmd .= " extOrgIn=$g_fb/Org0.xml ";
    $cmd .= " extOrgOut=$g_fb/Org1.xml ";

    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
    updateFeedbackFile('Org', 'MGRNT_PFA');
}
############################################
#
if($Phases{'GRNT_PE'}>0 || $g_all){
    print "\nPhase = GRNT_PE ================================\n";
    $g_curPhase = 'GRNT_PE';
    print "Collect all grant planned effort\n";
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/grantPlannedEffort.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_gr/cgrantpe.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

}
############################################
#
if($Phases{'MGRNT_PE'}>0 || $g_all){
    print "\nPhase = MGRNT_PE ================================\n";
    $g_curPhase = 'MGRNT_PE';
    mkUnoFile("$g_gr/cgrantpe.xml", "index", "$g_gr/grpe-unomap.xml");
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_gr/cgrantpe.xml ";
    $cmd .= "$g_xslts/mkGrantPlannedEffortRdf.xsl ";
    $cmd .= "unoMapFile=$g_gr/grpe-unomap.xml ";
    $cmd .= "rawXmlPath=$g_xmls_raw > $g_rdf/grantPE.rdf";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

}
############################################
#
if($Phases{'GRNT_COST'}>0 || $g_all){
    print "\nPhase = GRNT_COST ================================\n";
    $g_curPhase = 'GRNT_COST';
    print "Collect all grant cost\n";
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/grantCost.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_gr/cgrantCost.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

}
############################################
#
if($Phases{'MGRNT_COST'}>0 || $g_all){
    print "\nPhase = MGRNT_COST ================================\n";
    $g_curPhase = 'MGRNT_COST';
    mkUnoFile("$g_gr/cgrantCost.xml", "index", "$g_gr/grcost-unomap.xml");
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_gr/cgrantCost.xml ";
    $cmd .= "$g_xslts/mkGrantCostRdf.xsl unoMapFile=$g_gr/grcost-unomap.xml ";
    $cmd .= "rawXmlPath=$g_xmls_raw > $g_rdf/grantCost.rdf";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

}
############################################
#
if($Phases{'GRNT_COMM'}>0 || $g_all){
    print "\nPhase = GRNT_COMM ================================\n";
    $g_curPhase = 'GRNT_COMM';
    print "Collect all grant comments\n";
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/grantComment.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_gr/cgrantComments.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

}
############################################
#
if($Phases{'MGRNT_COMM'}>0 || $g_all){
    print "\nPhase = MGRNT_COMM ================================\n";
    $g_curPhase = 'MGRNT_COMM';
    mkUnoFile("$g_gr/cgrantComments.xml", "index", "$g_gr/grcomm-unomap.xml");
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_gr/cgrantComments.xml ";
    $cmd .= "$g_xslts/mkGrantCommentRdf.xsl unoMapFile=$g_gr/grcomm-unomap.xml ";
    $cmd .= "rawXmlPath=$g_xmls_raw > $g_rdf/grantComment.rdf";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

}


sub mkGrAIid2NetidMap {
    if(! -e "$g_gr/aiid-netid.xml"){
	print "\nConstruct a list mapping of netids to/from ai ids in $g_gr/aiid-netid.xml ...\n";
	my $cmd = "";
	$cmd .= "java $g_javaopts -jar $g_xslts/saxon9he.jar $g_store/Users.xml ";
	$cmd .= " $g_xslts/uidmap.xsl | $g_bin/nzsxml -d $g_xmls_raw ";
	$cmd .= " > $g_gr/aiid-netid.xml ";
	my$r = doit($cmd, $g_exef, $g_pw);
	exit(1) if($r);
    }
}


1;

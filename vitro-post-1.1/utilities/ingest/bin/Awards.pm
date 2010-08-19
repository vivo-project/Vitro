############################################
#
# Accumulate and group all awards by recipient, sponsor
# from all xmls
#
if($Phases{'CAW'}>0 || $g_all){
    print "\nPhase = CAW ================================\n";
    $g_curPhase = 'CAW';
    

    my $cmd = "";
    $cmd .= "$g_bin/mklist -d $g_xmls_raw > $g_aw/allnzraw.xml";
    my $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nCollect all awards from all RAW xmls ...\n";
    $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/award.xsl listxml=$g_aw/allnzraw.xml ";
    $cmd .= " > $g_aw/cawards.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nCollect all awards by recipient from all RAW xmls ...\n";
    $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/awardRecipient.xsl listxml=$g_aw/allnzraw.xml ";
    $cmd .= " > $g_aw/cawardRecipients.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nCollect all awards by sponsor org from all RAW xmls ...\n";
    $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_xslts/empty.xml ";
    $cmd .= "$g_xslts/awardOrg.xsl listxml=$g_aw/allnzraw.xml ";
    $cmd .= "> $g_aw/cawardOrganizations.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

}

if($Phases{'MAWR'}>0 || $g_all){
    print "\nPhase = MAWR ================================\n";
    $g_curPhase = 'MAWR';
    print "\n Make awarding org rdf\n";
    mkUnoFile("$g_aw/cawardOrganizations.xml",
	      "counter",
	      "$g_aw/awardOrg-unomap.xml");

    initFeedbackFile('Org','O-MAWR');

    print "\nConstruct the rdf for sponsor orgs and awards\n";
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar ";
    $cmd .= " -o $g_rdf/awardOrganizations.rdf ";
    $cmd .= " $g_aw/cawardOrganizations.xml $g_xslts/mkAwardOrgRdf.xsl ";
    $cmd .= " unoMapFile=$g_aw/awardOrg-unomap.xml ";
    $cmd .= " extOrgIn=$g_fb/Org0.xml ";
    $cmd .= " extOrgOut=$g_fb/Org1.xml ";
    my $r = doit($cmd, $g_exef);
    exit(1) if($r);

    updateFeedbackFile('Org', 'MAWR');

    #############################################
    print "\n Make award recipient rdf\n";

    mkUnoFile("$g_aw/cawardRecipients.xml", "counter",
	      "$g_aw/awardRecipient-unomap.xml");

    initFeedbackFile('Per','P-MAWR');

    $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar ";
    $cmd .= " -o $g_rdf/awardRecipients.rdf $g_aw/cawardRecipients.xml ";
    $cmd .= " $g_xslts/mkAwardRecipientRdf.xsl ";
    $cmd .= " unoMapFile=$g_aw/awardRecipient-unomap.xml ";
    $cmd .= " extPerIn=$g_fb/Per0.xml ";
    $cmd .= " extPerOut=$g_fb/Per1.xml ";
    $cmd .= " rawXmlPath=$g_xmls_raw";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    updateFeedbackFile('Per', 'MAWR');

    #############################################
    print "\n Make award rdf\n";
    mkUnoFile("$g_aw/cawards.xml","'<aiah:AWARD '",
	      "$g_aw/awards-unomap.xml");

    
    $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar -o $g_rdf/awards.rdf ";
    $cmd .= " $g_aw/cawards.xml $g_xslts/mkAwardRdf.xsl ";
    $cmd .= " unoMapFile=$g_aw/awards-unomap.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);


}

1;


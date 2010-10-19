############################################
#
# Accumulate and group all awards by recipient, sponsor
# from all xmls
# formerly CAW
if($Phases{'AWC'}>0 || $g_all){
    print "\nPhase = AWC ================================\n";
    $g_curPhase = 'AWC';
    

    my $cmd = "";
    $cmd .= "$g_bin/mklist -d $g_xmls_raw > $g_aw/allnzraw.xml";
    my $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nCollect all awards from all RAW xmls ...\n";
    $cmd = "";
    $cmd .= "java $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/award.xsl listxml=$g_aw/allnzraw.xml ";
    $cmd .= " > $g_aw/cawards.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nCollect all awards by recipient from all RAW xmls ...\n";
    $cmd = "";
    $cmd .= "java $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/awardRecipient.xsl listxml=$g_aw/allnzraw.xml ";
    $cmd .= " > $g_aw/cawardRecipients.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nCollect all awards by sponsor org from all RAW xmls ...\n";
    $cmd = "";
    $cmd .= "java $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= "$g_xslts/awardOrg.xsl listxml=$g_aw/allnzraw.xml ";
    $cmd .= "> $g_aw/cawardOrganizations.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

}
#formerly MAWR
if($Phases{'AWR'}>0 || $g_all){
    print "\nPhase = AWR ================================\n";
    $g_curPhase = 'AWR';
    print "\n Make awarding org rdf\n";
    mkUnoFile("$g_aw/cawardOrganizations.xml",
	      "counter",
	      "$g_aw/oawr-unomap.xml");

    initFeedbackFile('Org','OAWR');

    print "\nConstruct the rdf for sponsor orgs and awards\n";
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence ";
    $cmd .= " -o $g_rdf/awardOrganizations.rdf ";
    $cmd .= " $g_aw/cawardOrganizations.xml $g_xslts/mkAwardOrgRdf.xsl ";
    $cmd .= " unoMapFile=$g_aw/oawr-unomap.xml ";
    $cmd .= " extOrgIn=$g_fb/Org0.xml ";
    $cmd .= " extOrgOut=$g_fb/Org1.xml ";
    my $r = doit($cmd, $g_exef);
    exit(1) if($r);

    updateFeedbackFile('Org', 'OAWR');

    #############################################
    print "\n Make award recipient rdf\n";

    mkUnoFile("$g_aw/cawardRecipients.xml", "counter",
	      "$g_aw/pawr-unomap.xml");

    initFeedbackFile('Per','PAWR');

    $cmd = "";
    $cmd .= "java $g_saxonCmdSequence ";
    $cmd .= " -o $g_rdf/awardRecipients.rdf $g_aw/cawardRecipients.xml ";
    $cmd .= " $g_xslts/mkAwardRecipientRdf.xsl ";
    $cmd .= " unoMapFile=$g_aw/pawr-unomap.xml ";
    $cmd .= " extPerIn=$g_fb/Per0.xml ";
    $cmd .= " extPerOut=$g_fb/Per1.xml ";
    $cmd .= " rawXmlPath=$g_xmls_raw";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    updateFeedbackFile('Per', 'PAWR');

    #############################################
    print "\n Make award rdf\n";
    mkUnoFile("$g_aw/cawards.xml","'<aiah:AWARD '",
	      "$g_aw/awr-unomap.xml");

    
    $cmd = "";
    $cmd .= "java $g_saxonCmdSequence -o $g_rdf/awards.rdf ";
    $cmd .= " $g_aw/cawards.xml $g_xslts/mkAwardRdf.xsl ";
    $cmd .= " unoMapFile=$g_aw/awr-unomap.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);


}

1;


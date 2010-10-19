

############################################
#
# Extract Impact Statements, add collaborator index, summarize impact stmts by
# funding orgs, collaborative entities/investigators, geo areas, emphasis etc
#
# Also create an xml listing the xmls in $g_is_xmls_out
# formerly IS
if($Phases{'ISIS'}>0 || $g_all){
    print "\nPhase = ISIS ================================\n";
#
# depends on $g_xmls_raw e,r and $g_is_xmls_out e,r,w
# $g_xslts/$g_is_chain and $g_log_path
# $g_store/Users.xml
# xslt/impact-stmt-Filter.xsl, xslt/impact-stmt-addIndex.xsl
# xslt/impact-stmt-addAuthSummary.xsl
    $g_curPhase = 'ISIS';
    my $ext = ($op_jext ne '')?" -e $op_jext ": "" ;
    print "\nBegin integration process using $g_xslts/$g_is_chain ...\n";
    my $prefix = $g_aiis . "_";
    my $cmd = "";
    $cmd .= "$g_bin/xsltseq -I $g_xmls_raw -O $g_is_xmls_out ";
    $cmd .= " -X $g_xslts/$g_is_chain -x $g_xslts -p $prefix ";
    $cmd .= " -T $g_isd -t 'IS' -s $g_saxonJar $ext ";
    $cmd .= " -L $g_log_path " if $g_log_path ne '';
    my $r;
    $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

    print "\nConstruct a list of xmls by netids in $g_is/aiislist.xml ...\n";
    $cmd = "";
    $cmd .= "$g_bin/mklist -d $g_is_xmls_out > $g_is/aiislist.xml";
    $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

    print "\nConstruct a list mapping of netids to/from ai ids in $g_is/is-aiid-netid.xml ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_store/Users.xml ";
    $cmd .= " $g_xslts/uidmap.xsl | $g_bin/nzsxml -d $g_xmls_raw ";
    $cmd .= " > $g_is/is-aiid-netid.xml ";
    $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
}

############################################
#
# Accumulate and group all Impact Stmts by Investigator by combining data
# from all xmls
# formerly CINV
if($Phases{'ISIC'}>0 || $g_all){
    print "\nPhase = ISIC ================================\n";
    $g_curPhase = 'ISIC';
#
#depends on $g_xslts/empty.xml, $g_is/aiislist.xml, $g_is/is-aiid-netid.xml
# $g_xslts/collectByInvestigators.xsl, 
# $g_xslts/groupInvestigatorsImpactStmts.xsl
# $g_xslts/aiicsort.xsl    prior GX, IS
#
    print "\nCollect all Impact Stmts for each Investigator from all $g_aiis xmls ...\n";
    my $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/collectByInvestigators.xsl listxml=$g_is/aiislist.xml ";
    $cmd .= " aiid2netid=$g_is/is-aiid-netid.xml > $g_is/ci.xml ";
    my $r;
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nIdentify impact stmts mentioned in multiple AIIS xmls ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_is/ci.xml ";
    $cmd .= " $g_xslts/groupInvestigatorsImpactStmts.xsl > $g_is/cid.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    $cmd = "";
    print "\nSort impact stmts by Activity Insight id ...\n";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_is/cid.xml ";
    $cmd .= " $g_xslts/aiicsort.xsl > $g_is/cids.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    $cmd = "";
    print "\nExtract a list of impact stmts by Activity Insight id ...\n";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_is/cids.xml ";
    $cmd .= " $g_xslts/all-ImpactStmts.xsl > $g_is/all-impactsByInvest.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);
}
############################################
#
# Construct rdf for impact statement investigators
# formerly MIR
if($Phases{'ISIR'}>0 || $g_all){
    print "\nPhase = ISIR ================================\n";
    print "\nConstruct a (sufficiently) large number of unique ";
    print "strings for impact stmt rdf process\n";
    $g_curPhase = 'ISIR';
#
# depends on $g_is/cid.xml, $g_is/cids.xml, $g_is/all-impactsByInvest.xml
# $g_xslts/mkInvestRdf.xsl $g_xslts/rdfsort.xsl
# $g_store/all-curPersons.xml
# 
# 
#
    mkUnoFile("$g_is/cids.xml","counter","$g_is/isir-unomap.xml");

    initFeedbackFile('Per','ISIR');
    print "\nConstruct the rdf for investigators and impact stmts\n";

    my $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence ";
    $cmd .= " -o $g_rdf/isbyinv.rdf $g_is/cids.xml ";
    $cmd .= "  $g_xslts/mkInvestRdf.xsl isByInvFile=$g_is/all-impactsByInvest.xml ";
    $cmd .= " unoMapFile=$g_is/isir-unomap.xml ";
    $cmd .= "  aiisXmlPath=$g_is_xmls_out aiisPrefix=$g_aiis\_ ";
    $cmd .= " extPerIn=$g_fb/Per0.xml ";
    $cmd .= " extPerOut=$g_fb/Per1.xml ";

    my $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nSort results ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_rdf/isbyinv.rdf ";
    $cmd .= " $g_xslts/rdfsort.xsl > $g_rdf/sisbyinv.rdf ";

    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    updateFeedbackFile('Per','ISIR');

}
############################################
#
# Accumulate and group all Impact Stmts by Collaborative Entitiy by 
# combining data from all xmls
# formerly CCE
if($Phases{'ISCEC'}>0 || $g_all){
    print "\nPhase = ISCEC ================================\n";
    $g_curPhase = 'ISCEC';
#
#depends on $g_xslts/empty.xml, $g_is/aiislist.xml, $g_is/is-aiid-netid.xml
# $g_xslts/collectByCollaborativeEntity.xsl, 
# $g_xslts/groupCollaborativeEntitysImpactStmts.xsl
# $g_xslts/aiicsort.xsl    prior GX, ISIS
#
    print "\nCollect all Impact Stmts for each collaborative entity from all $g_aiis xmls ...\n";
    my $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/collectByCollaborativeEntity.xsl listxml=$g_is/aiislist.xml ";
    $cmd .= " aiid2netid=$g_is/is-aiid-netid.xml > $g_is/cce.xml ";
    my $r;
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nIdentify impact stmts mentioned in multiple AIIS xmls ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_is/cce.xml ";
    $cmd .= " $g_xslts/groupCollaborativeEntitysImpactStmts.xsl > $g_is/cced.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    $cmd = "";
    print "\nSort impact stmts by Activity Insight id ...\n";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_is/cced.xml ";
    $cmd .= " $g_xslts/aiicsort.xsl > $g_is/cceds.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);
}
# Construct rdf for impact statement collaborative entities
# formerly MCER
if($Phases{'ISCER'}>0 || $g_all){
    print "\nPhase = ISCER ================================\n";
    print "\nConstruct a (sufficiently) large number of unique ";
    print "strings for impact stmt rdf process\n";
    $g_curPhase = 'ISCER';
#
# depends on $g_is/cced.xml, $g_is/cceds.xml,
# $g_xslts/mkCollabEntityRdf.xsl $g_xslts/rdfsort.xsl
# 
# 
# 
#
    mkUnoFile("$g_is/cceds.xml","counter","$g_is/iscer-unomap.xml");
    initFeedbackFile('Ceo','ISCER');
    print "\nConstruct the rdf for collaborative entities and impact stmts\n";

    my $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence ";
    $cmd .= " -o $g_rdf/isbyce.rdf $g_is/cceds.xml ";
    $cmd .= "  $g_xslts/mkCollabEntityRdf.xsl ";
    $cmd .= " unoMapFile=$g_is/iscer-unomap.xml ";
    $cmd .= " aiisXmlPath=$g_is_xmls_out aiisPrefix=$g_aiis\_ ";
    $cmd .= " extOrgIn=$g_fb/Ceo0.xml ";
    $cmd .= " extOrgOut=$g_fb/Ceo1.xml ";

    my $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nSort results ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_rdf/isbyce.rdf ";
    $cmd .= " $g_xslts/rdfsort.xsl > $g_rdf/sisbyce.rdf ";

    $r = doit($cmd, $g_exef);
    exit(1) if($r);
    updateFeedbackFile('Ceo','ISCER');
}
############################################
#
# Accumulate and group all Impact Stmts by Funding Org by 
# combining data from all xmls
# formerly CFO
if($Phases{'ISFOC'}>0 || $g_all){
    print "\nPhase = ISFOC ================================\n";
    $g_curPhase = 'ISFOC';
#
#depends on $g_xslts/empty.xml, $g_is/aiislist.xml, $g_is/is-aiid-netid.xml
# $g_xslts/collectByFundingOrg.xsl, 
# $g_xslts/groupFundingOrgsImpactStmts.xsl
# $g_xslts/aiicsort.xsl    prior GX, IS
#
    print "\nCollect all Impact Stmts for each funding org from all $g_aiis xmls ...\n";
    my $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/collectByFundingOrg.xsl listxml=$g_is/aiislist.xml ";
    $cmd .= " aiid2netid=$g_is/is-aiid-netid.xml > $g_is/cfo.xml ";
    my $r;
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nIdentify impact stmts mentioned in multiple AIIS xmls ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_is/cfo.xml ";
    $cmd .= " $g_xslts/groupFundingOrgsImpactStmts.xsl > $g_is/cfod.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    $cmd = "";
    print "\nSort impact stmts by Activity Insight id ...\n";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_is/cfod.xml ";
    $cmd .= " $g_xslts/aiicsort.xsl > $g_is/cfods.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);
}
# Construct rdf for impact statement funding org
# formerly MFOR
if($Phases{'ISFOR'}>0 || $g_all){
    print "\nPhase = ISFOR ================================\n";
    print "\nConstruct a (sufficiently) large number of unique ";
    print "strings for impact stmt rdf process\n";
    $g_curPhase = 'ISFOR';
#
# depends on $g_is/cced.xml, $g_is/cfods.xml,
# $g_xslts/mkFundingOrgRdf.xsl $g_xslts/rdfsort.xsl
# $g_store/all-curOrgs.xml
# 
# 
#
    mkUnoFile("$g_is/cfods.xml","counter","$g_is/isfor-unomap.xml");


    initFeedbackFile('Org','ISFOR');

    print "\nConstruct the rdf for funding orgs and impact stmts\n";

    my $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence ";
    $cmd .= " -o $g_rdf/isbyfo.rdf $g_is/cfods.xml ";
    $cmd .= " $g_xslts/mkFundingOrgRdf.xsl ";
    $cmd .= " unoMapFile=$g_is/isfor-unomap.xml ";
    $cmd .= " aiisXmlPath=$g_is_xmls_out aiisPrefix=$g_aiis\_ ";
    $cmd .= " extFOrgIn=$g_fb/Org0.xml ";
    $cmd .= " extFOrgOut=$g_fb/Org1.xml ";

    my $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nSort results ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_rdf/isbyfo.rdf ";
    $cmd .= " $g_xslts/rdfsort.xsl > $g_rdf/sisbyfo.rdf ";

    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    updateFeedbackFile('Org', 'ISFOR');

}
############################################
#
# Accumulate and group all Impact Stmts by Geo Location by 
# combining data from all xmls
# formerly CGEO
if($Phases{'ISGEOC'}>0 || $g_all){
    print "\nPhase = ISGEOC ================================\n";
    $g_curPhase = 'ISGEOC';
#
#depends on $g_xslts/empty.xml, $g_is/aiislist.xml, $g_is/is-aiid-netid.xml
# $g_xslts/collectByGeoLocation.xsl, 
# $g_xslts/groupGeoLocationImpactStmts.xsl
# $g_xslts/aiicsort.xsl    prior GX, IS
#
    print "\nCollect all Impact Stmts for each Geo location from all $g_aiis xmls ...\n";
    my $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/collectByGeoLocation.xsl listxml=$g_is/aiislist.xml ";
    $cmd .= " aiid2netid=$g_is/is-aiid-netid.xml > $g_is/cgeo.xml ";
    my $r;
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nIdentify impact stmts mentioned in multiple AIIS xmls ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_is/cgeo.xml ";
    $cmd .= " $g_xslts/groupGeoLocationImpactStmts.xsl > $g_is/cgeod.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    $cmd = "";
    print "\nSort impact stmts by Activity Insight id ...\n";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_is/cgeod.xml ";
    $cmd .= " $g_xslts/aiicsort.xsl > $g_is/cgeods.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);
}
# Construct rdf for impact statement geo loc
# formerly MGR
if($Phases{'ISGEOR'}>0 || $g_all){
    print "\nPhase = ISGEOR ================================\n";
    print "\nConstruct a (sufficiently) large number of unique ";
    print "strings for impact stmt rdf process\n";
    $g_curPhase = 'ISGEOR';
#
# depends on $g_is/cgeod.xml, $g_is/cgeods.xml,
# $g_xslts/mkGeoLocRdf.xsl $g_xslts/rdfsort.xsl
# 
# 
# 
#
    mkUnoFile("$g_is/cgeods.xml","counter","$g_is/isgeor-unomap.xml");
    initFeedbackFile('Geo','ISGEOR');
    print "\nConstruct the rdf for geo locations and impact stmts\n";

    my $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence ";
    $cmd .= " -o $g_rdf/isbygeo.rdf $g_is/cgeods.xml ";
    $cmd .= " $g_xslts/mkGeoLocRdf.xsl ";
    $cmd .= " unoMapFile=$g_is/isgeor-unomap.xml ";
    $cmd .= " aiisXmlPath=$g_is_xmls_out aiisPrefix=$g_aiis\_ ";
    $cmd .= " extGeoIn=$g_fb/Geo0.xml ";
    $cmd .= " extGeoOut=$g_fb/Geo1.xml ";

    my $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nSort results ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_rdf/isbygeo.rdf ";
    $cmd .= " $g_xslts/rdfsort.xsl > $g_rdf/sisbygeo.rdf ";

    $r = doit($cmd, $g_exef);
    exit(1) if($r);
    updateFeedbackFile('Geo','ISGEOR');
}
############################################
#
# Accumulate and group all Impact Stmts by Priority Area by 
# combining data from all xmls
# formerly CPA
if($Phases{'ISPAC'}>0 || $g_all){
    print "\nPhase = ISPAC ================================\n";
    $g_curPhase = 'ISPAC';
#
#depends on $g_xslts/empty.xml, $g_is/aiislist.xml, $g_is/is-aiid-netid.xml
# $g_xslts/collectByPriorityArea.xsl, 
# $g_xslts/groupPriorityAreaImpactStmts.xsl
# $g_xslts/aiicsort.xsl    prior GX, IS
#
    print "\nCollect all Impact Stmts for each Priority Area from all $g_aiis xmls ...\n";
    my $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/collectByPriorityArea.xsl listxml=$g_is/aiislist.xml ";
    $cmd .= " aiid2netid=$g_is/is-aiid-netid.xml > $g_is/cpa.xml ";
    my $r;
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nIdentify impact stmts mentioned in multiple AIIS xmls ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_is/cpa.xml ";
    $cmd .= " $g_xslts/groupPriorityAreaImpactStmts.xsl > $g_is/cpad.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    $cmd = "";
    print "\nSort impact stmts by Activity Insight id ...\n";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_is/cpad.xml ";
    $cmd .= " $g_xslts/aiicsort.xsl > $g_is/cpads.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);
}

# Construct rdf for impact statement Priority  Area
# formerly MPAR
if($Phases{'ISPAR'}>0 || $g_all){
    print "\nPhase = ISPAR ================================\n";
    print "\nConstruct a (sufficiently) large number of unique ";
    print "strings for impact stmt rdf process\n";
    $g_curPhase = 'ISPAR';
#
# depends on $g_is/cpad.xml, $g_is/cpads.xml,
# $g_xslts/mkPriorityAreaRdf.xsl $g_xslts/rdfsort.xsl
# 
# 
# 
#
    mkUnoFile("$g_is/cpads.xml","counter","$g_is/ispar-unomap.xml");
    initFeedbackFile('Parea','ISPAR');
    print "\nConstruct the rdf for priority areas and impact stmts\n";

    my $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence ";
    $cmd .= " -o $g_rdf/isbypa.rdf $g_is/cpads.xml ";
    $cmd .= " $g_xslts/mkPriorityAreaRdf.xsl ";
    $cmd .= " unoMapFile=$g_is/ispar-unomap.xml ";
    $cmd .= " aiisXmlPath=$g_is_xmls_out aiisPrefix=$g_aiis\_ ";
    $cmd .= " extEmphIn=$g_fb/Parea0.xml ";
    $cmd .= " extEmphOut=$g_fb/Parea1.xml ";

    my $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nSort results ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_rdf/isbypa.rdf ";
    $cmd .= " $g_xslts/rdfsort.xsl > $g_rdf/sisbypa.rdf ";

    $r = doit($cmd, $g_exef);
    exit(1) if($r);
    updateFeedbackFile('Parea','ISPAR');
}
############################################
#
# Accumulate and group all Impact Stmts by Contribution Area by 
# combining data from all xmls
# formerly CCA
if($Phases{'ISCAC'}>0 || $g_all){
    print "\nPhase = ISCAC ================================\n";
    $g_curPhase = 'ISCAC';
#
#depends on $g_xslts/empty.xml, $g_is/aiislist.xml, $g_is/is-aiid-netid.xml
# $g_xslts/collectByConArea.xsl, 
# $g_xslts/groupConAreaImpactStmts.xsl
# $g_xslts/aiicsort.xsl    prior GX, IS
#
    print "\nCollect all Impact Stmts for each Contribution Area from all $g_aiis xmls ...\n";
    my $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/collectByConArea.xsl listxml=$g_is/aiislist.xml ";
    $cmd .= " aiid2netid=$g_is/is-aiid-netid.xml > $g_is/cca.xml ";
    my $r;
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nIdentify impact stmts mentioned in multiple AIIS xmls ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_is/cca.xml ";
    $cmd .= " $g_xslts/groupConAreaImpactStmts.xsl > $g_is/ccad.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    $cmd = "";
    print "\nSort impact stmts by Activity Insight id ...\n";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_is/ccad.xml ";
    $cmd .= " $g_xslts/aiicsort.xsl > $g_is/ccads.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);
}
# Construct rdf for impact statement Contribution  Area
# formerly MCAR
if($Phases{'ISCAR'}>0 || $g_all){
    print "\nPhase = ISCAR ================================\n";
    print "\nConstruct a (sufficiently) large number of unique ";
    print "strings for impact stmt rdf process\n";
    $g_curPhase = 'ISCAR';
#
#
# depends on $g_is/ccad.xml, $g_is/ccads.xml,
# $g_xslts/mkConAreaRdf.xsl $g_xslts/rdfsort.xsl
# $g_store/all-curConAreas.xml
# 
# 
#
    mkUnoFile("$g_is/ccads.xml","counter","$g_is/iscar-unomap.xml");
    initFeedbackFile('Carea','ISCAR');
    print "\nConstruct the rdf for conareas and impact stmts\n";

    my $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence ";
    $cmd .= " -o $g_rdf/isbyca.rdf $g_is/ccads.xml ";
    $cmd .= " $g_xslts/mkConAreaRdf.xsl ";
    $cmd .= " unoMapFile=$g_is/iscar-unomap.xml ";
    $cmd .= " aiisXmlPath=$g_is_xmls_out aiisPrefix=$g_aiis\_ ";
    $cmd .= " extConAreasIn=$g_fb/Carea0.xml ";
    $cmd .= " extConAreasOut=$g_fb/Carea1.xml ";

    my $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nSort results ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_rdf/isbyca.rdf ";
    $cmd .= " $g_xslts/rdfsort.xsl > $g_rdf/sisbyca.rdf ";

    $r = doit($cmd, $g_exef);
    exit(1) if($r);
    updateFeedbackFile('Carea','ISCAR');
}

############################################
#
# Accumulate and group all Impact Stmts by USDA Area by 
# combining data from all xmls
# formerly CUA
if($Phases{'ISUAC'}>0 || $g_all){
    print "\nPhase = ISUAC ================================\n";
    $g_curPhase = 'ISUAC';
#
#depends on $g_xslts/empty.xml, $g_is/aiislist.xml, $g_is/is-aiid-netid.xml
# $g_xslts/collectByUsdaArea.xsl, 
# $g_xslts/groupUsdaAreaImpactStmts.xsl
# $g_xslts/aiicsort.xsl    prior GX, IS
#
    print "\nCollect all Impact Stmts for each USDA Area from all $g_aiis xmls ...\n";
    my $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/collectByUsdaArea.xsl listxml=$g_is/aiislist.xml ";
    $cmd .= " aiid2netid=$g_is/is-aiid-netid.xml > $g_is/cua.xml ";
    my $r;
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nIdentify impact stmts mentioned in multiple AIIS xmls ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_is/cua.xml ";
    $cmd .= " $g_xslts/groupUsdaAreaImpactStmts.xsl > $g_is/cuad.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

    $cmd = "";
    print "\nSort impact stmts by Activity Insight id ...\n";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_is/cuad.xml ";
    $cmd .= " $g_xslts/aiicsort.xsl > $g_is/cuads.xml";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);
}

# Construct rdf for impact statement USDA Area
# formerly MUAR
if($Phases{'ISUAR'}>0 || $g_all){
    print "\nPhase = ISUAR ================================\n";
    print "\nConstruct a (sufficiently) large number of unique ";
    print "strings for impact stmt rdf process\n";
    $g_curPhase = 'ISUAR';
#
# depends on $g_is/cuad.xml, $g_is/cuads.xml,
# $g_xslts/mkUsdaAreaRdf.xsl $g_xslts/rdfsort.xsl
# $g_store/all-curUsdaAreas.xml
# 
# 
#
    mkUnoFile("$g_is/cuads.xml","counter","$g_is/isuar-unomap.xml");
    initFeedbackFile('Uarea','ISUAR');
    print "\nConstruct the rdf for geo locations and impact stmts\n";

    my $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence ";
    $cmd .= " -o $g_rdf/isbyua.rdf $g_is/cuads.xml ";
    $cmd .= " $g_xslts/mkUsdaAreaRdf.xsl ";
    $cmd .= " unoMapFile=$g_is/isuar-unomap.xml ";
    $cmd .= " aiisXmlPath=$g_is_xmls_out aiisPrefix=$g_aiis\_ ";
    $cmd .= " extUsdaAreasIn=$g_fb/Uarea0.xml ";
    $cmd .= " extUsdaAreasOut=$g_fb/Uarea1.xml ";

    my $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print "\nSort results ...\n";
    $cmd = "";
    $cmd .= "java $g_javaopts $g_saxonCmdSequence $g_rdf/isbyua.rdf ";
    $cmd .= " $g_xslts/rdfsort.xsl > $g_rdf/sisbyua.rdf ";

    $r = doit($cmd, $g_exef);
    exit(1) if($r);
    updateFeedbackFile('Uarea','ISUAR');
}




1;

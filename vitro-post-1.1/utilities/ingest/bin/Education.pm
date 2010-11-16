############################################
#
# Accumulate and group all education data by ai id
# from all xmls
# formerly EDU
if($Phases{'EDC'}>0 || $g_all){
    print "\nPhase = EDC ================================\n";
    $g_curPhase = 'EDC';
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/education.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_edu/ceducation.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
}
############################################
# make rdf
# formerly MEDU
if($Phases{'EDR'}>0 || $g_all){
    print "\nPhase = EDR ================================\n";
    $g_curPhase = 'EDR';
    mkUnoFile("$g_edu/ceducation.xml", "index", 
	      "$g_edu/edr-unomap.xml","AI-EDR-",$op_uno);
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence $g_edu/ceducation.xml ";
    $cmd .= "$g_xslts/mkEducationRdf.xsl unoMapFile=$g_edu/edr-unomap.xml ";
    $cmd .= " knownDegrees=$g_edu/KnownVivoDegrees.xml  ";
    $cmd .= "rawXmlPath=$g_xmls_raw > $g_rdf/education.rdf";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

}

############################################
#
# Accumulate and group all education person data by name (parts)
# from all xmls
# formerly EDUP
if($Phases{'EDPC'}>0 || $g_all){
    print "\nPhase = EDPC ================================\n";
    $g_curPhase = 'EDPC';
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/educationPerson.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_edu/ceducationPerson.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
}
############################################
# make rdf
# formerly MEDUP
if($Phases{'EDPR'}>0 || $g_all){
    print "\nPhase = EDPR ================================\n";
    $g_curPhase = 'EDPR';
    initFeedbackFile('Per','PEDPR');
    mkUnoFile("$g_edu/ceducationPerson.xml", 
	      "index", "$g_edu/edpr-unomap.xml",
	      "AI-EDPR-","$g_store/.Person");
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence ";
    $cmd .= " -o $g_rdf/educationPerson.rdf ";
    $cmd .= " $g_edu/ceducationPerson.xml ";
    $cmd .= " $g_xslts/mkEducationPersonRdf.xsl ";
    $cmd .= " unoMapFile=$g_edu/edpr-unomap.xml ";
    $cmd .= " rawXmlPath=$g_xmls_raw ";
    $cmd .= " extPerIn=$g_fb/Per0.xml ";
    $cmd .= " extPerOut=$g_fb/Per1.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

   updateFeedbackFile('Per', 'PEDPR');
}

############################################
#
# Accumulate and group all education person data by name (parts)
# from all xmls
# formerly EDUO
if($Phases{'EDOC'}>0 || $g_all){
    print "\nPhase = EDOC ================================\n";
    $g_curPhase = 'EDOC';
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/educationOrg.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_edu/ceducationOrg.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
}
############################################
# make rdf
# formerly MEDUO
if($Phases{'EDOR'}>0 || $g_all){
    print "\nPhase = EDOR ================================\n";
    $g_curPhase = 'EDOR';
    mkUnoFile("$g_edu/ceducationOrg.xml", 
	      "index", "$g_edu/edor-unomap.xml",
	      "AI-EDOR-","$g_store/.Org");
    initFeedbackFile('Org','OEDOR');
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence ";
    $cmd .= " -o $g_rdf/educationOrg.rdf ";
    $cmd .= " $g_edu/ceducationOrg.xml ";
    $cmd .= " $g_xslts/mkEducationOrgRdf.xsl ";
    $cmd .= " unoMapFile=$g_edu/edor-unomap.xml ";
    $cmd .= " rawXmlPath=$g_xmls_raw ";
    $cmd .= " extOrgIn=$g_fb/Org0.xml ";
    $cmd .= " extOrgOut=$g_fb/Org1.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

    updateFeedbackFile('Org', 'OEDOR');
}



1;


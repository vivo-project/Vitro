############################################
#
# Accumulate and group all MEDCONT data by ai id
# from all xmls
# formerly MED
if($Phases{'MCC'}>0 || $g_all){
    print "\nPhase = MCC ================================\n";
    $g_curPhase = 'MCC';
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/admin.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_med/cmedcont.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
}
############################################
# make rdf
# formerly MMED
if($Phases{'MCR'}>0 || $g_all){
    print "\nPhase = MCR ================================\n";
    $g_curPhase = 'MCR';
    mkUnoFile("$g_med/cmedcont.xml", "index", "$g_med/mcr-unomap.xml");
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence $g_med/cmedcont.xml ";
    $cmd .= "$g_xslts/mkMedcontRdf.xsl unoMapFile=$g_med/mcr-unomap.xml ";
    $cmd .= "rawXmlPath=$g_xmls_raw > $g_rdf/medcont.rdf";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

}

############################################
#
# Accumulate and group all medcont person data by name (parts)
# from all xmls
# formerly MEDP
if($Phases{'MCPC'}>0 || $g_all){
    print "\nPhase = MCPC ================================\n";
    $g_curPhase = 'MCPC';
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/medcontPerson.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_med/cmedcontperson.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
}
############################################
# make rdf
# formerly MMEDP
if($Phases{'MCPR'}>0 || $g_all){
    print "\nPhase = MCPR ================================\n";
    $g_curPhase = 'MCPR';
    mkUnoFile("$g_med/cmedcontperson.xml", 
	      "index", "$g_med/mcpr-unomap.xml");
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence ";
    $cmd .= " -o $g_rdf/medcontPerson.rdf ";
    $cmd .= " $g_med/cmedcontperson.xml ";
    $cmd .= " $g_xslts/mkMedcontPersonRdf.xsl ";
    $cmd .= " unoMapFile=$g_med/mcpr-unomap.xml ";
    $cmd .= " rawXmlPath=$g_xmls_raw ";
    $cmd .= " extPerIn=$g_fb/Per0.xml ";
    $cmd .= " extPerOut=$g_fb/Per1.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

    updateFeedbackFile('Per', 'MCPR');
}


1;


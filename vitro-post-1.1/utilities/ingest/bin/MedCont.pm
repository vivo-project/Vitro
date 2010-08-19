############################################
#
# Accumulate and group all MEDCONT data by ai id
# from all xmls
#
if($Phases{'MED'}>0 || $g_all){
    print "\nPhase = MED ================================\n";
    $g_curPhase = 'MED';
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/admin.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_med/cmedcont.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
}
############################################
# make rdf
#
if($Phases{'MMED'}>0 || $g_all){
    print "\nPhase = MMED ================================\n";
    $g_curPhase = 'MMED';
    mkUnoFile("$g_med/cmedcont.xml", "index", "$g_med/med-unomap.xml");
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_med/cmedcont.xml ";
    $cmd .= "$g_xslts/mkMedcontRdf.xsl unoMapFile=$g_med/med-unomap.xml ";
    $cmd .= "rawXmlPath=$g_xmls_raw > $g_rdf/medcont.rdf";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

}

############################################
#
# Accumulate and group all medcont person data by name (parts)
# from all xmls
#
if($Phases{'MEDP'}>0 || $g_all){
    print "\nPhase = MEDP ================================\n";
    $g_curPhase = 'MEDP';
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/medcontPerson.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_med/cmedcontperson.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
}
############################################
# make rdf
#
if($Phases{'MMEDP'}>0 || $g_all){
    print "\nPhase = MMEDP ================================\n";
    $g_curPhase = 'MMEDP';
    mkUnoFile("$g_med/cmedcontperson.xml", 
	      "index", "$g_med/medp-unomap.xml");
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar ";
    $cmd .= " -o $g_rdf/medcontPerson.rdf ";
    $cmd .= " $g_med/cmedcontperson.xml ";
    $cmd .= " $g_xslts/mkMedcontPersonRdf.xsl ";
    $cmd .= " unoMapFile=$g_med/medp-unomap.xml ";
    $cmd .= " rawXmlPath=$g_xmls_raw ";
    $cmd .= " extPerIn=$g_fb/Per0.xml ";
    $cmd .= " extPerOut=$g_fb/Per1.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

   updateFeedbackFile('Per', 'MMEDP');
}


1;


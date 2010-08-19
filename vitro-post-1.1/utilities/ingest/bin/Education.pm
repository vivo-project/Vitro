############################################
#
# Accumulate and group all education data by ai id
# from all xmls
#
if($Phases{'EDU'}>0 || $g_all){
    print "\nPhase = EDU ================================\n";
    $g_curPhase = 'EDU';
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/education.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_edu/ceducation.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
}
############################################
# make rdf
#
if($Phases{'MEDU'}>0 || $g_all){
    print "\nPhase = MEDU ================================\n";
    $g_curPhase = 'MEDU';
    mkUnoFile("$g_edu/ceducation.xml", "index", "$g_edu/edu-unomap.xml");
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_edu/ceducation.xml ";
    $cmd .= "$g_xslts/mkEducationRdf.xsl unoMapFile=$g_edu/edu-unomap.xml ";
    $cmd .= "rawXmlPath=$g_xmls_raw > $g_rdf/education.rdf";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

}

############################################
#
# Accumulate and group all education person data by name (parts)
# from all xmls
#
if($Phases{'EDUP'}>0 || $g_all){
    print "\nPhase = EDUP ================================\n";
    $g_curPhase = 'EDUP';
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/educationPerson.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_edu/ceducationperson.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
}
############################################
# make rdf
#
if($Phases{'MEDUP'}>0 || $g_all){
    print "\nPhase = MEDUP ================================\n";
    $g_curPhase = 'MEDUP';
    mkUnoFile("$g_edu/ceducationperson.xml", 
	      "index", "$g_edu/edup-unomap.xml");
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar ";
    $cmd .= " -o $g_rdf/educationPerson.rdf ";
    $cmd .= " $g_edu/ceducationperson.xml ";
    $cmd .= " $g_xslts/mkEducationPersonRdf.xsl ";
    $cmd .= " unoMapFile=$g_edu/edup-unomap.xml ";
    $cmd .= " rawXmlPath=$g_xmls_raw ";
    $cmd .= " extPerIn=$g_fb/Per0.xml ";
    $cmd .= " extPerOut=$g_fb/Per1.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

   updateFeedbackFile('Per', 'MEDUP');
}


1;


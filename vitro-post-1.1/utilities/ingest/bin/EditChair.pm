############################################
#
# Accumulate and group all editor/chair data by ai id
# from all xmls
#
if($Phases{'EDT'}>0 || $g_all){
    print "\nPhase = EDT ================================\n";
    $g_curPhase = 'EDT';
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/editChair.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_edt/ceditchair.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
}
############################################
# make rdf
#
if($Phases{'MEDT'}>0 || $g_all){
    print "\nPhase = MEDT ================================\n";
    $g_curPhase = 'MEDT';
    mkUnoFile("$g_edt/ceditchair.xml", "index", "$g_edt/edt-unomap.xml");
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_edt/ceditchair.xml ";
    $cmd .= "$g_xslts/mkEditChairRdf.xsl unoMapFile=$g_edt/edt-unomap.xml ";
    $cmd .= "rawXmlPath=$g_xmls_raw > $g_rdf/editChair.rdf";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

}

############################################
#
# Accumulate and group all editor/chair person data by name (parts)
# from all xmls
#
if($Phases{'EDTP'}>0 || $g_all){
    print "\nPhase = EDTP ================================\n";
    $g_curPhase = 'EDTP';
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/editChairPerson.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_edt/ceditchairperson.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
}
############################################
# make rdf
#
if($Phases{'MEDTP'}>0 || $g_all){
    print "\nPhase = MEDTP ================================\n";
    $g_curPhase = 'MEDTP';
    mkUnoFile("$g_edt/ceditchairperson.xml", 
	      "index", "$g_edt/edtp-unomap.xml");
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar ";
    $cmd .= " -o $g_rdf/editChairPerson.rdf ";
    $cmd .= " $g_edt/ceditchairperson.xml ";
    $cmd .= " $g_xslts/mkEditChairPersonRdf.xsl ";
    $cmd .= " unoMapFile=$g_edt/edtp-unomap.xml ";
    $cmd .= " rawXmlPath=$g_xmls_raw ";
    $cmd .= " extPerIn=$g_fb/Per0.xml ";
    $cmd .= " extPerOut=$g_fb/Per1.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

   updateFeedbackFile('Per', 'MEDTP');
}


1;


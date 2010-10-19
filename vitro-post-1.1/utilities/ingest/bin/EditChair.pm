############################################
#
# Accumulate and group all editor/chair data by ai id
# from all xmls
# formerly EDT
if($Phases{'ECC'}>0 || $g_all){
    print "\nPhase = ECC ================================\n";
    $g_curPhase = 'ECC';
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/editChair.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_edt/ceditchair.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
}
############################################
# make rdf
#formerly MEDT
if($Phases{'ECR'}>0 || $g_all){
    print "\nPhase = ECR ================================\n";
    $g_curPhase = 'ECR';
    mkUnoFile("$g_edt/ceditchair.xml", "index", "$g_edt/ecr-unomap.xml");
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence $g_edt/ceditchair.xml ";
    $cmd .= "$g_xslts/mkEditChairRdf.xsl unoMapFile=$g_edt/ecr-unomap.xml ";
    $cmd .= "rawXmlPath=$g_xmls_raw > $g_rdf/editChair.rdf";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

}

############################################
#
# Accumulate and group all editor/chair person data by name (parts)
# from all xmls
# formerly EDTP
if($Phases{'ECPC'}>0 || $g_all){
    print "\nPhase = ECPC ================================\n";
    $g_curPhase = 'ECPC';
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/editChairPerson.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_edt/ceditchairperson.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
}
############################################
# make rdf
# formerly MEDTP
if($Phases{'ECPR'}>0 || $g_all){
    print "\nPhase = ECPR ================================\n";
    $g_curPhase = 'ECPR';
    mkUnoFile("$g_edt/ceditchairperson.xml", 
	      "index", "$g_edt/ecpr-unomap.xml");
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence ";
    $cmd .= " -o $g_rdf/editChairPerson.rdf ";
    $cmd .= " $g_edt/ceditchairperson.xml ";
    $cmd .= " $g_xslts/mkEditChairPersonRdf.xsl ";
    $cmd .= " unoMapFile=$g_edt/ecpr-unomap.xml ";
    $cmd .= " rawXmlPath=$g_xmls_raw ";
    $cmd .= " extPerIn=$g_fb/Per0.xml ";
    $cmd .= " extPerOut=$g_fb/Per1.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

   updateFeedbackFile('Per', 'ECPR');
}


1;


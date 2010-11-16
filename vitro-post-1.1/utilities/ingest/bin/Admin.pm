############################################
#
# Accumulate and group all ADMIN data by ai id
# from all xmls
# formerly ADM
if($Phases{'ADC'}>0 || $g_all){
    print "\nPhase = ADC ================================\n";
    $g_curPhase = 'ADC';
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/admin.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_adm/cadmin.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
}
############################################
# make rdf
# formerly MADM
if($Phases{'ADR'}>0 || $g_all){
    print "\nPhase = ADR ================================\n";
    $g_curPhase = 'ADR';
    mkUnoFile("$g_adm/cadmin.xml", "index", 
	      "$g_adm/adr-unomap.xml","AI-ADR-",$op_uno);
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence $g_adm/cadmin.xml ";
    $cmd .= "$g_xslts/mkAdminRdf.xsl unoMapFile=$g_adm/adr-unomap.xml ";
    $cmd .= "rawXmlPath=$g_xmls_raw > $g_rdf/admin.rdf";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

}

############################################
#
# Accumulate and group all admin person data by name (parts)
# from all xmls
# formerly ADMP
if($Phases{'ADPC'}>0 || $g_all){
    print "\nPhase = ADPC ================================\n";
    $g_curPhase = 'ADPC';
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/adminPerson.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_adm/cadminperson.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
}
############################################
# make rdf
# formerly MADMP
if($Phases{'ADPR'}>0 || $g_all){
    print "\nPhase = ADPR ================================\n";
    $g_curPhase = 'ADPR';
    mkUnoFile("$g_adm/cadminperson.xml", 
	      "index", "$g_adm/adpr-unomap.xml",
	      "AI-ADPR-","$g_store/.Person");
    #initFeedbackFile('Per','ADPR');
    my $cmd = "";
    $cmd .= "java $g_saxonCmdSequence ";
    $cmd .= " -o $g_rdf/adminPerson.rdf ";
    $cmd .= " $g_adm/cadminperson.xml ";
    $cmd .= " $g_xslts/mkAdminPersonRdf.xsl ";
    $cmd .= " unoMapFile=$g_adm/adpr-unomap.xml ";
    $cmd .= " rawXmlPath=$g_xmls_raw ";
    $cmd .= " extPerIn=$g_fb/Per0.xml ";
    $cmd .= " extPerOut=$g_fb/Per1.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

   updateFeedbackFile('Per', 'ADPR');
}


1;


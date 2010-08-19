############################################
#
# Accumulate and group all ADMIN data by ai id
# from all xmls
#
if($Phases{'ADM'}>0 || $g_all){
    print "\nPhase = ADM ================================\n";
    $g_curPhase = 'ADM';
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/admin.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_adm/cadmin.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
}
############################################
# make rdf
#
if($Phases{'MADM'}>0 || $g_all){
    print "\nPhase = MADM ================================\n";
    $g_curPhase = 'MADM';
    mkUnoFile("$g_adm/cadmin.xml", "index", "$g_adm/adm-unomap.xml");
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_adm/cadmin.xml ";
    $cmd .= "$g_xslts/mkAdminRdf.xsl unoMapFile=$g_adm/adm-unomap.xml ";
    $cmd .= "rawXmlPath=$g_xmls_raw > $g_rdf/admin.rdf";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

}

############################################
#
# Accumulate and group all admin person data by name (parts)
# from all xmls
#
if($Phases{'ADMP'}>0 || $g_all){
    print "\nPhase = ADMP ================================\n";
    $g_curPhase = 'ADMP';
    mkListAllRaw();
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar $g_xslts/empty.xml ";
    $cmd .= " $g_xslts/adminPerson.xsl listxml=$g_store/nzraw.xml ";
    $cmd .= " > $g_adm/cadminperson.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
}
############################################
# make rdf
#
if($Phases{'MADMP'}>0 || $g_all){
    print "\nPhase = MADMP ================================\n";
    $g_curPhase = 'MADMP';
    mkUnoFile("$g_adm/cadminperson.xml", 
	      "index", "$g_adm/admp-unomap.xml");
    my $cmd = "";
    $cmd .= "java -jar $g_xslts/saxon9he.jar ";
    $cmd .= " -o $g_rdf/adminPerson.rdf ";
    $cmd .= " $g_adm/cadminperson.xml ";
    $cmd .= " $g_xslts/mkAdminPersonRdf.xsl ";
    $cmd .= " unoMapFile=$g_adm/admp-unomap.xml ";
    $cmd .= " rawXmlPath=$g_xmls_raw ";
    $cmd .= " extPerIn=$g_fb/Per0.xml ";
    $cmd .= " extPerOut=$g_fb/Per1.xml ";
    my $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);

   updateFeedbackFile('Per', 'MADMP');
}


1;


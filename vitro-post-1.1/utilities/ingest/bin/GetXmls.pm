############################################
############################################
#
# These two steps require a password 
#
if($Phases{'GU'}>0 || $Phases{'GX'}>0 || $g_all){
#
# get password
#
    print  CONSOLE $Phases{'GU'} . "\n" . $Phases{'GX'} . "\n";

    print  CONSOLE "Password: ";
    ReadMode 'noecho';
    $g_pw = ReadLine(0);
    print  CONSOLE "\n";
    chomp($g_pw);
    $g_pw =~ s/\$/\\\$/g;
    ReadMode 'normal';
    #print CONSOLE $g_pw . "\n";
}
############################################
#
# Get a User.xml from DM
#
if($Phases{'GU'}>0 || $g_all){
    print "\nPhase = GU ================================\n";
    $g_curPhase = 'GU';
#
# have password by this point
#
    print "Get Users list from Digital Measures...\n";
    my $cmd = "wget -O - --http-user cornell/fac_reports --http-passwd ";
    $cmd .= "_PW_ https://www.digitalmeasures.com/login/service/v4/User ";
    $cmd .= " > $g_store/Users.xml ";
    my $r;
    $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
    print "\nExtract netids from $g_store/Users.xml ...\n";
    $cmd = "$g_bin/xtract_netids $g_store/Users.xml  | sort | uniq  ";
    $cmd .= " > $g_store/ai_users.txt ";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);

}


############################################
#
# Get all the user xmls from DM and put them 
# in $g_xml_raw
#
if($Phases{'GX'}>0 || $g_all){
    print "\nPhase = GX ================================\n";
    $g_curPhase = 'GX';
#
# have password by this point
#
# depends on $g_store/ai_users.txt from GU

    print "\nGet xml for each netid and put them in $g_xmls_raw ...\n";
    my $cmd = "";
    $cmd .= "$g_bin/waiget -M -U $g_store/ai_users.txt -b $g_work -o $g_xmls_raw ";
    $cmd .= " -p _PW_ -m raw$g_TSR.md5 ";
    $cmd .= " -L $g_log_path " if $g_log_path ne '';
    my $r;
    $r = doit($cmd, $g_exef, $g_pw);
    exit(1) if($r);
    md5check() if $op_md5;
}

1;

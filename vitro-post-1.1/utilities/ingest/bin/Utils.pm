
sub Usage {
    print STDERR "Usage:\naiIngest\n";
    print STDERR "\t--aiic    prefix     a prefix string for Intellcont dir\n";
    print STDERR "\t                     of processed xmls. A subdirectory\n";
    print STDERR "\t                     of {xmldir}. default: AIIC\n";
    print STDERR "\t--aiicp   path       use this to directly identify aiic dir\n";
    print STDERR "\t--aiis    prefix     a prefix string for Impact Stmt dir\n";
    print STDERR "\t                     of processed xmls. A subdirectory\n";
    print STDERR "\t                     of {xmldir}. default: AIIS\n";
    print STDERR "\t--aiisp   path       use this to directly identify aiis dir\n";
    print STDERR "\t                     don't use/need other mechanisms\n";
    print STDERR "\t--bindir  path       path to bin dir containing\n";
    print STDERR "\t                     support scripts\n";
    print STDERR "\t--debug\n";
    print STDERR "\t--ic_xchain name     name of a file in {xsltdir} \n";
    print STDERR "\t                     listing a sequence of xslts to be\n";
    print STDERR "\t                     applied to all raw xmls\n";
    print STDERR "\t--is_xchain name     name of a file in {xsltdir} \n";
    print STDERR "\t                     listing a sequence of xslts to be\n";
    print STDERR "\t                     applied to all raw xmls\n";
    print STDERR "\t                     default: aiis-chain.xslc\n";
    print STDERR "\t--jopts   'opts'     java options e.g. -Xmx2048m \n";
    print STDERR "\t                     -XX:MaxPermSize=100m Use quotes\n";
    print STDERR "\t                     for multiple options\n";
    print STDERR "\t--L                  Live run. Actually execute\n";
    print STDERR "\t                     don't just list actions\n";
    print STDERR "\t--log                enable logging\n";
    print STDERR "\t--logdir  path       path to log dir. default \n";
    print STDERR "\t                     {workdir}/log\n";
#    print STDERR "\t--md5\n";
    print STDERR "\t--newrun             make new raw and aiic directories\n";
    print STDERR "\t--                   used inconjunction with\n";
    print STDERR "\t--raw     prefix     a prefix string for raw xml dir\n";
    print STDERR "\t                     name. A subdirectory of {xmldir} \n";
    print STDERR "\t                     default: AIRAW\n";
    print STDERR "\t--rawp    path       use this to directly identify raw dir\n";
    print STDERR "\t                     don't use/need other mechanisms\n";
    print STDERR "\t--reuse list         comma separated list of \n";
    print STDERR "\t                     {P-AR,P-ICAR,P-MIR,P-MAWR,P-MGRNT_INV\n";
    print STDERR "\t                      O-MFOR,O-MAWR,O-MGRNT_PFA,O-MGRNT_ARS,O-MGRNT_SPON}\n";
    print STDERR "\t--reuseAll           use current feedback files\n";
    print STDERR "\t--sparql             use sparql to initialize feedback files\n";
    print STDERR "\t--step[s] steplist   comma separated non-empty subset of\n";
    print STDERR "\t                     {\n";
    print STDERR "\t                       Get ops\n";
    print STDERR "\t                       GU,GX,\n";
    print STDERR "\t                       Intellectual Contribution ops\n";
    print STDERR "\t                       IC,CA,CJ,AR,JR,\n";
    print STDERR "\t                       CIA,ICAR,CIP,ICPR,\n";
    print STDERR "\t                       Impact Statement ops\n";
    print STDERR "\t                       IS,CINV,MIR, CCE,MCER, CFO,MFOR,\n";
    print STDERR "\t                       CGEO,MGR, CPA,MPAR, CUA,MUAR, CCA,MCAR,\n";
    print STDERR "\t                       Award ops\n";
    print STDERR "\t                       CAW,MAWR,\n";
    print STDERR "\t                       Grants and Contracts ops\n";
    print STDERR "\t                       GRNT,MGRNT, GRNT_INV,MGRNT_INV,\n";
    print STDERR "\t                       GRNT_SPON,MGRNT_SPON, GRNT_ARS,MGRNT_ARS,\n";
    print STDERR "\t                       GRNT_PFA,MGRNT_PFA, GRNT_PE,MGRNT_PE,\n";
    print STDERR "\t                       GRNT_COST,MGRNT_COST, GRNT_COMM,MGRNT_COMM,\n";
    print STDERR "\t                       \n";
    print STDERR "\t                     }\n";
    print STDERR "\t--store   path       same as --tmp \n";
    print STDERR "\t--tmp     path       path to process tmp store.\n";
    print STDERR "\t                     default: /tmp\n";
    print STDERR "\t--tsr     unit       unit 1of {day,hour,min}\n";
    print STDERR "\t--u                  show this usage info\n";
    print STDERR "\t--uno file           use file as uno repository\n";
    print STDERR "\t                     store/.Uno is default name.\n";
    print STDERR "\t--uts                use timestamp when naming xml\n";
    print STDERR "\t                     subdirectories. e.g \n";
    print STDERR "\t                         AIRAW_20100610120510 or\n";
    print STDERR "\t                         AII[CS]_20100610120510\n";
    print STDERR "\t--v                  verbose mode\n";
    print STDERR "\t--workdir path       path to work dir i.e. base directory\n";
    print STDERR "\t                     default: current directory '.' \n";
    print STDERR "\t--xchain  name       name of a file in {xsltdir} \n";
    print STDERR "\t                     listing a sequence of xslts to be\n";
    print STDERR "\t                     applied to all raw xmls\n";
    print STDERR "\t--xmldir  path       path to raw and processed xmls\n";
    print STDERR "\t                     default: {workdir}/AIXMLS\n";
    print STDERR "\t--xsltdir path       path to xslt directory containing\n";
    print STDERR "\t                     supporting .xsl files, etc\n";
    print STDERR "\t                     normally {workdir}/xslt\n";

    open(EXAMPLES, "$Bin/Examples.txt");
    while(my $line = <EXAMPLES>){
	print STDERR $line;
    }
    if(0){
	print STDERR "\t\nExamples:\n";
	print STDERR "\taiIngest --L --v --uts --steps GU,GX,IC,CA,CJ,CIA,AR,JR\n";
	print STDERR "\tpresumes that the current directory is the working\n";
	print STDERR "\tdirectory, this is a live verbose run and timestamp\n";
	print STDERR "\textensions are to be used when naming the xml directories\n";
	print STDERR "\t\n";
	print STDERR "\taiIngest --L --v --uts --log --steps GU,GX\n"; 
	print STDERR "\tlike the first only it includes a log and only does\n";
	print STDERR "\tthe first two steps.\n"; 
	print STDERR "\t\n";
	print STDERR "\taiIngest --L --v --aiic IC --steps GU,GX,IC,CA,CJ,CIA,AR,JR,TR,TX --log\n"; 
	print STDERR "\t         --jopts '-Xmx2048m -XX:MaxPermSize=100m'\n";
    }
    exit 0;

} 

sub trim {
    my($a) = @_;
    $a =~ s/^\s*(\S+)\s*$/$1/;
    $a =~ s/^\s*(\S.*\S)\s*$/$1/;
    $a =~ s/(\s*)//;
    return $a;
}



sub doit {
    my($cmd, $flag, $pw, $resp) = @_;
    #print "$cmd, $flag, $pw, $resp\n";
    print STDOUT "$cmd\n" if $flag =~ /V/;
    $cmd =~ s/_PW_/$pw/ if $pw;
    my $pkg,$fn,$lno;
    ($pkg,$fn,$lno) = caller();
    my $callinfo = "Pkg=$pkg, File=$fn, Line=$lno";
    if($flag =~ /L/ && $resp){
	@$resp = qx($cmd);
	my $r = ($? >> 8);
	print STDOUT ">>>> ERROR !!! $callinfo\n" . join("", @$resp) if($r);
	return $r;
    } elsif($flag =~ /L/){
	my @res = qx($cmd);
	my $r = ($? >> 8);
	print STDOUT ">>>> ERROR !!! $callinfo\n" . join("", @res) if($r);
	return $r;
    }
    return 0;
}

%TimeZero = ( 'Per' => 'personsAtTimeZero.xml' ,
	      'Org' => 'orgsAtTimeZero.xml'
	      );

%FeedbackFlags = ('Per' => 0, 'Org' => 0 );

sub initFeedbackFile{
    my($fn,$reuse)=@_;
    my $first = $TimeZero{$fn};
    my $file = $fn . "0.xml";

    #
    print STDOUT "\nInitializing $g_fb/$file ....\n";
    
    return if( -e  "$g_fb/$file" && $op_reuseAll);

    # e.g. if Per0.xml does not exist init Per0.xml or
    # if age of Per0.xml is newer than script start time
    # then init Per0.xml when --use-prev option
    #
    # NOTE: this will have to change for incremental updates
    if(($op_timeZero && ($FeedbackFlags{$fn} == 0)) || 
       ! -e "$g_fb/$file" || 
	(((stat("$g_fb/$file"))[9] < $g_T0) && 
	 ( $ReuseOutFiles{$reuse} <= 0 ))) {

	
	if($op_sparql && !$op_timeZero){
	    # nothing for now
	    ;
	} else {
	    print "\nSet up $file by making it a copy of $first.\n";
	    my $cmd = "";
	    $cmd .= "cp $g_fb/$first $g_fb/$file";
	    my $r = doit($cmd, $g_exef);
	    exit(1) if($r);
	}
    }
    # backup file on first use
    if( -e "$g_fb/$file" && $FeedbackFlags{$fn} == 0){
	print STDOUT "Saving initial version $g_fb/$file\n";
	my $tag = "AT_START_OF_RUN";
	my $dst =  "$g_fb/uri-maps/$fn"."0-$tag"."-$g_STARTED_AT.xml";
	qx(cp "$g_fb/$file" $dst);
	$FeedbackFlags{$fn} = 1;
    }
}

sub updateFeedbackFile {
    my($fn,$ilk) = @_;
    my $f0 = $fn . "0.xml";
    my $f1 = $fn . "1.xml";
    my $f2 = $fn . "2.xml";
    print STDOUT "\nUpdating $g_fb/$f0 ....\n";
    
    # e.g.
    # there may be individuals in Per0.xml that should still be available
    # for future matching. Per1.xml contains those individuals that
    # came from Per0.xml ( and match AI authors ) as well as new foaf:Person
    # individuals for those that did not match anything in Per0.xml.
    #
    #           Per2.xml = Per0.xml U Per1.xml   U = set union
    #
    print "\nMerge $f1 with $f0 to make $f2\n";
    my $cmd = "";

    $cmd .= "java $g_javaopts -jar $g_xslts/saxon9he.jar $g_fb/$f1 ";
    $cmd .= " $g_xslts/merge.xsl doc2=$g_fb/$f0 > $g_fb/$f2";

    my $r = doit($cmd, $g_exef);
    exit(1) if($r);

    print STDOUT "\nSaving $g_fb/$file etc ....\n";
    #
    my $now = strftime("%Y\%m%d%H\%M\%S", localtime() );
    $cmd = "";
    if($op_debug){
	$cmd .= " cp $g_fb/$f2 $g_fb/uri-maps/$fn"."2-$ilk\-At$now.xml; ";
	$cmd .= " cp $g_fb/$f1 $g_fb/uri-maps/$fn"."1-$ilk\-At$now.xml; ";
	$cmd .= " cp $g_fb/$f0 $g_fb/uri-maps/$fn"."0-$ilk\-At$now.xml; ";
    }
    $cmd .= " mv $g_fb/$f2 $g_fb/$f0";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);
}


sub mkUnoFile {
    my($collection,$str,$out)=@_;
    my $cmd = "grep $str $collection | wc -l ";

    my $r;
    my @res = ();
    $r = doit($cmd, $g_exef, undef, \@res);
    exit(1) if($r);
    my $ans = trim($res[0]);
    ($ans)=split /\s/, $ans;
    if($ans == 0){
	print STDOUT ">>>> ERROR !!!\nResult: $out will be empty!\n";
	exit 1;
    }
    $cmd = "$g_bin/nuno -X -n $ans -t AI-  > $out ";
    $r = doit($cmd, $g_exef);
    exit(1) if($r);
}

sub mkListAllRaw {
    if(!-e "$g_store/nzraw.xml"){
	my $cmd = "";
	$cmd .= "$g_bin/mklist -d $g_xmls_raw > $g_store/nzraw.xml";
	my $r = doit($cmd, $g_exef, $g_pw);
	exit(1) if($r);
    }
}


sub mkDirAsNeeded {
    my($d) = @_;
    return 0 if(-e $d);
    my @res = qx(mkdir $d);
    my  $r = ($? >> 8);
    print STDOUT join("", @res) . "\n" if($r);
    return $r;
}

sub mostRecentDirectory {
    my($dir,$prefix) = @_;
    $prefix .= "*";
    my @res = qx(cd $dir; ls -t1d $prefix); 
    foreach my $f (@res){
	chomp($f);
	if( -d "$dir/$f" ){
	    return $f;
	}
    }
    return '';
}

sub md5check {
    # get new md5 file and fix it if need be
    # cp to last raw dir
    qx(cp $g_xmls_raw/raw$g_TSR.md5 $g_xmls/prev-raw);
    

    # run md5sum --check 
    my @res = qx(cd $g_xmls/prev-raw; md5sum -c raw$g_TSR.md5);
    foreach my $rec (@res){
	# CASES to handle:
	# 'OK' - no change so drop new file
	# 'FAILED open or read' (expected a file to check)
	#        so this is an addition so keep new file
	# 'FAILED' - file changed (checksum changed)
	#        so this is an addition so keep new file
	my($f,$r) = map { trim($_); } split /:/, $rec;
	if($r eq 'OK'){
	    print STDOUT "qx(/bin/rm -f $g_xmls_raw/$f);\n";
	} elsif($r eq 'FAILED open or read'){
	    print STDOUT "# $g_xmls_raw/$f is new\n";
	} elsif($r eq 'FAILED'){
	    print STDOUT "# $g_xmls/prev-raw/$f has changed.\n" ;
	} else {
	    ;
	}
    }
    #
}

sub md5fix {
    my($p,$w) = @_;
    my $r = open INF, $p;
    if(!$r){
	print "Cannot open $p for read\n";
	return 0;
    }
    $r = open OUT, ">$w";
    if(!$r){
	print "Cannot open $w for write\n";
	return 0;
    }
    while( my $in = <INF> ){
	my(@parts,@comps);
	chomp($in);
	@parts = split /\s+/, $in;
	@comps = split /\//, $parts[1];
	#print "$in $parts[1]\n";
	if($parts[0] =~ /\*$/){
	    print OUT "$parts[0] $comps[$#comps]\n";
	} else {
	    print OUT "$parts[0]  $comps[$#comps]\n";
	}
    }
    close INF;
    close OUT;
    return 1;
}

sub clean {
    my $cmd = "";
    $cmd .= "cd $g_store; tar cvf $g_work/store$g_TSR.tar T[123]-*.xml ";
    $cmd .= " Users.xml ai_users.txt ";
    $cmd .= " aiiclist.xml aiid-netid.xml ";
    $cmd .= " ca.xml cad.xml cads.xml  ";
    $cmd .= " cj.xml cjd.xml cjds.xml ";
    $cmd .= " cica.xml cicad.xml ";
    $cmd .= " unomap.xml ";
    $cmd .= " ajnunos.xml ";
    qx($cmd);
    $cmd = "";
    $cmd .= "cd $g_store; /bin/rm -f T[123]-*.xml";
    qx($cmd);
    
}

sub depends {
    my(@files)=@_;
    my @res = ();
    foreach my $f (@files){
	chomp $f;
	next if( -e $f && -w _ & -r _);
	push @res, $f;
    }
    return @res;
}


1;

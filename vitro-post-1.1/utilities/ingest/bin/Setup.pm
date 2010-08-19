
@av = @ARGV;

$g_T0 = time();
$g_STARTED_AT = strftime("%Y\%m%d%H\%M\%S", localtime($g_T0) );

if(scalar(@ARGV) == 0 || $op_usage){
    Usage();
}
$g_cwd = $ENV{'PWD'};

GetOptions(
	   'aiic=s'      => \$op_aiic,
	   'aiicp=s'     => \$op_aiicpath,
	   'aiis=s'      => \$op_aiis,
	   'aiisp=s'     => \$op_aiispath,
	   'bindir=s'    => \$op_bindir,
	   'debug'       => \$op_debug,
	   'ic_xchain=s' => \$op_chain,
	   'is_xchain=s' => \$op_is_chain,
           'jopts=s'     => \@JavaOpts,
           'L'           => \$op_Live,
	   'log'         => \$op_log,
	   'logdir=s'    => \$op_logdir,
     	   'md5'         => \$op_md5,
	   'newrun'      => \$op_newrun,  
	   'raw=s'       => \$op_raw,
	   'rawp=s'      => \$op_rawpath,
	   'reuse=s'     => \@UsePrev,
	   'reuseAll'    => \$op_reuseAll,
	   'step=s'      => \@Steps,
	   'steps=s'     => \@Steps,
	   'store=s'     => \$opt_store,
	   'sparql'      => \$op_sparql,
	   'tmp=s'       => \$op_store,
	   'tsr=s'       => \$op_tsr,
	   't0'          => \$op_timeZero,
           'u'           => \$op_usage,
	   'uno=s'       => \$op_uno,
	   'uts'         => \$op_use_timestamp,
           'v'           => \$op_verb,
           'workdir=s'   => \$op_workdir,
	   'xchain=s'    => \$op_chain,
	   'xmldir=s'    => \$op_xmldir,
	   'xsltdir=s'   => \$op_xslts
);

$g_TSR = strftime("\%Y\%m\%d\%H\%M\%S", localtime());
if($op_tsr){
    if($op_tsr =~ /^day/i){
	$g_TSR = strftime("\%Y\%m\%d", localtime());
    } elsif ($op_tsr =~ /^hour/i){
	$g_TSR = strftime("\%Y\%m\%d\%H", localtime());
    } elsif ($op_tsr =~ /^min/){
	$g_TSR = strftime("\%Y\%m\%d\%H\%M", localtime());
    } else {
	$g_TSR = strftime("\%Y\%m\%d\%H\%M\%S", localtime());
    }
    $op_use_timestamp = 1;
}




# to maintain a way to write to
# good old stdout after this process
# redirects STDOUT to a log file
autoflush STDOUT 1;
open CONSOLE, ">&STDOUT";
autoflush CONSOLE 1;

END {
    my $err = $?;
    if($?){
	print CONSOLE "An Error has occurred.\n";
	print STDOUT "An Error has occurred.\n";
    } else {
	print CONSOLE "No Error has occurred.\n";
	print STDOUT "No Error has occurred.\n";
    }
    my $elapsed = time() - $g_T0;

    my $t1 = strftime("%Y\%m%d%H\%M\%S", localtime(time()));
    print STDOUT "Last phase attempted: $g_curPhase\n";
    print STDOUT "\nEnding at $t1. $elapsed seconds;\n";
    $? = $err;
}

$g_javaopts = " -Xmx1024m -Xms1024m ";
if(scalar(@JavaOpts) > 0){
    @JavaOpts = split(/[,;]/,join(',',@JavaOpts));
    $g_javaopts = ' ' . join(' ', @JavaOpts) . ' ';
} 

@Steps = split(/[,;]/,join(',',@Steps));
if(scalar(@Steps) == 0){
    $g_all = 'y';
} else {
    $g_all = '';
}

@UsePrev = split(/[,;]/,join(',',@UsePrev));
%ReuseOutFiles = ();
foreach my $item (@UsePrev){
    if($item =~ /^-/){
	$item =~ s/^-//;
	$ReuseOutFiles{uc($item)} = -1;
    } else {
	$ReuseOutFiles{uc($item)} = 1;
    }
}

$g_curPhase = "";
%Phases = ();
foreach my $item (@Steps){
    if($item =~ /^-/){
	$item =~ s/^-//;
	$Phases{uc($item)} = -1;
    } else {
	$Phases{uc($item)} = 1;
    }
}

$op_log = 0 if !$op_log;
$op_log = 1 if $op_logdir;
$op_workdir = $g_cwd if !$op_workdir;
$op_logdir = "$op_workdir/log" if !$op_logdir;

$op_store = "$op_workdir/store" if !$op_store;

$op_xmldir = "$op_workdir/AIXMLS" if !$op_xmldir;
$op_xslts = "$op_workdir/xslt" if !$op_xslts;
$op_bindir = "$op_workdir/bin" if !$op_bindir;
$op_raw = "AIRAW" if !$op_raw;
$g_raw = $op_raw;

$op_aiic = "AIIC" if !$op_aiic;
$g_aiic = $op_aiic;

$op_aiis = "AIIS" if !$op_aiis;
$g_aiis = $op_aiis;

$op_chain = "aiic-chain.xslc" if !$op_chain;
$g_chain = $op_chain;

$op_is_chain = "aiis-chain.xslc" if !$op_is_chain;
$g_is_chain = $op_is_chain;

$op_use_timestamp = "no" if ! $op_use_timestamp;

$g_store = $op_store;

$g_exef = "L" if $op_Live;
$g_exef .= "V" if $op_verb;

############################################
#
# working directory
if( ! -e $op_workdir ){
    doit("mkdir $op_workdir", "LV");
}
if( !(-e $op_workdir && -w _ && -d _)){
    print STDERR "Can't create or use working directory $op_workdir.\n";
    exit 1;
}
# make path absolute if not already
$g_work=$op_workdir;
$g_work = $g_cwd . '/' . $g_work if $g_work !~ /^\//;

############################################
#
# log directory
if($op_log){
    if( !(-e $op_logdir)){
	doit("mkdir $op_logdir",  "LV");
    }
    if( !(-e $op_logdir && -w _ && -d _)){
	print STDERR "Can't create or use log directory $op_logdir.\n";
	exit 1;
    }
# make path absolute if not already
    $g_log = $op_logdir;
    $g_log = $g_cwd . '/' . $g_log if $g_log !~ /^\//;
    
}
############################################
#
# xml parent directory
# make path absolute if not already
$g_xmls = $op_xmldir;
$g_xmls = $g_cwd . '/' . $g_xmls if $g_xmls  !~ /^\//;

if( ! -e $g_xmls ){
    doit("mkdir $g_xmls","LV" );
}
if( !(-e $g_xmls && -w _ && -d _)){
    print STDERR "Can't create or use xml directory $g_xmls.\n";
    exit 1;
}

############################################
#
# xml raw in directory
my @lnk = qx(readlink "$g_xmls/cur-raw");
my $curRaw = trim($lnk[0]);

if($op_rawpath){
    $g_xmls_raw = $op_rawpath;
} elsif($op_newrun || $curRaw  eq ''){
    $g_xmls_raw = "$g_xmls/$op_raw";
    $g_xmls_raw .= "_" . $g_TSR  if $op_use_timestamp;
} else {
    $g_xmls_raw = $curRaw;
    $g_xmls_raw = "$g_xmls/$g_xmls_raw" if $g_xmls_raw !~ /^\//;
}
# make path absolute if not already
$g_xmls_raw = $g_cwd . '/' . $g_xmls_raw if  $g_xmls_raw !~ /^\//;

if( !(-e $g_xmls_raw && -w _ && -d _)){
    print STDERR "Can't create or use raw xml in directory $g_xmls_raw.\n";
    exit 1;
}
if( ! -e  "$g_xmls/cur-raw"){
    doit("cd $g_xmls; ln -s $g_xmls_raw cur-raw", "LV");
}

############################################
#
# xml out directory finished process
@lnk = qx(readlink "$g_xmls/cur-aiic");
my $curOut = trim($lnk[0]);

if($op_aiicpath){
    $g_xmls_out = $op_aiicpath;
} elsif($op_newrun || $curOut  eq ''){
    $g_xmls_out = "$g_xmls/$op_aiic";
    $g_xmls_out .= "_" . $g_TSR  if $op_use_timestamp;
} else {
    $g_xmls_out = $curOut;
    $g_xmls_out = "$g_xmls/$g_xmls_out" if $g_xmls_out !~ /^\//;
}
# make path absolute if not already
$g_xmls_out = $g_cwd . '/' . $g_xmls_out if  $g_xmls_out !~ /^\//;

if( !(-e $g_xmls_out && -w _ && -d _)){
    print STDERR "Can't create or use xml out directory $g_xmls_out.\n";
    exit 1;
}
if( ! -e "$g_xmls/cur-aiic" && -e $g_xmls_out){
    doit("cd $g_xmls; ln -s $g_xmls_out cur-aiic", "LV");
}


############################################
#
# xml out directory finished process
@lnk = qx(readlink "$g_xmls/cur-aiis");
$curOut = trim($lnk[0]);

if($op_aiispath){
    $g_is_xmls_out = $op_aiispath;
} elsif($op_newrun || $curOut  eq ''){
    $g_is_xmls_out = "$g_xmls/$op_aiis";
    $g_is_xmls_out .= "_" . $g_TSR  if $op_use_timestamp;
} else {
    $g_is_xmls_out = $curOut;
    $g_is_xmls_out = "$g_xmls/$g_is_xmls_out" if $g_is_xmls_out !~ /^\//;
}
# make path absolute if not already
$g_is_xmls_out = $g_cwd . '/' . $g_is_xmls_out if  $g_is_xmls_out !~ /^\//;

if( !(-e $g_is_xmls_out && -w _ && -d _)){
    print STDERR "Can't create or use xml out directory $g_is_xmls_out.\n";
    exit 1;
}

if( ! -e "$g_xmls/cur-aiis" && -e $g_is_xmls_out){
    doit("cd $g_xmls; ln -s $g_is_xmls_out cur-aiis", "LV");
}


############################################
#
# gotta be able to read xslts
if( !(-e $op_xslts && -r _ && -d _)){
    print STDERR "Can't find xslt directory $op_xslts.\n";
    print STDERR "Provide a path to an xslt directory using --xsltdir option\n";
    exit 1;
}
# make path absolute if not already
$g_xslts = $op_xslts;
$g_xslts = $g_cwd . '/' . $g_xslts if  $g_xslts !~ /^\//;

############################################
#
#gotta have an empty.xml in xslts directory
if( !(-e "$op_xslts/empty.xml" && -r _ )){
    print STDERR "Can't find empty.xml in xslt directory $op_xslts.\n";
    print STDERR "Provide such a file.\n";
    exit 1;
}

############################################
#
#gotta have saxon jar in xslts directory
if( !(-e "$op_xslts/saxon9he.jar" && -r _ )){
    print STDERR "Can't find saxon9he.jar in xslt directory $op_xslts.\n";
    print STDERR "Provide such a file.\n";
    exit 1;
}

############################################
#
# gotta be able to read bin 
if( !(-e $op_bindir && -r _ && -d _)){
    print STDERR "Can't find bin directory $op_bindir.\n";
    print STDERR "Provide a path to a bin directory using --bindir option\n";
    exit 1;
}

# make path absolute if not already
$g_bin = $op_bindir;
$g_bin = $g_cwd . '/' . $g_bin if $g_bin !~ /^\//;

############################################
#
# gotta be able to read/write store dir
if( ! -e $op_store){
    doit("mkdir $op_store", "LV");
}
if( !(-e $op_store && -r _ && -d _ && -w _)){
    print STDERR "Can't create or use store directory $op_store.\n";
    exit 1;
}
# make path absolute if not already
$g_store = $g_cwd . '/' . $g_store if $g_store !~ /^\//;
if( ! -e "$g_store/uri-maps"){
    qx(mkdir $g_store/uri-maps);
}
if($op_uno && -e $op_uno && -f _ && -w _ && -r _){
    $g_uno = $op_uno;
} else {
    $g_uno = "$g_store/.Uno";
}
############################################
$g_log_path = "";
if($op_log) {
    my $flag = 0;
    if($g_log){
	$flag = open LOG, ">$g_log/aiIngest_$$\_$g_STARTED_AT";
	$g_log_path = "$g_log/aiIngest_$$\_$g_STARTED_AT";
    } else {
	$flag = open LOG, ">$g_log/log/aiIngest_$$\_$g_STARTED_AT";
	$g_log_path = "$g_log/log/aiIngest_$$\_$g_STARTED_AT";
    }
    
    if($flag){
	autoflush LOG 1;
	open(STDOUT,">&LOG");
	open(STDERR,">&LOG");
	print "Starting at $g_STARTED_AT\n";
	print "aiIngest " . join(' ',@av) . "\n";
autoflush STDOUT 1;
    } else {
	# write to display
	print "can't open $g_log\n";
	$g_log_path = "";
    }
}
print "\$op_newrun\t= $op_newrun\n";
print "\$g_work\t\t= $g_work\n";
print "\$g_log\t\t= $g_log\n" if $op_log;
print "\$g_xmls\t\t= $g_xmls\n";
print "\$g_xmls_raw\t= $g_xmls_raw\n";
print "\$g_xmls_out\t= $g_xmls_out\n";
print "\$g_is_xmls_out\t= $g_is_xmls_out\n";
print "\$g_xslts\t= $g_xslts\n";
print "\$g_bin\t\t= $g_bin\n";
print "\$g_store\t\t= $g_store\n";
print "\$op_rawp\t= $op_rawpath\n";
print "\$op_aiicp\t= $op_aiicpath\n";
print "\$op_aiisp\t= $op_aiispath\n";
print "\$op_raw\t\t= $op_raw \n";
print "\$op_aiic\t= $op_aiic\n";
print "\$op_aiis\t= $op_aiis\n";
print "\$op_chain\t= $op_chain\n";
print "\$op_is_chain\t= $op_is_chain\n";
print "\$op_use_timestamp = $op_use_timestamp\n";
print "\$op_tsr\t\t= $op_tsr\n";
print "\$g_TSR\t\t= $g_TSR\n";
print "\$g_javaopts\t= $g_javaopts\n";
print "\$g_exef\t\t= $g_exef\n";
print "\$op_Live\t= ". ($op_Live?"yes":"no") . "\n";
print "\$g_cwd\t\t= $g_cwd\n";
print "\$op_verb\t= $op_verb\n";
print "\$op_log\t\t= $op_log\n";
print "\$op_logdir\t= $op_logdir\n";
############################################
foreach my $i (@Steps){
    $i =~ s/^-//;
    print "$i -> $Phases{$i}\n";
}
#print CONSOLE $g_javaopts . "\n";

# sine qua non

exit 1 if(mkDirAsNeeded("$g_store/ic-aggregated"));
exit 1 if(mkDirAsNeeded("$g_store/is-aggregated"));
exit 1 if(mkDirAsNeeded("$g_store/aw-aggregated"));
exit 1 if(mkDirAsNeeded("$g_store/gr-aggregated"));
exit 1 if(mkDirAsNeeded("$g_store/edt-aggregated"));
exit 1 if(mkDirAsNeeded("$g_store/edu-aggregated"));
exit 1 if(mkDirAsNeeded("$g_store/med-aggregated"));
exit 1 if(mkDirAsNeeded("$g_store/adm-aggregated"));


exit 1 if(mkDirAsNeeded("$g_store/ic-digest"));
exit 1 if(mkDirAsNeeded("$g_store/is-digest"));
exit 1 if(mkDirAsNeeded("$g_store/aw-digest"));



exit 1 if(mkDirAsNeeded("$g_store/rdf"));
exit 1 if(mkDirAsNeeded("$g_store/feedback"));

$g_rdf = "$g_store/rdf";
$g_fb = "$g_store/feedback";


$g_ic = "$g_store/ic-aggregated";
$g_icd = "$g_store/ic-digest";
$g_is = "$g_store/is-aggregated";
$g_isd = "$g_store/is-digest";
$g_aw = "$g_store/aw-aggregated";
$g_gr = "$g_store/gr-aggregated";
$g_edt = "$g_store/edt-aggregated";
$g_edu = "$g_store/edu-aggregated";
$g_med = "$g_store/med-aggregated";
$g_adm = "$g_store/adm-aggregated";

1;

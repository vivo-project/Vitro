=begin
--------------------------------------------------------------------------------

This script will try to show that the JARs in a list are not needed for VIVO
(or Vitro).

Given a list of JARs, it will determine what packages the JAR contains, and will
search the source files of the project for references to those packages.

The list of JARs should be created by running the jarlist target of the build file.
That will use JarJar to create a list of JAR file that are not directly referenced
by the classes in the build. But what about JARs that are needed by a call using
reflection? Like Class.forName("this.that.theOther"). We know this is how the MySQL
driver is loaded (so we need mysql-connector-java-5.1.16-bin.jar). Are there any 
others? This script will try to find them.

The jarlist target includes a list of JARs that we know are needed, such as the MySQL
connector. If this script finds any JARs that are needed, they should be added to that
list, and the target run again, in case they depend on other JARs in turn.

--------------------------------------------------------------------------------

One of the tricks that this script uses is to prune the list of packages to 
search for. If a JAR contains both "this.that.other" and "this.that", then we
only need to search for "this.that", since it will reveal uses of "this.that.other"
as well.

--------------------------------------------------------------------------------

pass the name of the file that contains the JAR names
pass the root directory of the combined vitro/vivo distro 
	(appbase)
	
--------------------------------------------------------------------------------

Search all of the *.java, *.jsp, *.js, *.xml files for mention of these package names
	For each hit, print the file path, line number, the package name and the JAR name.
	
To search files:
	find -X . -name \*.db -or -name \*pot | xargs grep 'org\.package\.name'
	grep -H -n -f file_with_package_patterns
	
	or can we do this all with grep on each string?
		grep -r --include=*.jsp
		
	grep -H -n -r --include=*.javaxd --include=*.jsp org\.package\.name .
		and precede the output with a header that lists the package
	
--------------------------------------------------------------------------------
--------------------------------------------------------------------------------
=end

# Just say what we're doing.
#
def print_args
	puts "Scanning for JAR usage."
	puts "Base directory is #{@scan_base_directory}"
	puts "JAR list is in #{@jar_names_file}"
	puts
end

# Build a Hash of JAR names mapped to (reduced) arrays of package names.
#
def figure_package_names_from_jars
	@packages_for_jars = {}
	File.open(@jar_names_file) do |file|
		file.each do |line|
			jar_name = line.strip
			@packages_for_jars[jar_name] = figure_package_names_for_jar(jar_name)
		end
	end
end

# Figure out the reduced set of package names for this JAR
#
def figure_package_names_for_jar(jar_name)
	jar_path = "#{@scan_base_directory}/lib/#{jar_name}"
	jar_contents = `jar -tf '#{jar_path}'`
	packages = analyze_jar_contents(jar_contents)
	reduced = reduce_package_names(packages)
end

# Ask the JAR what it contains. Keep only the names of populated packages.
# Ignore packages that are not at least 2 levels deep.
#
def analyze_jar_contents(jar_contents)
	packages = []
	jar_contents.lines do |line|
		line.strip!
		if line.end_with?('/')
		elsif line.start_with?('META-INF')
		elsif line.count('/') < 2
		else
			package = line[0...line.rindex('/')].tr('/', '.')
			packages << package
		end
	end
	packages.uniq.sort!
end

# Remove the names of any sub-packages. Searching for the parent package will be sufficient.
#
def reduce_package_names(packages)
	reduced = []
	packages.each do |candidate|
		redundant = FALSE
		reduced.each do |result|
			redundant = TRUE if candidate.start_with?(result)
		end
		reduced << candidate unless redundant
	end
	reduced
end

# Show what packages we will search for, and for which JAR
#
def print_package_names_for_jars
	puts "Packages for each jar"
	@packages_for_jars.each do |jar_name, package_array|
		puts "   #{jar_name}"
		package_array.each do |package_name|
			puts "      #{package_name}"
		end
		puts 
	end
end

#
#
def show_packages_in_source_files
	@packages_for_jars.each do |jar_name, package_array|
		show_packages_for_jar_in_source_files(jar_name, package_array)
	end
end

#
#
def show_packages_for_jar_in_source_files(jar_name, package_array)
	puts "------------------------------- #{jar_name} ------------------------------"
	package_array.each do |package_name|
		show_package_in_source_files(package_name)
	end
end

#
#
def show_package_in_source_files(package_name)
	puts "#{package_name}"
	include_parms = build_include_parms(["*.java", "*.jsp", "*.xml", "*.js" ])
	package_name_pattern = package_name.sub(/\./, "\\.")
	system "grep -H -n -r #{include_parms} #{package_name_pattern} '#{@scan_base_directory}'"
	puts
end

#
#
def build_include_parms(file_specs)
	"--include=" + file_specs.join(" --include=")
end

#
#
# ------------------------------------------------------------------------------------
# Standalone calling.
#
# Do this if this program was called from the command line. That is, if the command
# expands to the path of this file.
# ------------------------------------------------------------------------------------
#

if File.expand_path($0) == File.expand_path(__FILE__)
	if ARGV.length != 2
		raise("usage is: ruby jarUsageScanner.rb <jar_names_file> <scan_base_directory>")
	end
  
	@jar_names_file, @scan_base_directory = ARGV
  
	if !File.file?(@jar_names_file)
		raise "File does not exist: '#{@jar_names_file}'."
	end

	if !File.directory?(@scan_base_directory)
		raise "Directory does not exist: '#{@scan_base_directory}'."
	end

	print_args
	
	figure_package_names_from_jars
	print_package_names_for_jars
	
	show_packages_in_source_files
end
=begin
--------------------------------------------------------------------------------

Scan the source directory, checking for expected "magic license tags",
         or
Copy the source directory, inserting licensing information into the files.

--------------------------------------------------------------------------------
--------------------------------------------------------------------------------
=end
$: << File.dirname(File.expand_path(__FILE__))
require 'date'
require 'fileutils'
require 'pathname'
require 'property_file_reader'
require 'licenser_stats'

class Licenser

  MAGIC_STRING = '$This file is distributed under the terms of the license in LICENSE$'

  # ------------------------------------------------------------------------------------
  private
  # ------------------------------------------------------------------------------------
  #
  # Some paths in the properties file, if they are relative, should be relative to the
  # properties file itself.
  def relative_to_properties(properties, key)
    path = properties[key]
    base = File.dirname(properties['properties_file_path'])

    return nil if path == nil
    return File.expand_path(path) if Pathname.new(path).absolute?
    return File.expand_path(File.join(base, path))
  end

  # Some paths in the properties file, if they are relative, should be relative to the
  # source directory.
  def relative_to_source(properties, key)
    path = properties[key]
    base = @source_dir ? @source_dir : ''

    return nil if path == nil
    return path if Pathname.new(path).absolute?
    return File.expand_path(File.join(base, path))
  end

  # Confirm that the parameters are reasonable.
  #
  def sanity_checks_on_parameters()
    # Check that all necessary properties are here.
    raise("Properties file must contain a value for 'source_dir'") if @source_dir == nil
    raise("Properties file must contain a value for 'known_exceptions'") if @known_exceptions_file == nil
    raise("Properties file must contain a value for 'skip_directories'") if @skip_directories_list == nil
    raise("Properties file must contain a value for 'file_matchers'") if @file_match_list == nil
    raise("Properties file must contain a value for 'report_level'") if @report_level_string == nil

    if !File.exist?(@source_dir)
      raise "Source directory does not exist: #{@source_dir}"
    end

    if !File.exist?(@known_exceptions_file)
      raise "Known exceptions file does not exist: #{@known_exceptions_file}"
    end
  end

  # The globs in the exceptions file are assumed to be
  # relative to the source directory. Make them explicitly so.
  #
  # Ignore any blank lines or lines that start with a '#'
  #
  def prepare_exception_globs(exceptions_file, source_dir)
    source_path = File.expand_path(source_dir)
    globs = []
    File.open(exceptions_file) do |file|
      file.each do |line|
        glob = line.strip
        if (glob.length > 0) && (glob[0..0] != '#')
          globs << "#{source_path}/#{glob}".gsub('//', '/')
        end
      end
    end
    return globs
  end

  # Recursively scan this directory, and copy if we are not scan-only.
  #
  def scan_dir(source_dir, target_dir)
    @stats.enter_directory(source_dir)

    Dir.foreach(File.join(@source_dir, source_dir)) do |filename|
      source_path_relative = File.join(source_dir, filename)
      source_path = File.join(@source_dir, source_path_relative)
      target_path_relative = File.join(target_dir, filename)
      target_path = File.join(@target_dir, target_path_relative)

      # What kind of beast is this?
      if filename == '.' || filename == '..'
        is_skipped_directory = true
      else
        if File.directory?(source_path)
          if (path_matches_skipped?(source_path_relative))
            is_skipped_directory = true
          else
            is_directory = true
          end
        else
          if filename_matches_pattern?(filename)
            if path_matches_exception?(source_path_relative)
              is_exception = true
            else
              is_match = true
            end
          else
            is_ignored = true
          end
        end
      end

      if is_skipped_directory
        # do nothing
      elsif is_directory
        scan_dir(source_path_relative, target_path_relative)
      elsif is_match
        @stats.record_scan_matching(filename)
        scan_file(source_path, filename)
      elsif is_exception
        @stats.record_known_exception(filename)
      else # not a match
        @stats.record_scan_non_matching(filename)
      end
    end
  end

  # Is this directory one of the skipped?
  #
  def path_matches_skipped?(relative_path)
    @skip_directories.each do |glob|
      return true if File.fnmatch(glob, relative_path)
    end
    return false
  end

  # Does this file path match any of the exceptions?
  #
  def path_matches_exception?(relative_path)
    path = File.expand_path(File.join(@source_dir, relative_path))
    @known_exceptions.each do |pattern|
      return true if File.fnmatch(pattern, path)
    end
    return false
  end

  # Does this filename match any of the patterns?
  #
  def filename_matches_pattern?(filename)
    @file_matchers.each do |pattern|
      return true if File.fnmatch(pattern, filename)
    end
    return false
  end

  # This file should contain a license tag.
  #
  def scan_file(source_path, filename)
    found = 0
    File.open(source_path) do |source_file|
      source_file.each do |line|
        if line.include?(MAGIC_STRING)
          found += 1
        end
      end
    end

    if found == 0
      @stats.record_no_tag(filename, source_path)
    elsif found == 1
      @stats.record_tag(filename)
    else
      raise("File contains #{found} license lines: #{source_path}")
    end
  end

  # ------------------------------------------------------------------------------------
  public
  # ------------------------------------------------------------------------------------

  # Setup and get ready to process.
  #
  # * properties is a map of keys to values, probably parsed from a properties file.
  #
  def initialize(properties)
    @file_match_list = properties['file_matchers']
    @skip_directories_list = properties['skip_directories']
    @report_level_string = properties['report_level']

    # These properties contain paths, and if they are relative paths, they
    # should be relative to the properties file itself.
    @source_dir = relative_to_properties(properties, 'source_dir')
    @target_dir = relative_to_properties(properties, 'target_dir')

    # These properties contain paths, and if they are relative paths, they
    # should be relative to the source_directory.
    @license_file = relative_to_source(properties, 'license_file')
    @known_exceptions_file = relative_to_source(properties, 'known_exceptions')

    sanity_checks_on_parameters()

    @full_report = @report_level_string === 'full'
    @short_report = @report_level_string === 'short'
    @file_matchers = @file_match_list.strip.split(/,\s*/)
    @skip_directories = @skip_directories_list.strip.split(/,\s*/)
    @known_exceptions = prepare_exception_globs(@known_exceptions_file, @source_dir)

    @stats = LicenserStats.new(@source_dir, @file_matchers, @full_report)
  end

  # Start the recursive scanning (and copying).
  def process()
    scan_dir('.', '.')
  end

  # Report the summary statistics
  def report(properties)
    if (@short_report)
      tags = 0
      @stats.tagged_files.each {|line| tags += line[1] }
      known = 0
      @stats.known_exceptions.each {|line| known += line[1] }
      missing = 0
      @stats.missing_tags.each {|line| missing += line[1] }

      puts "Licenser: scanned #{@stats.file_count} files in #{@stats.dir_count} directories."
      printf("   Licensed files:    %5d\n", tags)
      printf("   Known exceptions: %5d\n", known)
      printf("   Missing tags:     %5d\n", missing)
    else
      puts "Licenser: run completed at #{DateTime.now.strftime("%H:%M:%S on %b %d, %Y")}"
      puts "          scanned #{@stats.file_count} files in #{@stats.dir_count} directories."
      puts
      puts 'Licensed files'
      @stats.tagged_files.sort.each do |line|
        printf("%5d %s\n", line[1], line[0])
      end
      puts
      puts 'Known non-licensed files'
      @stats.known_exceptions.sort.each do |line|
        printf("%5d %s\n", line[1], line[0])
      end
      puts
      puts 'Missing tags'
      @stats.missing_tags.sort.each do |line|
        printf("%5d %s\n", line[1], line[0])
      end
      puts
      puts 'properties:'
      properties.each do |key, value|
        puts "    #{key} = #{value}"
      end
    end
  end

  # Were we successful or not?
  def success?
    return @stats.missing_tags.empty?
  end
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
  if ARGV.length == 0
    raise("No arguments - usage is: ruby licenser.rb <properties_file>")
  end
  if !File.file?(ARGV[0])
    raise "File does not exist: '#{ARGV[0]}'."
  end

  properties = PropertyFileReader.read(ARGV[0])

  l = Licenser.new(properties)
  l.process
  l.report(properties)

  if l.success?
    puts "Licenser was successful."
    exit 0
  else
    puts "Licenser found problems."
    exit 1
  end
end

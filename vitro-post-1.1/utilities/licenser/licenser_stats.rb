=begin
--------------------------------------------------------------------------------

Collect the statistics of a licenser run.

--------------------------------------------------------------------------------
=end

class LicenserStats
  attr_reader :substitutions
  attr_reader :missing_tags
  attr_reader :known_exceptions
  attr_reader :file_count
  attr_reader :dir_count

  # ------------------------------------------------------------------------------------
  private
  # ------------------------------------------------------------------------------------
  #
  def which_match(filename)
    @file_matchers.each do |matcher|
      return matcher if File.fnmatch(matcher, filename)
    end
    raise("filename matches no matchers!: #{filename}")
  end

  # ------------------------------------------------------------------------------------
  public
  # ------------------------------------------------------------------------------------

  def initialize(root_dir, file_matchers, full)
    @root_dir = "#{root_dir}/".gsub('//', '/')
    @file_matchers = file_matchers
    @full = full

    # keep track of how many substitutions for all file types
    @substitutions = Hash.new()
    file_matchers.each do |matcher|
      @substitutions[matcher] = 0
    end

    # keep track of missing tags, only in file types that have missing tags
    @missing_tags =  Hash.new(0)

    # keep track of how many known non-licensed files we encounter, and what types.
    @known_exceptions =  Hash.new(0)

    # keep track of how many files are copied
    @file_count = 0

    #keep track of how many directories are copied
    @dir_count = 0
  end

  def enter_directory(path)
    @dir_count += 1
    puts "Entering directory: #{path}" if @full
  end

  def record_scan_non_matching(filename)
    @file_count += 1
    puts "    Scan without mods: #{filename}" if @full
  end

  def record_copy_non_matching(filename)
    @file_count += 1
    puts "    Copy without mods: #{filename}" if @full
  end

  def record_scan_matching(filename)
    @file_count += 1
    puts "    Scan with mods: #{filename}" if @full
  end

  def record_copy_matching(filename)
    @file_count += 1
    puts "    Copy with mods: #{filename}" if @full
  end

  def record_known_exception(filename)
    @file_count += 1
    puts "    Known exception:              #{filename}" if @full
    @known_exceptions[which_match(filename)] += 1
  end

  def record_tag(filename)
    puts "    Substituted license text into #{filename}" if @full
    @substitutions[which_match(filename)] += 1
  end

  def record_no_tag(filename, source_path)
    puts "ERROR: Found no license tag in #{source_path.sub(@root_dir, '')}"
    @missing_tags[which_match(filename)] += 1
  end
end

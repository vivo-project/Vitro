/* $This file is distributed under the terms of the license in /doc/license.txt$ */

/**
 * <p>
 * The code in this package implements the Vitro file-storage system.
 * </p>
 * 
 * <h1>Relationship to PairTree</h1>
 * 
 * <p>
 * The system incorporates a number of ideas from the PairTree specification, 
 *   <ul>
 *     <li>
 *       The basic pairtree algorithm – 
 *       mapping an encoded identifier string into a filesystem directory path.
 *     </li>
 *     <li>
 *       Identifier string cleaning – 
 *       encoding identifiers in a two-step process so that all illegal 
 *       characters are eliminated, but some commonly-used 
 *       illegal characters are handled by simple substitution.
 *       Actually, it becomes a three-step process because namespaces are 
 *       invoved.
 *     </li>
 *   </ul> 
 * but is different in several respects:
 *   <ul>
 *     <li>
 *       Each "object" will consist only of a single file, 
 *       causing the entire issue of object encapsulation to be moot.
 *     </li>
 *     <li>
 *       Filenames will be cleaned in the same manner as identifiers, 
 *       guarding against illegal characters in filenames.
 *     </li>
 *     <li>
 *       Character encoding will include backslashes, 
 *       for compatibility with Windows.
 *     </li>
 *     <li>
 *       Character encoding will include tildes, to allow for "namespaces". 
 *     </li>
 *     <li>
 *       A namespace/prefix capability will be used to shorten file paths, 
 *       but with more flexibility than the prefix algorithm given in the specification.
 *     </li>
 *   </ul> 
 * </p>
 *
 * <h1>Directory structure</h1>
 * 
 * <p>
 *   A typical structure would look like this:
 *   <pre>
 *   + basedir
 *   |
 *   +--+ file_storage_namespaces.properties
 *   |
 *   +--+ file_storage_root
 *   </pre>
 *   The <code>file_storage_root</code> directory contains the subdirectories 
 *   that implement the encoded IDs, and the final directory for each ID will 
 *   contain a single file that corresponds to that ID. 
 * </p>
 * 
 * <h1>Namespaces</h1>
 * 
 * <p>
 *   To reduce the length of the file paths, the system will can be initialized
 *   to recognize certain sets of characters (loosely termed "namespaces") and
 *   to replace them with a given prefix and separator character during ID 
 *   encoding. 
 * </p>
 * <p>
 *   For example, the sytem might be initialized with a "namespace" of 
 *   "http://vivo.mydomain.edu/file/". If that is the only namespace, it will
 *   be internally assigned a prefix of "a", so a URI like this:
 *   <pre>http://vivo.mydomain.edu/file/n3424/myPhoto.jpg</pre>
 *   would be converted to this:
 *   <pre>a~n3424/myPhoto.jpg</pre>
 * </p>
 * <p>
 *   The namespaces and their assigned prefixes are stored in a properties file 
 *   when the structure is initialized. When the structure is re-opened, the
 *   file is read to find the correct prefixes. The file
 *   might look like this:
 *   <pre>
 *     a = http://the.first.namespace/
 *     b = http://the.second.namespace/
 *   </pre>
 * </p>
 * 
 * <h1>ID encoding</h1>
 * 
 * <p>
 * </p>
 * 
 * <h1>Filename encoding</h1>
 * 
 * <p>
 *   The name of the file is encoded as needed to guard against illegal 
 *   characters for the filesystem, but in practice we expect little encoding
 *   to be required, since few files are named with the special characters.
 * </p>
 * 
 */

package edu.cornell.mannlib.vitro.webapp.utils.filestorage;

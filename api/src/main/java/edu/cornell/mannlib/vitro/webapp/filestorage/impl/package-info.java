/**
 * <!-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
 * <p>
 * The code in this package implements the Vitro file-storage system.
 * </p>
 * <h1>Relationship to PairTree</h1>
 * <p>
 * The system incorporates a number of ideas from the PairTree specification,
 * </p>
 * <ul>
 * <li>
 * The basic pairtree algorithm -
 * mapping an encoded identifier string into a filesystem directory path.
 * </li>
 * <li>
 * Identifier string cleaning -
 * encoding identifiers in a two-step process so that all illegal
 * characters are eliminated, but some commonly-used
 * illegal characters are handled by simple substitution.
 * Actually, it becomes a three-step process because namespaces are
 * invoved.
 * </li>
 * </ul>
 * but is different in several respects:
 * <ul>
 * <li>
 * Each "object" will consist only of a single file,
 * causing the entire issue of object encapsulation to be moot.
 * </li>
 * <li>
 * Filenames will be cleaned in the same manner as identifiers,
 * guarding against illegal characters in filenames.
 * </li>
 * <li>
 * Character encoding will include backslashes,
 * for compatibility with Windows.
 * </li>
 * <li>
 * Character encoding will include tildes, to allow for "namespaces".
 * </li>
 * <li>
 * A namespace/prefix capability will be used to shorten file paths,
 * but with more flexibility than the prefix algorithm given in the specification.
 * </li>
 * <li>
 * "shorty" directory names may be up to 3 characters long, not 2.
 * </li>
 * </ul>
 * <h1>Directory structure</h1>
 * <p>
 * A typical structure would look like this:
 * {@code
 * + basedir
 * |
 * +--+ file_storage_namespaces.properties
 * |
 * +--+ file_storage_root
 * }
 * The {@code file_storage_root} directory contains the subdirectories
 * that implement the encoded IDs, and the final directory for each ID will
 * contain a single file that corresponds to that ID.
 * </p>
 * <h1>Namespaces</h1>
 * <p>
 * To reduce the length of the file paths, the system will can be initialized
 * to recognize certain sets of characters (loosely termed "namespaces") and
 * to replace them with a given prefix and separator character during ID
 * encoding.
 * </p>
 * <p>
 * For example, the sytem might be initialized with a "namespace" of
 * "http://vivo.mydomain.edu/file/". If that is the only namespace, it will
 * be internally assigned a prefix of "a", so a URI like this:
 * </p>
 * <pre>http://vivo.mydomain.edu/file/n3424/myPhoto.jpg</pre>
 * would be converted to this:
 * <pre>a~n3424/myPhoto.jpg</pre>
 * <p>
 * The namespaces and their assigned prefixes are stored in a properties file
 * when the structure is initialized. When the structure is re-opened, the
 * file is read to find the correct prefixes. The file
 * might look like this:
 * </p>
 * <pre>
 * a = http://the.first.namespace/
 * b = http://the.second.namespace/
 * </pre>
 * <h1>ID encoding</h1>
 * <p>
 * This is a multi-step process:
 * </p>
 * <ul>
 * <li>
 * <strong>Namespace recognition</strong> -
 * If the ID begins with a recognized namespace, then that namespace is
 * stripped from the ID, and the prefix associated with that namespace
 * is set aside for later in the process.
 * </li>
 * <li>
 * <strong>Rare character encoding</strong> -
 * Illegal characters are translated to their hexadecimal equivalents,
 * as are some rarely used characters which will be given other
 * purposes later in the process. The translated characters include any
 * octet outside of the visible ASCII range (21-7e), and these additional
 * characters:
 * <pre> " * + , &lt; = &gt; ? ^ | \ ~ </pre>
 * The hexadecimal encoding consists of a caret followed by 2 hex digits,
 * e.g.: ^7C
 * </li>
 * <li>
 * <strong>Common character encoding</strong> -
 * To keep the file paths short and readable, characters that are used
 * commonly in IDs but may be illegal in the file system are translated
 * to a single, lesser-used character.
 * <ul>
 * <li> / becomes = </li>
 * <li> : becomes + </li>
 * <li> . becomes , </li>
 * </ul>
 * </li>
 * <li>
 * <strong>Prefixing</strong> -
 * If a namespace was recognized on the ID in the first step, the
 * associated prefix letter will be prepended to the string, with a
 * tilde separator.
 * </li>
 * <li>
 * <strong>Path breakdown</strong> -
 * Path separator characters are inserted after every third character
 * in the processed ID string.
 * </li>
 * <li>
 * <strong>Exclusion of reserved Windows filenames</strong> -
 * Windows will not permit certain specific filename or directory names,
 * so if any part of the path would be equal to one of those reserved
 * names, it is prefixed with a tilde. The reserved names are:
 * CON, PRN, AUX, NUL, COM1, COM2, COM3, COM4, COM5, COM6, COM7, COM8,
 * COM9, LPT1, LPT2, LPT3, LPT4, LPT5, LPT6, LPT7, LPT8, and LPT9.
 * And remember, Windows is case-insensitive.
 * </li>
 * </ul>
 * Examples:
 * {@code ark:/13030/xt12t3} becomes {@code ark/+=1/303/0=x/t12/t3}
 * {@code http://n2t.info/urn:nbn:se:kb:repos-1} becomes {@code htt/p+=/=n2/t,i/nfo/=ur/n+n/bn+/se+/kb+/rep/os-/1}
 * {@code what-the-*@?#!^!~?} becomes {@code wha/t-t/he-/^2a/@^3/f#!/^5e/!^7/e^3/f}
 * {@code http://vivo.myDomain.edu/file/n3424} with namespace
 * {@code http://vivo.myDomain.edu/file/} and prefix
 * {@code a} becomes {@code a~n/342/4}
 * <h1>Filename encoding</h1>
 * <p>
 * The name of the file is encoded as needed to guard against illegal
 * characters for the filesystem, but in practice we expect little encoding
 * to be required, since few files are named with the special characters.
 * </p>
 * <p>
 * The encoding process is the same as the "rare character encoding" and
 * "common character encoding" steps used for ID encoding, except that
 * periods are not encoded.
 * </p>
 * <h2>
 * 	This was summarized in a post to the vivo-dev-all list on 11/29/2010
 * </h2>
 * <p>
 * 	The uploaded image files are identified by a combination of URI and filename.
 * 	The URI is used as the principal identifier so we don't need to worry about
 * 	collisions if two people each upload an image named "image.jpg".
 *
 * 	The filename is retained so the user can use their browser to download their
 * 	image from the system and it will be named as they expect it to be.
 * </p>
 * <p>
 * 	We wanted a way to store thousands of image files so they would not
 * 	all be in the same directory. We took our inspiration from the
 * 	<a href="https://confluence.ucop.edu/display/Curation/PairTree">PairTree</a>
 * 	folks, and modified their algorithm to suit our needs.
 *
 * 	The general idea is to store files in a multi-layer directory structure
 * 	based on the URI assigned to the file.
 * </p>
 * <p>
 * 	Let's consider a file with this information:
 * </p>
 * 	<pre>
 * 		URI = http://vivo.mydomain.edu/individual/n3156
 * 		Filename = lily1.jpg
 * 	</pre>
 * <p>
 * 	We want to turn the URI into the directory path, but the URI contains
 * 	prohibited characters. Using a PairTree-like character substitution,
 * 	we might store it at this path:
 * </p>
 * 	<pre>
 * 		/usr/local/vivo/uploads/file_storage_root/http+==vivo.mydomain.edu=individual=n3156/lily1.jpg
 * 	</pre>
 * <p>
 * 	Using that scheme would mean that each file sits in its own directory
 * 	under the storage root. At a large institution, there might be hundreds of
 * 	thousands of directories under that root.
 * </p>
 * <p>
 * 	By breaking this into PairTree-like groupings, we insure that all files
 * 	don't go into the same directory.
 *
 * 	Limiting to 3-character names will insure a maximum of about 30,000 files
 * 	per directory. In practice, the number will be considerably smaller.
 *
 * 	So then it would look like this:
 * </p>
 * 	<pre>
 * 		/usr/local/vivo/uploads/file_storage_root/htt/p+=/=vi/vo./myd/oma/in./edu/=in/div/idu/al=/n31/56/lily1.jpg
 * 	</pre>
 * <p>
 * 	But almost all of our URIs will start with the same namespace, so the
 * 	namespace just adds unnecessary and unhelpful depth to the directory tree.
 * 	We assign a single-character prefix to that namespace, using the
 * 	file_storage_namespaces.properties file in the uploads directory, like this:
 * </p>
 * 	<pre>
 * 		a = http://vivo.mydomain.edu/individual/
 * 	</pre>
 * 	And our URI now looks like this:
 * 	<pre>
 * 		a~n3156
 * 	</pre>
 * 	Which translates to:
 * 	<pre>
 * 		/usr/local/vivo/uploads/file_storage_root/a~n/315/6/lily1.jpg
 * 	</pre>
 * <p>
 * 	So what we hope we have implemented is a system where:
 * </p>
 * 	<ul>
 * 		<li>Files are stored by URI and filename.</li>
 * 		<li>File paths are constructed to limit the maximum number of files in a directory.</li>
 * 		<li>"Illegal" characters in URIs or filenames will not cause problems.
 * 			<ul><li>even if a character is legal on the client and illegal on the server.</li></ul>
 * 		</li>
 * 		<li>Frequently-used namespaces on the URIs can be collapsed to short prefix sequences.</li>
 * 		<li>URIs with unrecognized namespaces will not cause problems.</li>
 * 	</ul>
 * <p>
 * 	By the way, almost all of this is implemented in
 * </p>
 * 	<pre>
 * 		edu.cornell.mannlib.vitro.webapp.filestorage.impl.FileStorageHelper
 * 	</pre>
 * 	and illustrated in
 * 	<pre>
 * 		edu.cornell.mannlib.vitro.webapp.filestorage.impl.FileStorageHelperTest
 * 	</pre>
 */
package edu.cornell.mannlib.vitro.webapp.filestorage.impl;
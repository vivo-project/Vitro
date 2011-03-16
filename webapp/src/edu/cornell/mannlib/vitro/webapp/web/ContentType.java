package edu.cornell.mannlib.vitro.webapp.web;

/* Copyright (c) 2008 Google Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

/*
 * THIS CODE HAS BEEN MODIFIED:
 * The members of the Vitro/VIVO project have modified this code.  It has
 * been modified from the version produced by Google Inc.
 * 
 * The code in this file is from the Google data API project 1.40.3 on 2010-03-08.
 * See full license from gdata at bottom of this file.  
 */

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* Simple class for parsing and generating Content-Type header values, per
* RFC 2045 (MIME) and 2616 (HTTP 1.1).
*
* 
*/
public class ContentType implements Serializable {

 private static String TOKEN =
   "[\\p{ASCII}&&[^\\p{Cntrl} ;/=\\[\\]\\(\\)\\<\\>\\@\\,\\:\\\"\\?\\=]]+";

 // Precisely matches a token
 private static Pattern TOKEN_PATTERN = Pattern.compile(
   "^" + TOKEN + "$");

 // Matches a media type value
 private static Pattern TYPE_PATTERN = Pattern.compile(
   "(" + TOKEN + ")" +         // type  (G1)
   "/" +                       // separator
   "(" + TOKEN + ")" +         // subtype (G2)
   "\\s*(.*)\\s*", Pattern.DOTALL);

 // Matches an attribute value
 private static Pattern ATTR_PATTERN = Pattern.compile(
   "\\s*;\\s*" +
     "(" + TOKEN + ")" +       // attr name  (G1)
     "\\s*=\\s*" +
     "(?:" +
       "\"([^\"]*)\"" +        // value as quoted string (G3)
       "|" +
       "(" + TOKEN + ")?" +    // value as token (G2)
     ")"
   );

 /**
  * Name of the attribute that contains the encoding character set for
  * the content type.
  * @see #getCharset()
  */
 public static final String ATTR_CHARSET = "charset";

 /**
  * Special "*" character to match any type or subtype.
  */
 private static final String STAR = "*";

 /**
  * The UTF-8 charset encoding is used by default for all text and xml
  * based MIME types.
  */
 private static final String DEFAULT_CHARSET = ATTR_CHARSET + "=UTF-8";

 /**
  * A ContentType constant that describes the base unqualified Atom content
  * type.
  */
 public static final ContentType ATOM =
     new ContentType("application/atom+xml;" + DEFAULT_CHARSET).lock();

 /**
  * A ContentType constant that describes the qualified Atom entry content
  * type.
  *
  * @see #getAtomEntry()
  */
 public static final ContentType ATOM_ENTRY =
     new ContentType("application/atom+xml;type=entry;" + DEFAULT_CHARSET)
         .lock();

 /**
  * A ContentType constant that describes the qualified Atom feed content
  * type.
  *
  * @see #getAtomFeed()
  */
 public static final ContentType ATOM_FEED =
     new ContentType("application/atom+xml;type=feed;" + DEFAULT_CHARSET)
         .lock();

 /**
  * Returns the ContentType that should be used in contexts that expect
  * an Atom entry.
  */
 public static ContentType getAtomEntry() {
   // Use the unqualifed type for v1, the qualifed one for later versions
   //return Service.getVersion().isCompatible(Service.Versions.V1) ?
    //   ATOM : ATOM_ENTRY;
	 return ATOM_ENTRY;
 }

 /**
  * Returns the ContentType that should be used in contexts that expect
  * an Atom feed.
  */
 public static ContentType getAtomFeed() {
   // Use the unqualified type for v1, the qualified one for later versions
   //return Service.getVersion().isCompatible(Service.Versions.V1) ?
   //    ATOM : ATOM_FEED;	 
	 return ATOM_FEED;
 }

 /**
  * A ContentType constant that describes the Atom Service content type.
  */
 public static final ContentType ATOM_SERVICE =
     new ContentType("application/atomsvc+xml;" + DEFAULT_CHARSET).lock();

 /**
  * A ContentType constant that describes the RSS channel/item content type.
  */
 public static final ContentType RSS =
     new ContentType("application/rss+xml;" + DEFAULT_CHARSET).lock();

 /**
  * A ContentType constant that describes the JSON content type.
  */
 public static final ContentType JSON =
     new ContentType("application/json;" + DEFAULT_CHARSET).lock();

 /**
  * A ContentType constant that describes the Javascript content type.
  */
 public static final ContentType JAVASCRIPT =
     new ContentType("text/javascript;" + DEFAULT_CHARSET).lock();

 /**
  * A ContentType constant that describes the generic text/xml content type.
  */
 public static final ContentType TEXT_XML =
     new ContentType("text/xml;" + DEFAULT_CHARSET).lock();

 /**
  * A ContentType constant that describes the generic text/html content type.
  */
 public static final ContentType TEXT_HTML =
     new ContentType("text/html;" + DEFAULT_CHARSET).lock();

 /**
  * A ContentType constant that describes the generic text/plain content type.
  */
 public static final ContentType TEXT_PLAIN =
     new ContentType("text/plain;" + DEFAULT_CHARSET).lock();

 /**
  * A ContentType constant that describes the GData error content type.
  */
 public static final ContentType GDATA_ERROR =
     new ContentType("application/vnd.google.gdata.error+xml").lock();

 /**
  * A ContentType constant that describes the OpenSearch description document
  */
 public static final ContentType OPENSEARCH =
     new ContentType("application/opensearchdescription+xml").lock();
 

 /**
  * A ContentType constant that describes the MIME multipart/related content
  * type.
  */
 public static final ContentType MULTIPART_RELATED =
     new ContentType("multipart/related").lock();
 
 /**
  * A ContentType constant that describes the application/xml content
  * type.
  */
 public static final ContentType APPLICATION_XML =
     new ContentType("application/xml").lock();
 
 /**
  * A ContentType constant that indicates that the body contains an
  * encapsulated message, with the syntax of an RFC 822 email message.
  */
 public static final ContentType MESSAGE_RFC822 =
     new ContentType("message/rfc822").lock();
 
 /**
  * Wildcard content type that will match any MIME type
  */
 public static final ContentType ANY = new ContentType("*/*").lock();

 

 /**
  * A ContetType that describes RDF/XML.
  * Added by Brian Caruso for VIVO.
  */
 public final static ContentType RDFXML = new ContentType("application/rdf+xml").lock();
 
 /**
  * A ContetType that describes N3 RDF, this is unofficial and unregistered
  * Added by Brian Caruso for VIVO.
  */
 public final static ContentType N3 = new ContentType("text/n3").lock(); 
 
 /**
  * A ContetType that describes turtle RDF, this is unofficial and unregistered
  * Added by Brian Caruso for VIVO.
  */
 public final static ContentType TURTLE = new ContentType("text/turtle").lock(); 

 /**
  * Determines the best "Content-Type" header to use in a servlet response
  * based on the "Accept" header from a servlet request.
  *
  * @param acceptHeader       "Accept" header value from a servlet request (not
  *                           <code>null</code>)
  * @param actualContentTypes actual content types in descending order of
  *                           preference (non-empty, and each entry is of the
  *                           form "type/subtype" without the wildcard char
  *                           '*') or <code>null</code> if no "Accept" header
  *                           was specified
  * @return the best content type to use (or <code>null</code> on no match).
  */
 public static ContentType getBestContentType(String acceptHeader,
     List<ContentType> actualContentTypes) {

   // If not accept header is specified, return the first actual type
   if (acceptHeader == null) {
     return actualContentTypes.get(0);
   }

   // iterate over all of the accepted content types to find the best match
   float bestQ = 0;
   ContentType bestContentType = null;
   String[] acceptedTypes = acceptHeader.split(",");
   for (String acceptedTypeString : acceptedTypes) {

     // create the content type object
     ContentType acceptedContentType;
     try {
       acceptedContentType = new ContentType(acceptedTypeString.trim());
     } catch (IllegalArgumentException ex) {
       // ignore exception
       continue;
     }

     // parse the "q" value (default of 1)
     float curQ = 1;
     try {
       String qAttr = acceptedContentType.getAttribute("q");
       if (qAttr != null) {
         float qValue = Float.valueOf(qAttr);
         if (qValue <= 0 || qValue > 1) {
           continue;
         }
         curQ = qValue + 0.0001F;
       }
     } catch (NumberFormatException ex) {
       // ignore exception
       continue;
     }

     // only check it if it's at least as good ("q") as the best one so far
     if (curQ < bestQ) {
       continue;
     }

     /* iterate over the actual content types in order to find the best match
     to the current accepted content type */
     for (ContentType actualContentType : actualContentTypes) {

       /* if the "q" value is the same as the current best, only check for
       better content types */
       if (curQ == bestQ && bestContentType == actualContentType) {
         break;
       }

       /* check if the accepted content type matches the current actual
       content type */
       if (actualContentType.match(acceptedContentType)) {
         bestContentType = actualContentType;
         bestQ = curQ;
         break;
       }
     }
   }

   // if found an acceptable content type, return the best one
   if (bestQ != 0) {
     return bestContentType;
   }

   // Return null if no match
   return null;
 }

/**
 * Gets the best content type based weighted q from client accept header and 
 * the server weighted q of the extent that the type conveys the resource.
 * 
 * From suggestions by Tim Berners-Lee at http://www.w3.org/DesignIssues/Conneg
 * 
 * @param clentAcceptsTypes types the client can accept with Q weights.
 * @param serverTypes types the server can provide with Q weights.
 * @return returns content type of best match or null if no match.
 */
 public static String getBestContentType(
         Map<String, Float> clientAcceptsTypes,
         Map<String, Float> serverTypes) {
     float maxQ = 0.0f;
     String type = null;
     for( String serverType:  serverTypes.keySet()){
         float serverQ = serverTypes.get(serverType);
         Float clientQ = clientAcceptsTypes.get(serverType);
         if( clientQ != null && ((serverQ * clientQ)+ 0.001) > (maxQ + 0.001) ){
             maxQ = (serverQ * clientQ);
             type = serverType;
         }
     }     
     return type;
 }
 
 /**
  * This method was added by Brian Caruso of the VIVO project. March 15 2011.
  * 
  * @param acceptHeader
  * @return the types and the q values from the accept header
  */
 public static Map<String,Float> getTypesAndQ(String acceptHeader){
     if (acceptHeader == null) {
       return Collections.emptyMap();
     }

     Map<String,Float> qcMap = new HashMap<String,Float>();
     // iterate over all of the accepted content types
     String[] acceptedTypes = acceptHeader.split(",");
     for (String acceptedTypeString : acceptedTypes) {

       // create the content type object
       ContentType acceptedContentType;
       try {
         acceptedContentType = new ContentType(acceptedTypeString.trim());
       } catch (IllegalArgumentException ex) {
         // ignore exception
         continue;
       }

       // parse the "q" value (default of 1)
       float curQ = 1;
       try {
         String qAttr = acceptedContentType.getAttribute("q");
         if (qAttr != null) {
           float qValue = Float.valueOf(qAttr);
           if (qValue <= 0 || qValue > 1) {
             continue;
           }
           curQ = qValue + 0.0001F;
         }
       } catch (NumberFormatException ex) {
         // ignore exception
         continue;
       }
       
       if( acceptedContentType != null ){
           qcMap.put(acceptedContentType.getMediaType(), curQ);
       }
     }
     
     return qcMap;
 }
 
/**
  * Constructs a new instance with default media type
  */
 public ContentType() {
   this(null);
 }

 /**
  * Constructs a new instance from a content-type header value
  * parsing the MIME content type (RFC2045) format.  If the type
  * is {@code null}, then media type and charset will be
  * initialized to default values.
  *
  * @param typeHeader content type value in RFC2045 header format.
  */
 public ContentType(String typeHeader) {

   // If the type header is no provided, then use the HTTP defaults.
   if (typeHeader == null) {
     type = "application";
     subType = "octet-stream";
     attributes.put(ATTR_CHARSET, "iso-8859-1"); // http default
     return;
   }

   // Get type and subtype
   Matcher typeMatch = TYPE_PATTERN.matcher(typeHeader);
   if (!typeMatch.matches()) {
     throw new IllegalArgumentException("Invalid media type:" + typeHeader);
   }

   type = typeMatch.group(1).toLowerCase();
   subType = typeMatch.group(2).toLowerCase();
   if (typeMatch.groupCount() < 3) {
     return;
   }

   // Get attributes (if any)
   Matcher attrMatch = ATTR_PATTERN.matcher(typeMatch.group(3));
   while (attrMatch.find()) {

     String value = attrMatch.group(2);
     if (value == null) {
       value = attrMatch.group(3);
       if (value == null) {
         value = "";
       }
     }

     attributes.put(attrMatch.group(1).toLowerCase(), value);
   }

   // Infer a default charset encoding if unspecified.
   if (!attributes.containsKey(ATTR_CHARSET)) {
     inferredCharset = true;
     if (subType.endsWith("xml")) {
       if (type.equals("application")) {
         // BUGBUG: Actually have need to look at the raw stream here, but
         // if client omitted the charset for "application/xml", they are
         // ignoring the STRONGLY RECOMMEND language in RFC 3023, sec 3.2.
         // I have little sympathy.
         attributes.put(ATTR_CHARSET, "utf-8");    // best guess
       } else {
         attributes.put(ATTR_CHARSET, "us-ascii"); // RFC3023, sec 3.1
       }
     } else if (subType.equals("json")) {
       attributes.put(ATTR_CHARSET, "utf-8");    // RFC4627, sec 3
     } else {
       attributes.put(ATTR_CHARSET, "iso-8859-1"); // http default
     }
   }
 }

 /** {@code true} if parsed input didn't contain charset encoding info */
 private boolean inferredCharset = false;

 /** If set to {@code true}, the object is immutable. */
 private boolean locked;

 private String type;
 public String getType() { return type; }
 public void setType(String type) { 
   assertNotLocked();
   this.type = type; 
 }


 private String subType;
 public String getSubType() { return subType; }
 public void setSubType(String subType) { 
   assertNotLocked();
   this.subType = subType; 
 }

 /** Returns the full media type */
 public String getMediaType() {
   StringBuilder sb = new StringBuilder();
   sb.append(type);
   sb.append("/");
   sb.append(subType);
   if (attributes.containsKey("type")) {
     sb.append(";type=").append(attributes.get("type"));
   }
   return sb.toString();
 }

 private Float q=1.0f;
 
 private HashMap<String, String> attributes = new HashMap<String, String>();

 /**
  * Makes the object immutable and returns it.
  *
  * This should at least be used when keeping a {@link ContentType} instance as
  * a static.
  */
 public ContentType lock() {
   locked = true;
   return this;
 }

 private void assertNotLocked() {
   if (locked) {
     throw new IllegalStateException("Unmodifiable instance");
   }
 }

 /**
  * Returns the additional attributes of the content type.
  */
 public Map<String, String> getAttributes() { 
   if (locked) {
     return Collections.unmodifiableMap(attributes);
   } 
   return attributes; 
 }

 /**
  * Returns the additional attribute by name of the content type.
  *
  * @param name attribute name
  */
 public String getAttribute(String name) {
   return attributes.get(name);
 }

 /**
  * returns q associated with content type. 
  */
 public float getQ(){
     return q;
 }
 
 public void setQ(float q){
     this.q = q;
 }
 
 /*
  * Returns the charset attribute of the content type or null if the
  * attribute has not been set.
  */
 public String getCharset() { return attributes.get(ATTR_CHARSET); }


 /**
  * Returns whether this content type is match by the content type found in the
  * "Accept" header field of an HTTP request.
  *
  * <p>For atom content type, this method will check the optional attribute
  * 'type'. If the type attribute is set in both this and {@code
  * acceptedContentType}, then they must be the same. That is, {@code
  * application/atom+xml} will match both {@code
  * application/atom+xml;type=feed} and {@code
  * application/atom+xml;type=entry}, but {@code
  * application/atom+xml;type=entry} will not match {@code
  * application/atom+xml;type=feed}.a
  *
  * @param acceptedContentType content type found in the "Accept" header field
  *                            of an HTTP request
  */
 public boolean match(ContentType acceptedContentType) {
   String acceptedType = acceptedContentType.getType();
   String acceptedSubType = acceptedContentType.getSubType();
   return STAR.equals(acceptedType) || type.equals(acceptedType) 
       && (STAR.equals(acceptedSubType) || subType.equals(acceptedSubType)) 
       && (!isAtom() || matchAtom(acceptedContentType));
 }

 /** Returns true if this is an atom content type. */
 private boolean isAtom() {
   return "application".equals(type) && "atom+xml".equals(subType);
 }

 /** 
  * Compares the optional 'type' attribute of two content types.
  *
  * <p>This method accepts atom content type without the 'type' attribute
  * but if the types are specified, they must match.
  */
 private boolean matchAtom(ContentType acceptedContentType) {
   String atomType = getAttribute("type");
   String acceptedAtomType = acceptedContentType.getAttribute("type");

   return atomType == null || acceptedAtomType == null
       || atomType.equals(acceptedAtomType);
 }

 /**
  * Generates the Content-Type value
  */
 @Override
 public String toString() {

   StringBuffer sb = new StringBuffer();
   sb.append(type);
   sb.append("/");
   sb.append(subType);
   for (String name : attributes.keySet()) {

     // Don't include any inferred charset attribute in output.
     if (inferredCharset && ATTR_CHARSET.equals(name)) {
       continue;
     }
     sb.append(";");
     sb.append(name);
     sb.append("=");
     String value = attributes.get(name);
     Matcher tokenMatcher = TOKEN_PATTERN.matcher(value);
     if (tokenMatcher.matches()) {
       sb.append(value);
     } else {
       sb.append("\"" + value + "\"");
     }
   }
   return sb.toString();
 }


 @Override
 public boolean equals(Object o) {
   if (this == o) {
     return true;
   }
   if (o == null || getClass() != o.getClass()) {
     return false;
   }
   ContentType that = (ContentType) o;
   return type.equals(that.type) && subType.equals(that.subType) && attributes
       .equals(that.attributes);
 }


 @Override
 public int hashCode() {
   return (type.hashCode() * 31 + subType.hashCode()) * 31 + attributes
       .hashCode();
 }

}
/*


Apache License
Version 2.0, January 2004
http://www.apache.org/licenses/

TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION

1. Definitions.

"License" shall mean the terms and conditions for use, reproduction,
and distribution as defined by Sections 1 through 9 of this document.

"Licensor" shall mean the copyright owner or entity authorized by
the copyright owner that is granting the License.

"Legal Entity" shall mean the union of the acting entity and all
other entities that control, are controlled by, or are under common
control with that entity. For the purposes of this definition,
"control" means (i) the power, direct or indirect, to cause the
direction or management of such entity, whether by contract or
otherwise, or (ii) ownership of fifty percent (50%) or more of the
outstanding shares, or (iii) beneficial ownership of such entity.

"You" (or "Your") shall mean an individual or Legal Entity
exercising permissions granted by this License.

"Source" form shall mean the preferred form for making modifications,
including but not limited to software source code, documentation
source, and configuration files.

"Object" form shall mean any form resulting from mechanical
transformation or translation of a Source form, including but
not limited to compiled object code, generated documentation,
and conversions to other media types.

"Work" shall mean the work of authorship, whether in Source or
Object form, made available under the License, as indicated by a
copyright notice that is included in or attached to the work
(an example is provided in the Appendix below).

"Derivative Works" shall mean any work, whether in Source or Object
form, that is based on (or derived from) the Work and for which the
editorial revisions, annotations, elaborations, or other modifications
represent, as a whole, an original work of authorship. For the purposes
of this License, Derivative Works shall not include works that remain
separable from, or merely link (or bind by name) to the interfaces of,
the Work and Derivative Works thereof.

"Contribution" shall mean any work of authorship, including
the original version of the Work and any modifications or additions
to that Work or Derivative Works thereof, that is intentionally
submitted to Licensor for inclusion in the Work by the copyright owner
or by an individual or Legal Entity authorized to submit on behalf of
the copyright owner. For the purposes of this definition, "submitted"
means any form of electronic, verbal, or written communication sent
to the Licensor or its representatives, including but not limited to
communication on electronic mailing lists, source code control systems,
and issue tracking systems that are managed by, or on behalf of, the
Licensor for the purpose of discussing and improving the Work, but
excluding communication that is conspicuously marked or otherwise
designated in writing by the copyright owner as "Not a Contribution."

"Contributor" shall mean Licensor and any individual or Legal Entity
on behalf of whom a Contribution has been received by Licensor and
subsequently incorporated within the Work.

2. Grant of Copyright License. Subject to the terms and conditions of
this License, each Contributor hereby grants to You a perpetual,
worldwide, non-exclusive, no-charge, royalty-free, irrevocable
copyright license to reproduce, prepare Derivative Works of,
publicly display, publicly perform, sublicense, and distribute the
Work and such Derivative Works in Source or Object form.

3. Grant of Patent License. Subject to the terms and conditions of
this License, each Contributor hereby grants to You a perpetual,
worldwide, non-exclusive, no-charge, royalty-free, irrevocable
(except as stated in this section) patent license to make, have made,
use, offer to sell, sell, import, and otherwise transfer the Work,
where such license applies only to those patent claims licensable
by such Contributor that are necessarily infringed by their
Contribution(s) alone or by combination of their Contribution(s)
with the Work to which such Contribution(s) was submitted. If You
institute patent litigation against any entity (including a
cross-claim or counterclaim in a lawsuit) alleging that the Work
or a Contribution incorporated within the Work constitutes direct
or contributory patent infringement, then any patent licenses
granted to You under this License for that Work shall terminate
as of the date such litigation is filed.

4. Redistribution. You may reproduce and distribute copies of the
Work or Derivative Works thereof in any medium, with or without
modifications, and in Source or Object form, provided that You
meet the following conditions:

(a) You must give any other recipients of the Work or
Derivative Works a copy of this License; and

(b) You must cause any modified files to carry prominent notices
stating that You changed the files; and

(c) You must retain, in the Source form of any Derivative Works
that You distribute, all copyright, patent, trademark, and
attribution notices from the Source form of the Work,
excluding those notices that do not pertain to any part of
the Derivative Works; and

(d) If the Work includes a "NOTICE" text file as part of its
distribution, then any Derivative Works that You distribute must
include a readable copy of the attribution notices contained
within such NOTICE file, excluding those notices that do not
pertain to any part of the Derivative Works, in at least one
of the following places: within a NOTICE text file distributed
as part of the Derivative Works; within the Source form or
documentation, if provided along with the Derivative Works; or,
within a display generated by the Derivative Works, if and
wherever such third-party notices normally appear. The contents
of the NOTICE file are for informational purposes only and
do not modify the License. You may add Your own attribution
notices within Derivative Works that You distribute, alongside
or as an addendum to the NOTICE text from the Work, provided
that such additional attribution notices cannot be construed
as modifying the License.

You may add Your own copyright statement to Your modifications and
may provide additional or different license terms and conditions
for use, reproduction, or distribution of Your modifications, or
for any such Derivative Works as a whole, provided Your use,
reproduction, and distribution of the Work otherwise complies with
the conditions stated in this License.

5. Submission of Contributions. Unless You explicitly state otherwise,
any Contribution intentionally submitted for inclusion in the Work
by You to the Licensor shall be under the terms and conditions of
this License, without any additional terms or conditions.
Notwithstanding the above, nothing herein shall supersede or modify
the terms of any separate license agreement you may have executed
with Licensor regarding such Contributions.

6. Trademarks. This License does not grant permission to use the trade
names, trademarks, service marks, or product names of the Licensor,
except as required for reasonable and customary use in describing the
origin of the Work and reproducing the content of the NOTICE file.

7. Disclaimer of Warranty. Unless required by applicable law or
agreed to in writing, Licensor provides the Work (and each
Contributor provides its Contributions) on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied, including, without limitation, any warranties or conditions
of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A
PARTICULAR PURPOSE. You are solely responsible for determining the
appropriateness of using or redistributing the Work and assume any
risks associated with Your exercise of permissions under this License.

8. Limitation of Liability. In no event and under no legal theory,
whether in tort (including negligence), contract, or otherwise,
unless required by applicable law (such as deliberate and grossly
negligent acts) or agreed to in writing, shall any Contributor be
liable to You for damages, including any direct, indirect, special,
incidental, or consequential damages of any character arising as a
result of this License or out of the use or inability to use the
Work (including but not limited to damages for loss of goodwill,
work stoppage, computer failure or malfunction, or any and all
other commercial damages or losses), even if such Contributor
has been advised of the possibility of such damages.

9. Accepting Warranty or Additional Liability. While redistributing
the Work or Derivative Works thereof, You may choose to offer,
and charge a fee for, acceptance of support, warranty, indemnity,
or other liability obligations and/or rights consistent with this
License. However, in accepting such obligations, You may act only
on Your own behalf and on Your sole responsibility, not on behalf
of any other Contributor, and only if You agree to indemnify,
defend, and hold each Contributor harmless for any liability
incurred by, or claims asserted against, such Contributor by reason
of your accepting any such warranty or additional liability.

END OF TERMS AND CONDITIONS

APPENDIX: How to apply the Apache License to your work.

To apply the Apache License to your work, attach the following
boilerplate notice, with the fields enclosed by brackets "[]"
replaced with your own identifying information. (Don't include
the brackets!)  The text should be enclosed in the appropriate
comment syntax for the file format. We also recommend that a
file or class name and description of purpose be included on the
same "printed page" as the copyright notice for easier
identification within third-party archives.

Copyright [yyyy] [name of copyright owner]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
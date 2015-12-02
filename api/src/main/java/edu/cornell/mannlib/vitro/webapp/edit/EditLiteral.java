/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;/**
 * bdc34: I needed to have a representation of a RDF literal for
 * editing.  Jena seems to have a Model associated with the literals and
 * has depreciated the creation of simple Literals as if they were data
 * structures.  So this code was written.
 *
 * THESE MAY NOT BE USED AS LITERALS WITH THE JENA LIBRARY
 */
public class EditLiteral implements Literal {

    String value = null;
    String datatype =null;
    String lang =null;

    public EditLiteral(String value, String datatype, String lang){
        //both datatype and lang set is not suppored in jena2
//        if( lang != null && datatype != null)
//            throw new IllegalArgumentException("a literal cannot have a lang and a datatype");

        this.value= value;
        this.datatype = datatype;
        this.lang = lang;
    }

     public Object getValue() {
        return value;
    }

    public String getDatatypeURI() {
        return datatype;
    }

    public String getLexicalForm() {
//        if( lang != null && lang.length() > 0)
//            return '"' + value + "\"@" + lang;
//        else if ( datatype != null && datatype.length() > 0 )
//            return '"' + value + "\"^^<" + datatype + '>';
//        else
//            return '"' + value + '"';
        return value;
    }

    public boolean isLiteral() {
        return true;
    }

    public String getLanguage() {
        return lang;
    }


    public String getString(){
        return value;
    }

    public String toString(){
        return "value: "+value+"\ndatatype: "+datatype+"\nlanguage: "+lang;
    }

    public RDFDatatype getDatatype() {
        throw new UnsupportedOperationException();
    }

    public boolean getBoolean() {
        throw new UnsupportedOperationException();
    }

    public byte getByte() {
        throw new UnsupportedOperationException();
    }

    public short getShort() {
        throw new UnsupportedOperationException();
    }

    public int getInt() {
        throw new UnsupportedOperationException();
    }

    public long getLong() {
        throw new UnsupportedOperationException();
    }

    public char getChar() {
        throw new UnsupportedOperationException();
    }

    public float getFloat() {
        throw new UnsupportedOperationException();
    }

    public double getDouble() {
        throw new UnsupportedOperationException();
    }

//    @Deprecated
//    public Object getObject(ObjectF objectF) {
//        throw new UnsupportedOperationException();
//    }


    @Deprecated
    public boolean getWellFormed() {
        throw new UnsupportedOperationException();
    }

    public boolean isWellFormedXML() {
        throw new UnsupportedOperationException();
    }

    public boolean sameValueAs(Literal literal) {
        return equalLiterals( this, literal);
    }

    public boolean isAnon() {
        throw new UnsupportedOperationException();
    }



    public boolean isURIResource() {
        throw new UnsupportedOperationException();
    }

    public boolean isResource() {
        throw new UnsupportedOperationException();
    }

    public Literal inModel(Model model) {
        throw new UnsupportedOperationException();
    }

    public Object visitWith(RDFVisitor rdfVisitor) {
        throw new UnsupportedOperationException();
    }

    public Node asNode() {
        throw new UnsupportedOperationException();
    }


    public static  boolean equalLiterals( Literal a, Literal b){
        if( a == null && b == null )
            return true; //?
        if((a == null && b != null) || ( b == null && a != null))
            return false;

        if( ( a.getDatatypeURI() != null && b.getDatatypeURI() == null )
            || ( a.getDatatypeURI() == null && b.getDatatypeURI() != null ))
            return false;

        //in Jena2, typed literals with languages are not supported, ignore lang
        if( a.getDatatypeURI() != null && b.getDatatypeURI() != null ){
            if( ! a.getDatatypeURI().equals( b.getDatatypeURI() ) ){
                return false;
            }else{
                return compareValues( a, b );
            }
        }

        if( a.getLanguage() == null && b.getLanguage() == null ){
            return compareValues( a, b );
        }

        if(( a.getLanguage() == null && b.getLanguage() != null ) ||
            (a.getLanguage() != null && b.getLanguage() == null ) )
            return false;

        if( a.getLanguage() != null && b.getLanguage() != null &&
            a.getLanguage().equals( b.getLanguage() ) ){
            return compareValues( a, b );
        }else{
            return false;
        }
    }

    private static boolean compareValues( Literal a, Literal b){
        if( a.getValue() == null && b.getValue() == null )
            return true; //?
        else  if( a.getValue() == null && b.getValue() != null
                  || a.getValue() != null && b.getValue() == null )
            return false;
        else
            return a.getValue().equals( b.getValue() ) ;
    }

    @Deprecated
	//public Object getObject(ObjectF arg0) {
	//	throw new UnsupportedOperationException();
	//}

	public <T extends RDFNode> T as(Class<T> arg0) {
        throw new UnsupportedOperationException();
	}

	public <T extends RDFNode> boolean canAs(Class<T> arg0) {
        throw new UnsupportedOperationException();
	}

	public Literal asLiteral() {
		throw new UnsupportedOperationException();
	}

	public Resource asResource() {
		throw new UnsupportedOperationException();
	}

	public Model getModel() {
		throw new UnsupportedOperationException();
	}
}

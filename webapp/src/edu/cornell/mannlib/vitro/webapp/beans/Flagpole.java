package edu.cornell.mannlib.vitro.webapp.beans;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

/**
 * TODO: jc55, Please put a description of this class and its intended use here.
 *
 */
public class Flagpole {
    private int    index=0;

    /**
     * What is this the index of?
     * @return
     */
    public  int    getIndex()        { return index;}
    public  void   setIndex(int i)   { index=i; }

    private int    numeric=0;
    public  int    getNumeric()      { return numeric; }
    public  void   setNumeric(int i) { numeric=i;      }

    private String checkboxLabel=null;
    public  String getCheckboxLabel()         { return checkboxLabel; }
    public  void   setCheckboxLabel(String s) { checkboxLabel=s;      }

    /**
     * What is this the status of?
     */
    private String status="normal";
    public  String getStatus()         { return status; }
    public  void   setStatus(String s) { status=s;      }

    /**
     * What is i, n and cLable?
     * @param i
     * @param n
     * @param cLabel
     */
    public Flagpole(int i, int n, String cLabel) {
        index        =i;
        numeric      =n;
        checkboxLabel=cLabel;
    }
}

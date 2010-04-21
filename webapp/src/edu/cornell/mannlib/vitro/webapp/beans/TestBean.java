package edu.cornell.mannlib.vitro.webapp.beans;

import java.util.List;

public class TestBean {
    
    protected String name;
    protected int[] myInts = null;
    
    public TestBean() {
        myInts = null;
    }

    public String sayHello() { return "hello"; }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setInts(int[] ints) {
        myInts = ints;
    }
    
    public int[] getInts() {
        return myInts;
    }
    
    
}

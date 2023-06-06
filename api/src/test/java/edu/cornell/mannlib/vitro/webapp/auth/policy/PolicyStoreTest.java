package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PolicyStoreTest {

    @Test
    public void testPriorityOrder() {
        PolicyStore store = PolicyStore.getInstance();
        store.clear();
        store.add(new DynamicPolicy("lowest", 0));
        assertEquals(0, store.getList().get(0).getPriority());
        store.add(new DynamicPolicy("lower", 1));
        assertEquals(1, store.getList().get(0).getPriority());
        store.add(new DynamicPolicy("more important", 4));
        assertEquals(4, store.getList().get(0).getPriority());
        store.add(new DynamicPolicy("imporant", 3));
        assertEquals(4, store.getList().get(0).getPriority());
        store.add(new DynamicPolicy("normal", 2));
        assertEquals(4, store.getList().get(0).getPriority());
        store.add(new DynamicPolicy("most imporant", 5));
        assertEquals(5, store.getList().get(0).getPriority());
        store.clear();
    }
    
    @Test
    public void testPriorityDuplicates() {
        PolicyStore store = PolicyStore.getInstance();
        store.clear();
        store.add(new DynamicPolicy("not important", 0));
        assertEquals(0, store.getList().get(0).getPriority());
        assertEquals(1, store.size());
        store.add(new DynamicPolicy("important", 1));
        assertEquals(2, store.size());
        assertEquals(1, store.getList().get(0).getPriority());
        store.add(new DynamicPolicy("not imporant too", 0));
        assertEquals(3, store.size());
        assertEquals(1, store.getList().get(0).getPriority());
        store.add(new DynamicPolicy("equally imporant", 1));
        assertEquals(4, store.size());
        store.clear();
    }
    
    @Test
    public void testUriDuplicates() {
        PolicyStore store = PolicyStore.getInstance();
        store.clear();
        store.add(new DynamicPolicy("unique", 0));
        assertEquals(1, store.size());
        store.add(new DynamicPolicy("not unique", 0));
        assertEquals(2, store.size());
        //replace current policy with the same priority
        store.add(new DynamicPolicy("not unique", 0));
        assertEquals(2, store.size());
        assertEquals(0, store.getList().get(0).getPriority());
        //replace current policy with different priority
        store.add(new DynamicPolicy("not unique", 1));
        assertEquals(2, store.size());
        assertEquals(1, store.getList().get(0).getPriority());
        store.add(new DynamicPolicy("unique too", 0));
        assertEquals(3, store.size());
        store.clear();
    }
}

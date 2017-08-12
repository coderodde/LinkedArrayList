package net.coderodde.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class LinkedArrayListTest {
    
    @Test
    public void testIterator() {
        LinkedArrayList<Integer> list = new LinkedArrayList<>(4); // degree = 4.
        
        for (int i = 0; i < 20; ++i) {
            list.add(i);
        }
        
        Iterator<Integer> iterator = list.iterator();
        
        for (int i = 0; i < list.size(); ++i) {
            assertTrue(iterator.hasNext());
            assertEquals((Integer) i, iterator.next());
        }
        
        assertFalse(iterator.hasNext());
    }
    
    @Test
    public void testIteratorRemove() {
        LinkedArrayList<Integer> list = new LinkedArrayList<>(4);
        
        for (int i = 0; i < 10; ++i) {
            list.add(i);
        }
        
        assertEquals(10, list.size());
        
        Iterator<Integer> iterator = list.iterator();
        
        assertTrue(iterator.hasNext());
        assertEquals(Integer.valueOf(0), iterator.next());
        
        iterator.remove(); // Remove 0.
        
        assertEquals(9, list.size());
        
        assertTrue(iterator.hasNext());
        assertEquals(Integer.valueOf(1), iterator.next()); // Point to 1.
        
        assertTrue(iterator.hasNext());
        assertEquals(Integer.valueOf(2), iterator.next()); // Point to 2.
        
        assertEquals(3, list.getNumberOfBlocks());
        
        assertTrue(iterator.hasNext());
        assertEquals(Integer.valueOf(3), iterator.next()); // Point to 3.
        
        assertTrue(iterator.hasNext());
        assertEquals(Integer.valueOf(4), iterator.next());
        iterator.remove();
        assertEquals(3, list.getNumberOfBlocks());
        
        assertTrue(iterator.hasNext());
        assertEquals(Integer.valueOf(5), iterator.next());
        iterator.remove();
        assertEquals(3, list.getNumberOfBlocks());
        
        assertTrue(iterator.hasNext());
        assertEquals(Integer.valueOf(6), iterator.next());
        iterator.remove();
        assertEquals(3, list.getNumberOfBlocks());
        
        assertTrue(iterator.hasNext());
        assertEquals(Integer.valueOf(7), iterator.next());
        iterator.remove();
        // The 2nd block should be unlinked.
        assertEquals(2, list.getNumberOfBlocks());
    }
    
    @Test
    public void testBruteForceIterator() {
        long seed = System.currentTimeMillis();
        System.out.println("testBruteForceIterator: seed = " + seed);
        Random random = new Random(seed);
        
        List<Integer> myList = new LinkedArrayList<>(4);
        List<Integer> referenceList = new ArrayList<>();
        
        for (int iteration = 0; iteration != 100; iteration++) {
            for (int i = 0; i < 50; ++i) {
                Integer element = random.nextInt(10000);
                
                assertTrue(myList.add(element));
                assertTrue(referenceList.add(element));                
            }
            
            Iterator<Integer> myIterator = myList.iterator();
            Iterator<Integer> referenceIterator = referenceList.iterator();
            boolean lastOperationWasNext = false;
            
            while (myIterator.hasNext()) {
                double coin = random.nextDouble();
                
                if (!lastOperationWasNext || coin < 0.55) {
                    assertEquals(referenceIterator.hasNext(),
                                 myIterator.hasNext());
                    
                    assertEquals(referenceIterator.next(), myIterator.next());
                } else {
                    referenceIterator.remove();
                    myIterator.remove();
                }
            }
            
            while (myIterator.hasNext() && referenceIterator.hasNext()) {
                assertEquals(referenceIterator.next(), myIterator.next());
            }
            
            assertEquals(referenceIterator.hasNext(), myIterator.hasNext());
        }
    }
    
    @Test
    public void testEquals() {
        
    }
}

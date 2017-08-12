package net.coderodde.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
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
    public void testIteratorRemove1() {
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
    
    // Primarily used for finding a failing case.
    @Test
    public void testIteratorRemove2() {
        LinkedArrayList<Integer> list = new LinkedArrayList<>(2);
        
        for (int i = 0; i < 11; ++i) {
            list.add(i);
        }
        
        Iterator<Integer> iterator = list.iterator();
        
        // Omit 0, 1, 2, 3, 4.
        for (int i = 0; i < 5; ++i) {
            assertEquals(Integer.valueOf(i), iterator.next());
        }
        
        iterator.remove();
        
        assertEquals(Integer.valueOf(5), iterator.next());
    }
    
    @Test
    public void testBruteForceIterator() {
        long seed = 1502556885254L; System.currentTimeMillis();
        System.out.println("testBruteForceIterator: seed = " + seed);
        Random random = new Random(seed);
        
        LinkedArrayList<Integer> myList = new LinkedArrayList<>(4);
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
            int op = 0;
            
            while (myIterator.hasNext()) {
                double coin = random.nextDouble();
                System.out.println("Iteration: " + iteration + ", op: " + op);
                
                if (lastOperationWasNext) {
                    // Can remove().
                    if (coin < 0.55) {
                        assertEquals(referenceIterator.hasNext(),
                                     myIterator.hasNext());

                        Integer referenceInt = referenceIterator.next();
                        Integer myInt = myIterator.next();
                        
                        assertEquals(referenceInt, myInt);
                    } else {
                        referenceIterator.remove();
                        myIterator.remove();
                        lastOperationWasNext = false;
                    }
                } else {
                    assertEquals(referenceIterator.hasNext(),
                                 myIterator.hasNext());
                    
                    assertEquals(referenceIterator.next(),
                                 myIterator.next());
                    
                    lastOperationWasNext = true;
                }
                
                myList.checkHealty();
                op++;
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

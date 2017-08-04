package net.coderodde.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class LinkedArrayList<E> implements List<E> {

    /**
     * This static inner class implements a linked array list block.
     * 
     * @param <E> the element type.
     */
    private static final class LinkedArrayListBlock<E> {
        
        /**
         * The index of the head element.
         */
        private int headIndex;
        
        /**
         * The number of elements stored in this block.
         */
        private int size;
        
        /**
         * The array holding the elements.
         */
        private final E[] elements;
        
        /**
         * Used for faster modulo computation.
         */
        private final int moduloMask;
        
        /**
         * The next block in the chain.
         */
        private LinkedArrayListBlock<E> next;
        
        /**
         * The next block in the chain.
         */
        private LinkedArrayListBlock<E> prev;
        
        /**
         * Creates a new block with given degree.
         * @param degree 
         */
        LinkedArrayListBlock(int degree) {
            this.elements = (E[]) new Object[degree];
            this.moduloMask = degree - 1;
        }
        
        /**
         * Returns the number of elements stored in this block.
         * 
         * @return the number of elements stored.
         */
        int size() {
            return size;
        }
        
        /**
         * Appends the input element to the tail of this block.
         * 
         * @param element the element to append.
         */
        void add(E element) {
            int index = (headIndex + size++) & moduloMask;
            elements[index] = element;
        }
        
        /**
         * Inserts the input element at a given index.
         * 
         * @param index   the insertion index.
         * @param element the element to insert.
         */
        void insert(int index, E element) {
            int leftPartLength = index;
            int rightPartLength = size - index;
            
            if (leftPartLength < rightPartLength) {
                shiftLeft(index - 1);
                headIndex = (headIndex - 1) & moduloMask;
            } else {
                shiftRight(index);
            }
            
            elements[index] = element;
            ++size;
        }
        
        /**
         * Removes the element with order index {@code index}.
         * 
         * @param index the index of the element to remove.
         */
        E delete(int index) {
            E ret = elements[(headIndex + index) & moduloMask];
            int leftPartLength = index;
            int rightPartLength = size - index - 1;
            
            if (leftPartLength < rightPartLength) {
                shiftRight(leftPartLength);
                elements[headIndex] = null;
                headIndex = (headIndex + 1) & moduloMask;
            } else {
                shiftLeft(rightPartLength);
                elements[(headIndex + size - 1) & moduloMask] = null;
            }
            
            --size;
            return ret;
        }
        
        /**
         * Shifts {@code count} first elements one position to the left (towards
         * smaller indices). When the leftmost element is shifted one position 
         * to the left, it "wraps around" and goes to the very last array
         * component.
         * 
         * @param count number of leftmost elements to shift.
         */
        private void shiftLeft(int count) {
            for (int i = 0; i < count; ++i) {
                elements[(headIndex - 1 + i) & moduloMask] =
                elements[(headIndex + i) & moduloMask];
            }
        }
        
        /**
         * Shifts {@code count} last elements one position to the right (towards
         * larger indices). When the rightmost element is shifted one position
         * to the right, it "wraps around" and goes to the very first array
         * component.
         * 
         * @param count number of rightmost elements to shift.
         */
        private void shiftRight(int count) {
            for (int i = count - 1; i >= 0; --i) {
                elements[(headIndex + 1 + i) & moduloMask] =
                elements[(headIndex + i) & moduloMask];
            }
        }
    }
    
    /**
     * The minimum block degree.
     */
    private static final int MINIMUM_BLOCK_DEGREE = 2;
    
    /**
     * The default block degree.
     */
    private static final int DEFAULT_BLOCK_DEGREE = 128;
    
    /**
     * The block degree used by this list.
     */
    private final int degree;
    
    /**
     * The number of elements stored in this list.
     */
    private int size;
    
    /**
     * The number of blocks used by this list.
     */
    private int blocks;
    
    /**
     * The modification count of this list.
     */
    private int modificationCount;
    
    /**
     * The head block.
     */
    private LinkedArrayListBlock<E> head;
    
    /**
     * The tail block.
     */
    private LinkedArrayListBlock<E> tail;
    
    /**
     * Constructs a new, empty list with given degree.
     * 
     * @param degree the degree of this list.
     */
    public LinkedArrayList(int degree) {
        degree = Math.max(degree, MINIMUM_BLOCK_DEGREE);
        this.degree = fixDegree(degree);
    }
    
    /**
     * Constructs a new, empty list with the default degree.
     */
    public LinkedArrayList() {
        this(DEFAULT_BLOCK_DEGREE);
    }
    
    /**
     * Returns the number of blocks of this list.
     * 
     * @return the number of blocks.
     */
    public int getNumberOfBlocks() {
        return blocks;
    }
    
    /**
     * Returns the degree of this list.
     * 
     * @return the degree.
     */
    public int getDegree() {
        return degree;
    }
    
    /**
     * Returns the load factor of this list.
     * 
     * @return the load factor.
     */
    public float getLoadFactor() {
        if (blocks == 0) {
            return 0.0f;
        }
        
        return 1.0f * size / (blocks * degree);
    }
    
    /**
     * Compacts this list so that the minimum possible space is used by this
     * list.
     */
    public void compact() {
        if (size == 0) {
            return;
        }
        
        LinkedArrayListBlock<E> targetBlock = head;
        LinkedArrayListBlock<E> sourceBlock = head.next;
        
        while (sourceBlock != null) {
            int capacity = degree - targetBlock.size;
            int available = sourceBlock.size;
            
            while (capacity > 0 && available > 0) {
                targetBlock.add(sourceBlock.delete(0));
                --capacity;
                --available;
            }
            
        }
    }
    
    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterator<E> iterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[size];
    // TODO:
        return array;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E get(int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ListIterator<E> listIterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Returns a smallest integer that is a power of two no less than 
     * {@code degree}.
     * 
     * @param degree the degree to fix.
     * 
     * @return the fixed degree that is a power of two.
     */
    private int fixDegree(int degree) {
        int c = MINIMUM_BLOCK_DEGREE;
        
        while (c < degree) {
            c <<= 1; // Multiply by 2.
        }
        
        return c;
    }
}

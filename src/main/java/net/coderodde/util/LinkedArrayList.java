package net.coderodde.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

/**
 * This class implements a list data structure that combines a linked list and
 * an array-based list.
 * 
 * @author Rodion "rodde" Efremov
 * @param <E> the element type.
 * 
 * @version 1.6 (Aug 4, 2017)
 */
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
        
        E get(int index) {
            return (E) elements[(headIndex + index) & moduloMask];
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
        
        void bulkInsert(int index, int count, Collection<?> collection) {
            int leftPartLength = index;
            int rightPartLength = size - index;
            
            if (leftPartLength < rightPartLength) {
                
            } else {
                
            }
        }
        
        /**
         * This method should be used only when inserting into a full block.
         * 
         * @param index   the index of the element within this block.
         * @param element the element to insert.
         * @return a new spawned block containing a half of elements of this 
         *         block.
         */
        LinkedArrayListBlock<E> splitInsert(int index, E element) {
            LinkedArrayListBlock<E> followerBlock = 
                    new LinkedArrayListBlock<>(elements.length);
            
            if (index < elements.length - index) {
                // The new element goes into this block:
                splitInsertInThis(index, element, followerBlock);
            } else {
                // The new element goes into the new block:
                splitInsertInFollower(index, element, followerBlock);
            }
            
            return followerBlock;
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
         * Wipes all the content from this block.
         */
        void clear() {
            for (int i = 0; i < size; ++i) {
                elements[(headIndex + i) & moduloMask] = null;
            }
            
            size = 0;
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
        
        /**
         * Shifts {@code count} first elements {@code shiftLength} positions to
         * the left (towards smaller indices). This method "wraps around" the
         * elements that are shifted too much to the left, and inserts them to
         * the tail part of the block.
         * 
         * @param count       the number of leftmost elements to shift.
         * @param shiftLength the number of positions shifted for each element.
         */
        private void shiftLeft(int count, int shiftLength) {
            for (int i = 0; i < count; ++i) {
                elements[(headIndex - shiftLength + i) & moduloMask] =
                elements[(headIndex + i) & moduloMask];
            }
        }
        
        /**
         * Inserts the element in this block.
         * 
         * @param index         the index of the element to insert.
         * @param element       the element to insert.
         * @param followerBlock the follower block.
         */
        private void splitInsertInThis(int index,
                                       E element,
                                       LinkedArrayListBlock<E> followerBlock) {
            
        }
        
        /**
         * Inserts the element in the follower block.
         * 
         * @param index         the index of the element to insert.
         * @param element       the element to insert.
         * @param followerBlock the follower block.
         */
        private void splitInsertInFollower(
                int index, 
                E element,
                LinkedArrayListBlock<E> followerBlock) {
            
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
        LinkedArrayListBlock<E> initialBlock = 
                new LinkedArrayListBlock<>(this.degree);
        head = initialBlock;
        tail = initialBlock;
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
        
        while (true) {
            int capacity = degree - targetBlock.size;
            int available = sourceBlock.size;
            int load = Math.min(capacity, available);
            
            for (int i = 0; i < load; ++i) {
                targetBlock.add(sourceBlock.delete(0));
            }
            
            capacity -= load;
            available -= load;
            
            if (capacity == 0) {
                targetBlock = targetBlock.next;
            }
            
            if (available == 0) {
                sourceBlock = sourceBlock.next;
                
                if (sourceBlock == null) {
                    return;
                }
            }
            
            if (targetBlock == sourceBlock) {
                sourceBlock = sourceBlock.next;
            
                if (sourceBlock == null) {
                    return;
                }
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
        for (LinkedArrayListBlock<E> block = head; 
                block != null; 
                block = block.next) {
            for (int i = 0; i < block.size; ++i) {
                if (Objects.equals(o, block.get(i))) {
                    return true;
                }
            }
        }
        
        return false;
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
        if (tail.size == degree) {
            LinkedArrayListBlock<E> newTail =
                    new LinkedArrayListBlock<>(degree);
            tail.next = newTail;
            newTail.prev = tail;
            tail = newTail;
        }
        
        tail.add(e);
        modificationCount++;
        size++;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        for (LinkedArrayListBlock<E> block = head; 
                head != null;
                head = head.next) {
            for (int i = 0; i < block.size; ++i) {
                if (Objects.equals(o, block.get(i))) {
                    block.delete(i);
                    modificationCount++;
                    size--;
                    return true;
                }
            }
        }
        
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains((E) o)) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        if (c.isEmpty()) {
            return false;
        }
        
        for (Object o : c) {
            if (tail.size == degree) {
                LinkedArrayListBlock<E> newTail = 
                        new LinkedArrayListBlock<>(degree);
                
                tail.next = newTail;
                newTail.prev = tail;
                tail = newTail;
            }
            
            tail.add((E) o);
        }
        
        modificationCount += c.size();
        size += c.size();
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        if (c.isEmpty()) {
            return false;
        }
        checkInsertionIndex(index);
        LinkedArrayListBlock<E> block = findBlock(index);
        // Do some arithmetics here!
        int totalElements = c.size();
        totalElements -= block.size;
        int numberOfNewBlocks = totalElements / degree;
        int leftoverElements = totalElements - numberOfNewBlocks * degree;
        
        
        
        
        
        return true;
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
        modificationCount += size;
        size = 0;
        tail = head;
        head.clear();
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
    
    private LinkedArrayListBlock<E> findBlock(int index) {
        // The position indexed by 'index' is closer to the head?
        if (index < size - index) {
            // There is a good chance that we may reach the target block faster
            // if we move starting from the head block towards the tail:
            for (LinkedArrayListBlock<E> block = head;; block = block.next) {
                if (index < block.size) {
                    return block;
                }
                
                index -= block.size;
            }
        } else {
            // Symmetrically, we might reach the target block faster if we start
            // from the tail moving towards the head:
            int currentBlockMinIndex = size - tail.size;
            
            for (LinkedArrayListBlock<E> block = tail;;) {
                if (index >= currentBlockMinIndex) {
                    return block;
                }
                
                block = block.prev;
                currentBlockMinIndex -= block.size;
            }
        }
    }
    
    private void checkInsertionIndex(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException(
                    "The insertion index is negative: " + index + ".");
        }
        
        if (index > size) {
            throw new IndexOutOfBoundsException(
                    "The insertion index is too large: " + index + ". " +
                            "Must be at most " + size + ".");
        }
    }
}

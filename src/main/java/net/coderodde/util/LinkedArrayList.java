package net.coderodde.util;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * This class implements a list data structure that combines a linked list and
 * an array-based list. Basically, it is a linked list of small arrays, and can
 * be seen as an approximation of both array-based and linked lists. The 
 * <b>degree</b> of a linked array list is the capacity of each block.
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
         * 
         * @param degree the degree of this block. Is assumed to be a positive
         *               power of two.
         */
        LinkedArrayListBlock(int degree) {
            this.elements = (E[]) new Object[degree];
            this.moduloMask = degree - 1;
        }
        
        /**
         * Returns the element at index {@code index}.
         * 
         * @param index the element index within this block.
         * 
         * @return the {@code index}th element of this block.
         */
        E get(int index) {
            return (E) elements[(headIndex + index) & moduloMask];
        }
        
        /**
         * Sets the new element at index {@code index}.
         * 
         * @param index the local element index within this block.
         * @param value the new element.
         * 
         * @return the old element.
         */
        E set(int index, E value) {
            E oldValue = (E) elements[(headIndex + index) & moduloMask];
            elements[(headIndex + index) & moduloMask] = value;
            return oldValue;
        }
        
        /**
         * Appends the input element to the tail of this block.
         * 
         * @param element the element to append.
         */
        void append(E element) {
            elements[(headIndex + size++) & moduloMask] = element;
        }
        
        /**
         * Prepends the input element to the head of this block.
         * 
         * @param element the element to append.
         */
        void prepend(E element) {
            elements[headIndex = ((headIndex - 1) & moduloMask)] = element;
            ++size;
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
         * Removes the first element from this block.
         * 
         * @return the first element.
         */
        E removeFirst() {
            E first = (E) elements[headIndex];
            headIndex = (headIndex + 1) & moduloMask;
            --size;
            return first;
        }
        
        /**
         * Removes the last element from this block.
         * 
         * @return the last element.
         */
        E removeLast() {
            E last = (E) elements[(headIndex + size - 1) & moduloMask];
            --size;
            return last;
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
            shiftLeft(count, 1);
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
            shiftRight(count, 1);
        }
        
        /**
         * Shifts {@code count} first elements {@code shiftLength} positions to
         * the left (towards smaller indices). This method "wraps around" the
         * elements that are shifted too much to the left, and inserts them to
         * the tail part of the block.
         * 
         * @param count       the number of leftmost elements to shift.
         * @param shiftLength the number of positions shifted for each affected
         *                    element.
         */
        private void shiftLeft(int count, int shiftLength) {
            for (int i = 0; i < count; ++i) {
                elements[(headIndex - shiftLength + i) & moduloMask] =
                elements[(headIndex + i) & moduloMask];
            }
        }
        
        /**
         * Shifts {@codde count} last elements {@code shiftLength} positions to
         * the right (towards larger indices). This method "wraps around" the 
         * elements that are shifted too much to the right, and inserts them to
         * the head of the block.
         * 
         * @param count       the number of rightmost elements to shift.
         * @param shiftLength the number of positions shifted for each affected
         *                    element.
         */
        private void shiftRight(int count, int shiftLength) {
            for (int i = count - 1; i >= 0; --i) {
                elements[(headIndex + size - 1 - i + shiftLength) & moduloMask]
                = elements[(headIndex + size - 1 - i) & moduloMask];
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
    
    private int localBlockIndex;
    
    private LinkedArrayListBlock<E> block;
    
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
        blocks = 1;
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
        if (size == 0) {
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
                targetBlock.append(sourceBlock.delete(0));
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
        return new LinkedArrayListIterator();
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
            blocks++;
        }
        
        tail.append(e);
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
                    
                    if (block.size == 0 && head != tail) {
                        unlink(block);
                        blocks--;
                    }
                    
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
                blocks++;
            }
            
            tail.append((E) o);
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
        
        LinkedArrayListBlock<E> firstBlock;
        int firstBlockIndex;
        
        // The position indexed by 'index' is closer to the head?
        if (index < size - index) {
            // There is a good chance that we may reach the target block faster
            // if we move starting from the head block towards the tail:
            for (LinkedArrayListBlock<E> block = head;; block = block.next) {
                if (index <= block.size) {
                    firstBlock = block;
                    firstBlockIndex = index;
                    break;
                }
                
                index -= block.size;
            }
        } else {
            // Symmetrically, we might reach the target block faster if we start
            // from the tail moving towards the head:
            int currentBlockMinIndex = size - tail.size;
            
            for (LinkedArrayListBlock<E> block = tail;;) {
                if (index >= currentBlockMinIndex) {
                    firstBlock = block;
                    break;
                }
                
                block = block.prev;
                currentBlockMinIndex -= block.size;
            }
        }
        
        // Do some arithmetics here!
        int collectionSize = c.size();
        int blockSpaceAvailable = degree - firstBlock.size;
        
        if (collectionSize <= blockSpaceAvailable) {
            for (E element : c) {
                
            }
        }
        
        
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if (c.isEmpty()) {
            return false;
        }
        
        for (Object o : c) {
            remove((E) o);
        }
        
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;
        Iterator<E> iterator = iterator();
        
        while (iterator.hasNext()) {
            E currentElement = iterator.next();
            
            if (!c.contains(currentElement)) {
                iterator.remove();
                modified = true;
            }
        }
        
        return modified;
    }

    @Override
    public void clear() {
        modificationCount += size;
        size = 0;
        head.next = null;
        tail = head;
        blocks = 1;
        head.clear();
    }

    @Override
    public E get(int index) {
        checkAccessIndex(index);
        searchBlockIndexAccess(index);
        return block.get(localBlockIndex);
    }

    @Override
    public E set(int index, E element) {
        checkAccessIndex(index);
        searchBlockIndexAccess(index);
        return block.set(localBlockIndex, element);
    }

    @Override
    public void add(int index, E element) {
        checkInsertionIndex(index);
        searchBlockIndexInsertion(index);
        
        if (block.size < degree) {
            block.insert(localBlockIndex, element);
        } else {
            insertIntoFullBlock(element);
        }
        
        size++;
    }
    
    private void insertIntoFullBlock(E element) {
        int localBlockIndex = this.localBlockIndex;
        LinkedArrayListBlock<E> block = this.block;
        
        boolean leftNeighborCanAccommodate = 
                block.prev != null && block.prev.size < degree;
        
        boolean rightNeighborCanAccommodate = 
                block.next != null && block.next.size < degree;
        
        if (leftNeighborCanAccommodate) {
            if (rightNeighborCanAccommodate) {
                insertIntoFullBlockBothNeighborsNotFull(element, 
                                                        block.prev,
                                                        block.next);
            } else {
                insertIntoFullBlockLeftNeighborNotFull(element, block.prev);
            }
        } else {
            if (rightNeighborCanAccommodate) {
                insertIntoFullBlockRightNeighborNotFull(element, block.next);
            } else {
                insertIntoFullBlockNoneNeighborNotFull(element);
            }
        }
    }
    
    /**
     * Links {@code beforeBlock} immediately before {@code block}.
     * 
     * @param beforeBlock the block to link.
     * @param block       the target block.
     */
    private void linkBefore(LinkedArrayListBlock<E> beforeBlock,
                            LinkedArrayListBlock<E> block) {
        if (block.prev != null) {
            beforeBlock.prev = block.prev;
            beforeBlock.next = block;
            beforeBlock.prev.next = beforeBlock;
            block.prev = beforeBlock;
        } else {
            beforeBlock.next = block;
            block.prev = beforeBlock;
            head = beforeBlock;
        }
    }
    
    /**
     * Links {@code afterBlock} immediately after {@code block}.
     * 
     * @param afterBlock the block to link.
     * @param block      the target block.
     */
    private void linkAfter(LinkedArrayListBlock<E> afterBlock,
                           LinkedArrayListBlock<E> block) {
        if (block.next != null) {
            afterBlock.prev = block;
            afterBlock.next = block.next;
            block.next.prev = afterBlock;
            block.next = afterBlock;
        } else {
            afterBlock.prev = block;
            block.next = afterBlock;
            tail = afterBlock;
        }
    }
    
    private void insertIntoFullBlockNoneNeighborNotFull(E element) {
        int localBlockIndex = this.localBlockIndex;
        LinkedArrayListBlock<E> block = this.block;
        
        if (localBlockIndex == 0) {
            LinkedArrayListBlock<E> predecessor = 
                    new LinkedArrayListBlock<>(degree);
            predecessor.append(element);
            linkBefore(predecessor, block);
        } else if (localBlockIndex == degree) {
            LinkedArrayListBlock<E> successor = 
                    new LinkedArrayListBlock<>(degree);
            successor.append(element);
            linkAfter(successor, block);
        } else {
            int leftPartLength = localBlockIndex;
            int rightPartLength = degree - localBlockIndex;
            
            if (leftPartLength < rightPartLength) {
                LinkedArrayListBlock<E> newBlock =
                        new LinkedArrayListBlock<>(degree);
                newBlock.append(block.removeFirst());
                ///
            } else {
                
            }
        }
    }
    
    
    /**
     * Inserts the input element into the current block given that the current
     * block is full, but the right neighbor (that exists) is not full.
     * 
     * @param element       the element to insert.
     * @param rightNeighbor the right neighbor of the current block.
     */
    private void insertIntoFullBlockRightNeighborNotFull(
            E element, LinkedArrayListBlock<E> rightNeighbor) {
        int localBlockIndex = this.localBlockIndex;
        
        if (localBlockIndex == degree) {
            rightNeighbor.prepend(element);
        } else {
            LinkedArrayListBlock<E> block = this.block;
            rightNeighbor.prepend(block.removeLast());
            block.shiftRight(degree - localBlockIndex);
            block.set(localBlockIndex, element);
        }
    }
    
    /**
     * Inserts the input element into the current block given that the current
     * block is full, but the left neighbor (that exists) is not full.
     * 
     * @param element      the element to insert.
     * @param leftNeighbor the left neighbor of the current block.
     */
    private void insertIntoFullBlockLeftNeighborNotFull(
            E element, LinkedArrayListBlock<E> leftNeighbor) {
        int localBlockIndex = this.localBlockIndex;
        
        if (localBlockIndex == 0) {
            leftNeighbor.append(element);
        } else {
            LinkedArrayListBlock<E> block = this.block;
            leftNeighbor.append(block.removeFirst());
            block.shiftLeft(localBlockIndex - 1);
            block.set(localBlockIndex, element);
        }   
    }
    
    /**
     * Inserts the input element into the current block given that the current
     * block is full, but both its neighbors (that exist) are not full. This 
     * routine aims to do as little work as possible.
     * 
     * @param element       the element to insert.
     * @param leftNeighbor  the left neighbor of the current block.
     * @param rightNeighbor the right neighbor of the current block.
     */
    private void insertIntoFullBlockBothNeighborsNotFull(
            E element, 
            LinkedArrayListBlock<E> leftNeighbor,
            LinkedArrayListBlock<E> rightNeighbor) {
        int localBlockIndex = this.localBlockIndex;
        
        if (localBlockIndex == 0) {
            // Just append to the end of the left neighbor:
            leftNeighbor.append(element);
        } else if (localBlockIndex == degree) {
            // Just preprend to the head of the right neighbor:
            rightNeighbor.prepend(element);
        } else {
            LinkedArrayListBlock<E> block = this.block;

            int leftPartLength = localBlockIndex;
            int rightPartLength = block.size - localBlockIndex;

            if (leftPartLength < rightPartLength) {
                leftNeighbor.append(block.removeFirst());
                block.shiftLeft(leftPartLength - 1);
                block.set(localBlockIndex, element);
            } else {
                rightNeighbor.prepend(block.removeLast());
                block.shiftRight(rightPartLength - 1);
                block.set(localBlockIndex, element);
            }
        }
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
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof List)) {
            return false;
        }
        
        List<E> other = (List<E>) o;
        
        if (size() != other.size()) {
            return false;
        }
        
        Iterator<E> iterator = iterator();
        Iterator<E> otherIterator = other.iterator();
        
        while (iterator.hasNext()) {
            if (!Objects.equals(iterator.next(), otherIterator.next())) {
                return false;
            }
        }
        
        return true;
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
    
    /**
     * Searches the block and a local block index containing the element that is
     * at index {@code index} in the entire global list. This method modifies
     * {@code block} and {@code localBlockIndex}.
     * 
     * @param index the global index of the element to search.
     */
    private void searchBlockIndexAccess(int index) {
        // The position indexed by 'index' is closer to the head?
        if (index < size - index) {
            // There is a good chance that we may reach the target block faster
            // if we move starting from the head block towards the tail:
            for (LinkedArrayListBlock<E> block = head;; block = block.next) {
                if (index < block.size) {
                    this.localBlockIndex = index;
                    this.block = block;
                    return;
                }
                
                index -= block.size;
            }
        } else {
            // Symmetrically, we might reach the target block faster if we start
            // from the tail moving towards the head:
            int currentBlockMinIndex = size - tail.size;
            
            for (LinkedArrayListBlock<E> block = tail;;) {
                if (index >= currentBlockMinIndex) {
                    this.localBlockIndex = index - currentBlockMinIndex;
                    this.block = block;
                    return;
                }
                
                block = block.prev;
                currentBlockMinIndex -= block.size;
            }
        }
    }
    
    private void searchBlockIndexInsertion(int index) {
        if (index < size - index) {
            for (LinkedArrayListBlock<E> block = head;; block = block.next) {
                if (index <= block.size) {
                    this.localBlockIndex = index;
                    this.block = block;
                    return;
                }
                
                index -= block.size;
            }
        } else {
            int currentBlockMinIndex = size - tail.size;
            
            for (LinkedArrayListBlock<E> block = tail;; block = block.prev) {
                if (index >= currentBlockMinIndex) {
                    this.localBlockIndex = index - currentBlockMinIndex;
                    this.block = block;
                    return;
                }
                
                currentBlockMinIndex -= block.size;
            }
        }
    }
    
    /**
     * Checks that the input index is a valid insertion index, i.e., that it is
     * at least 0 and at most {@code size}.
     * 
     * @param index the index to check.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    private void checkInsertionIndex(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException(
                    "The insertion index is negative: " + index + ".");
        }
        
        if (index > size) {
            throw new IndexOutOfBoundsException(
                    "The insertion index is too large: " + index + ". " +
                    "Must be at most " + size + " since the size of " +
                    "this list is " + size + ".");
        }
    }
    
    /**
     * Checks that the input index is a valid access index, i.e., that it is at
     * least 0 and at most {@code size - 1}.
     * 
     * @param index the index to check.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    private void checkAccessIndex(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException(
                    "The access index is negative: " + index + ".");
        }
        
        if (index >= size) {
            throw new IndexOutOfBoundsException(
                    "The access index is too large: " + index + ". " +
                    "Must be at most " + (size - 1) + " since the size of " +
                    "this list is " + size + ".");
        }
    }
    
    /**
     * Unlinks the block from the linked list of blocks.
     * 
     * @param block the block to unlink.
     */
    private void unlink(LinkedArrayListBlock<E> block) {
        if (block.prev != null) {
            block.prev.next = block.next;
        } else {
            // 'block' is the head node:
            head = block.next;
            head.prev = null;
        }
        
        if (block.next != null) {
            block.next.prev = block.prev;
        } else {
            // 'block' is the tail node:
            tail = block.prev;
            tail.next = null;
        }
    }
    
    /**
     * This class implements an {@link Iterator} over this list.
     */
    private final class LinkedArrayListIterator implements Iterator<E> {

        /**
         * Caches the size of the list prior to iteration. We need this field
         * since the client may remove elements while iterating via the 
         * {@code remove} method.
         */
        private final int cachedSize = size;
        
        /**
         * The number of elements iterated so far.
         */
        private int iterated = 0;
        
        /**
         * The block being currently iterated.
         */
        private LinkedArrayListBlock<E> currentBlock = head;
        
        /**
         * The index pointing at the next element being iterated.
         */
        private int currentLocalIndex = 0;
        
        /**
         * The expected modification count of the list being iterated.
         */
        private final int expectedModificationCount = modificationCount;
        
        private boolean previousOperationWasRemove = true;
        
        @Override
        public boolean hasNext() {
            return iterated != cachedSize;
        }

        @Override
        public E next() {
            checkConcurrentModification();
            
            if (!hasNext()) {
                throw new NoSuchElementException("Nothing to iterate left.");
            }
            
            E element = currentBlock.get(currentLocalIndex);
            currentLocalIndex++;
            
            if (currentLocalIndex == currentBlock.size) {
                currentLocalIndex = 0;
                currentBlock = currentBlock.next;
            }
            
            previousOperationWasRemove = false;
            iterated++;
            return element;
        }
        
        @Override
        public void remove() {
            if (previousOperationWasRemove) {
                if (iterated == 0) {
                    throw new IllegalStateException(
                            "next() was not called yet.");                    
                } else {
                    throw new IllegalStateException(
                            "Removing the same element twice.");
                }
            }
            
            if (currentLocalIndex == 0) {
                currentBlock.prev.removeLast();
            } else {
                currentBlock.delete(currentLocalIndex - 1);
            }
            
            previousOperationWasRemove = true;
        }
        
        private void checkConcurrentModification() {
            if (expectedModificationCount != modificationCount) {
                throw new ConcurrentModificationException(
                        "The list was modified while iterated via Iterator.");
            }
        }
    }
}

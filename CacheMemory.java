/**
* Simulates cache memory
*
* @author Zoe Beals
*/

import java.util.*;

public class CacheMemory{

    /** Set to true to print additional messages for debugging purposes */
    private static final boolean DEBUG = false;

    /** The number of bytes read or written by one CPU operation */
    public static final int WORD_SIZE = 4; // 4 bytes = 32 bits

    /** The Main Memory this cache is connected to. */
    private MainMemory mainMemory;

    /** Simulate cache as an array of CacheSet objects. */
    private CacheSet[] cache;

    /** Number of bits used for selecting one byte within a cache line.
    * These are the least significant bits of the memory address. */
    private int numByteBits;

    /** Number of bits used for specifying the cache set that a memory adddres
    * belongs to. These are the middle bits of the memory address. */
    private int numSetBits;

    /** Number of bits used for specifying the tag associated with the
    * memory address. These are the most significant bits of the memory address. */
    private int numTagBits;

    /** Count of the total number of cache requests. This is used for implementing
    * the least recently used replacement algorithm; and for reporting information
    * about the cache simulation. */
    private int requestCount;

    /** Count of the number of times a cache request is a hit. This is used for
    * reporting information about the cache simulation. */
    private int hitCount;

    /** Track the "cost" of a hit. For each cache hit, record the number of cache lines
    * that are searched in order to determine this is a hit. This data member
    * is an accumulator for the hit cost (each hit will add its cost to this
    * data member). This is used for reporting information about the cache simulation.
    */
    private int hitCost;

    /** Count of the number of cache requests that are performed during the
    * warmUp method.  This is used for reporting information about the cache
    * simulation.*/
    private int warmUpRequests;

    /**
    * DO NOT MODIFY THIS METHOD
    *
    * Constructor creates a CacheMemory object. Note the design rules for valid values of each parameter.
    * The simulated computer reads or writes a unit of one WORD_SIZE.
    *
    * @param m The MainMemory object this cache is connected to.
    * @param size The size of this cache, in Bytes. Must be a multiple of the lineSize.
    * @param lineSize The size of one cache line, in Bytes. Must be a multiple of 4 Bytes.
    * @param linesPerSet The number of lines per set. The number of lines in the cache must be a multiple
    * of the linesPerSet.
    *
    * @exception IllegalArgumentExcepction if a parameter value violates a design rule.
    */
    public CacheMemory(MainMemory m, int size, int lineSize, int linesPerSet) {

        if(lineSize % WORD_SIZE != 0) {
            throw new IllegalArgumentException("lineSize is not a multiple of " + WORD_SIZE);
        }

        if(size % lineSize != 0) {
            throw new IllegalArgumentException("size is not a multiple of lineSize.");
        }

        // number of lines in the cache
        int numLines = size / lineSize;

        if(numLines % linesPerSet != 0) {
            throw new IllegalArgumentException("number of lines is not a multiple of linesPerSet.");
        }

        // number of sets in the cache
        int numSets = numLines / linesPerSet;

        // Set the main memory
        mainMemory = m;

        // Initialize the counters to zero
        requestCount = 0;
        warmUpRequests = 0;
        hitCount = 0;
        hitCost = 0;

        // Determine the number of bits required for the byte within a line,
        // for the set, and for the tag.
        int value;
        numByteBits = 0; // initialize to zero
        value = 1; // initialize to 2^0
        while(value < lineSize) {
            numByteBits++;
            value *= 2; // increase value by a power of 2
        }

        numSetBits = 0;
        value = 1;
        while(value < numSets) {
            numSetBits++;
            value *= 2;
        }

        // numTagBits is the remaining memory address bits
        numTagBits = 32 - numSetBits - numByteBits;

        System.out.println("CacheMemory constructor:");
        System.out.println("    numLines = " + numLines);
        System.out.println("    numSets = " + numSets);
        System.out.println("    numByteBits = " + numByteBits);
        System.out.println("    numSetBits = " + numSetBits);
        System.out.println("    numTagBits = " + numTagBits);
        System.out.println();

        // Create the array of CacheSet objects and initialize each CacheSet object
        cache = new CacheSet[numSets];
        for(int i = 0; i < cache.length; i++) {
            cache[i] = new CacheSet(lineSize, linesPerSet, numTagBits);
        }
    } // end of constructor

    /**
    * DO NOT MODIFY THIS METHOD
    *
    * "Warm Up" the cache by reading random memory addresses. This method is
    * used by programs that do not want to run on a "cold" cache. The cache
    * performance statistics do not include requests from this warm up phase.
    *
    * @param numReads The number of warm-up read operations to perform.
    * @param random A random number generator object.
    */
    public void warmUp(int numReads, Random random) {
        int wordsInMainMem = (mainMemory.getSize() / WORD_SIZE);

        for(int i = 0; i < numReads; i++) {
            // Generate a random line address
            int wordAddress = random.nextInt(wordsInMainMem) * WORD_SIZE;
            boolean[] address = Binary.uDecToBin(wordAddress, 32);
            readWord(address, false);
        }
        warmUpRequests = requestCount;
    }

    /**
    * DO NOT MODIFY THIS METHOD
    *
    * Prints the total number of requests and the number of requests that
    * resulted in a cache hit.
    */
    public void reportStats() {
        System.out.println("Number of requests: " + (requestCount - warmUpRequests));
        System.out.println("Number of hits: " + hitCount);
        System.out.println("hit ratio: " + (double)hitCount / (requestCount - warmUpRequests));
        System.out.println("Average hit cost: " + (double)hitCost / hitCount);
    }

    /**
    * DO NOT MODIFY THIS METHOD
    *
    * Returns the word that begins at the specified memory address.
    *
    * This is the public version of readWord. It calls the private version
    * of readWord with the recordStats parameter set to true so that the
    * cache statistics information will be recorded.
    *
    * @param address The byte address where the 32-bit value begins.
    * @return The word read from memory. Index 0 of the array holds
    * the least significant bit of the binary value.
    *
    */
    public boolean[] readWord(boolean[] address) {
        return readWord(address, true);
    }

    /**
    * STUDENT MUST COMPLETE THIS METHOD
    *
    * Returns the word that begins at the specified memory address.
    *
    * This is the private version of readWord that includes the cache statistic
    * tracking parameter. When recordStats is false, this method should not
    * update the cache statistics data members (hitCount and hitCost).
    *
    * @param address The byte address where the 32-bit value begins.
    * @param recordStats Set to true if cache statistics tracking data members
    *                   (hitCount and hitCost) should be updated.
    * @return The word read from memory. Index 0 of the array holds
    * the least significant bit of the binary value.
    * @exception IllegalArgumentExcepction if the address is not valid.
    */
    private boolean[] readWord(boolean[] address, boolean recordStats) {
        if(address.length > 32) {
            throw new IllegalArgumentException("address parameter must be 32 bits");
        }
        // Programming Assignment 5: Complete this method
        // The comments provide a guide for this method.
        boolean[] bytes = Arrays.copyOfRange(address, 0, numByteBits); //array to hold the byte bits
        boolean[] set = Arrays.copyOfRange(address, numByteBits, numByteBits+numSetBits); //array to hold the set bits
        boolean[] tag = Arrays.copyOfRange(address, numSetBits+numByteBits, address.length); //array to hold the tag bits
        int setNum = (int)Binary.binToUDec(set); //integer representation of the setNumber to use in a loop
        CacheLine line; //temporary CacheLine
        boolean[][] data = new boolean[4][8]; //2d array to hold the data from a line
        boolean[] tempData = new boolean[32]; //array to hold the data from a line
        int i; //to use in the for loop
        // Where does the address map in the cache?
        //loop thru the specific set in the cache
        for (i = 0; i < cache[setNum].size(); i++) {
          line = cache[setNum].getLine(i); //get the current line
          if (Arrays.equals(line.getTag(), tag) && line.isValid()) { //if the tag from the line is equal to the address tag and the line is valid,
            line.setLastUsed(requestCount); //set the last used to the current request Count
            //if recordStats is true
            if (recordStats) {
              hitCount += 1; //increment hitCount
              hitCost += i+1; //increment hitCost
              break; //get out of the loop, bc its a hit
            }
          } else if (i == cache[setNum].size() - 1) { //if we are at the last line and there has been no hit
              line = readLineFromMemory(address, setNum, tag); //read the line from memory and set it into the line
          }
          data = line.getData(); //get the data from the line
        }
        int x = 0; //iteration variable for the 2d array to 1d array
        //loop thru the data 2d array
        for (i = 0; i < data.length; i++) {
          for (int j = 0; j < data[i].length; j++) {
            tempData[x] = data[i][j]; //put the data into the 1d array
          }
          x++;
        }
        requestCount++; //increment request count each time the method is called
        return tempData; //return the 1d array of data
    }
    /**
    * STUDENT MUST COMPLETE THIS METHOD
    *
    * Copies a line of data from memory into cache. Selects the cache line to
    * replace. Uses the Least Recently Used (LRU) algorithm when a choice must
    * be made between multiple cache lines that could be replaced.
    *
    * @param address The requested memory address.
    * @param setNum The set number where the address maps to.
    * @param tagBits The tag bits from the memory address.
    *
    * @return The line that was read from memory. This line is also written
    * into the cache.
    */
    private CacheLine readLineFromMemory(boolean[] address, int setNum, boolean[] tagBits) {

        // Use the LRU (least recently used) replacement scheme to select a line
        // within the set.
        CacheLine selectedLine = cache[setNum].getLine(0); //set current selectedLine to the first line in the given set in the cache
        int lru = selectedLine.getLastUsed(); //get the last used of that line and store it into a variable
        boolean[] bytes = Arrays.copyOfRange(address, 0, numByteBits); //array to hold bytes
        boolean[] set = Arrays.copyOfRange(address, numByteBits, numByteBits+numSetBits); //array to hold set
        boolean[] tag = Arrays.copyOfRange(address, numSetBits+numByteBits, address.length); //array to hold the tag
        //loop thru the cache at a given set
        for (int x = 1; x < cache[setNum].size(); x++) {
          CacheLine line = cache[setNum].getLine(x); //get the current line
            if (line.getLastUsed() < lru) { //if the line's last used is less than lru
              lru = line.getLastUsed(); //set lru = line's last used
              selectedLine = cache[setNum].getLine(x); //set selectedLine equal to the current line
            }
        }
        //set the byte bits to zero
        for (int i = 0; i < numByteBits; i++) {
          address[i] = false;
        }
        // Read the line from memory. The memory address to read is the
        // first memory address of the line that contains the requested address.
        // The MainMemory read method should be called.
        boolean[][] tempLine = mainMemory.read(address, selectedLine.size());
        // Copy the line read from memory into the cache
        //set the CacheLine data members
        selectedLine.setData(tempLine);
        selectedLine.setValid();
        selectedLine.setTag(tag);
        selectedLine.setLastUsed(requestCount);
        // replace this placeholder return with the correct line to return
        return selectedLine;
    }

}

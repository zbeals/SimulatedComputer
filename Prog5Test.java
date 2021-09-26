/**
* Test program for the Cache Memory Simulation
*/
import java.util.*;
import java.io.*;

public class Prog5Test {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        String dataFile = "example.data";
        int cacheSize; // size of the cache in Bytes
        int lineSize; // size of one cache line in Bytes
        int linesPerSet; // number of cache lines per set

        int numReads = 100000; // number of read requests to perform in the large cache experiments

        Random random = new Random(3182019); // A random number generator object

        // Create a MainMemory object
        MainMemory mainMemory = new MainMemory(dataFile);
        int mainMemSize = mainMemory.getSize();
        System.out.println("Size of Main Memory in Bytes: " + mainMemSize);


        /////////////////// Small Cache Test //////////////////////////////////

        System.out.println("*********** Small Cache ***********");
        // Create a small set-associative cache
        // This is similar to the cache in Lab 8 Problem 3
        // 2 lines per set (this is a 2-way set associative cache)
        // 4 Bytes per line
        // Cache size is 32 Bytes
        cacheSize = 32;
        lineSize = 4;
        linesPerSet = 2;
        CacheMemory smallCache = new CacheMemory(mainMemory, cacheSize, lineSize, linesPerSet);
        smallTest(smallCache);
        System.out.println("Report from small cache test: ");
        smallCache.reportStats();
        System.out.println();



        /////////////////// Cache Performance Tests //////////////////////////

        // Student should comment out these performance tests (from here until
        // the end of the main method) until after the small test is working.
        //
        // The performance tests of cache associativity all use the same
        // cache size and line size.
        cacheSize = 2048;
        lineSize = 32;
        int warmUpReads = 100000;

        /////////////////// Test Set-Associative Cache ////////////////////////

        // Create a set-associative cache
        // 4 lines per set (this is a 4-way set associative cache)
        System.out.println();
        System.out.println("*********** Set-Associative Cache ***********");
        CacheMemory cacheMemoryA;
        linesPerSet = 4;
        cacheMemoryA = new CacheMemory(mainMemory, cacheSize, lineSize, linesPerSet);
        cacheMemoryA.warmUp(warmUpReads, random);
        randomReads(cacheMemoryA, numReads, mainMemSize, random);
        System.out.println("Report from cacheMemoryA with randomReads:");
        cacheMemoryA.reportStats();
        System.out.println();

        cacheMemoryA = new CacheMemory(mainMemory, cacheSize, lineSize, linesPerSet);
        cacheMemoryA.warmUp(warmUpReads, random);
        sequentialReads(cacheMemoryA, mainMemSize);
        System.out.println("Report from cacheMemoryA with sequentialReads:");
        cacheMemoryA.reportStats();
        System.out.println();

        cacheMemoryA = new CacheMemory(mainMemory, cacheSize, lineSize, linesPerSet);
        cacheMemoryA.warmUp(warmUpReads, random);
        repeatReads(cacheMemoryA, numReads, mainMemSize, random);
        System.out.println("Report from cacheMemoryA with repeatReads:");
        cacheMemoryA.reportStats();
        System.out.println();


        /////////////////// Test Direct Mapped Cache /////////////////////////

        // Create a direct mapped cache
        System.out.println();
        System.out.println("*********** Direct Mapped Cache ***********");
        CacheMemory cacheMemoryB;
        linesPerSet = 1;
        cacheMemoryB = new CacheMemory(mainMemory, cacheSize, lineSize, linesPerSet);
        cacheMemoryB.warmUp(warmUpReads, random);
        randomReads(cacheMemoryB, numReads, mainMemSize, random);
        System.out.println("Report from cacheMemoryB with randomReads:");
        cacheMemoryB.reportStats();
        System.out.println();

        cacheMemoryB = new CacheMemory(mainMemory, cacheSize, lineSize, linesPerSet);
        cacheMemoryB.warmUp(warmUpReads, random);
        sequentialReads(cacheMemoryB, mainMemSize);
        System.out.println("Report from cacheMemoryB with sequentialReads:");
        cacheMemoryB.reportStats();
        System.out.println();

        cacheMemoryB = new CacheMemory(mainMemory, cacheSize, lineSize, linesPerSet);
        cacheMemoryB.warmUp(warmUpReads, random);
        repeatReads(cacheMemoryB, numReads, mainMemSize, random);
        System.out.println("Report from cacheMemoryB with repeatReads:");
        cacheMemoryB.reportStats();
        System.out.println();


        /////////////////// Test Fully Associative Cache ///////////////////////

        // Create a fully associative cache (all lines in 1 set)
        System.out.println();
        System.out.println("*********** Fully Associative Cache ***********");
        CacheMemory cacheMemoryC;
        linesPerSet = 64; // 2048 Bytes in cache / 32 bytes per line = 64 lines in cache
        cacheMemoryC = new CacheMemory(mainMemory, cacheSize, lineSize, linesPerSet);
        cacheMemoryC.warmUp(warmUpReads, random);
        randomReads(cacheMemoryC, numReads, mainMemSize, random);
        System.out.println("Report from cacheMemoryC with randomReads:");
        cacheMemoryC.reportStats();
        System.out.println();

        cacheMemoryC = new CacheMemory(mainMemory, cacheSize, lineSize, linesPerSet);
        cacheMemoryC.warmUp(warmUpReads, random);
        sequentialReads(cacheMemoryC, mainMemSize);
        System.out.println("Report from cacheMemoryC with sequentialReads:");
        cacheMemoryC.reportStats();
        System.out.println();

        cacheMemoryC = new CacheMemory(mainMemory, cacheSize, lineSize, linesPerSet);
        cacheMemoryC.warmUp(warmUpReads, random);
        repeatReads(cacheMemoryC, numReads, mainMemSize, random);
        System.out.println("Report from cacheMemoryC with repeatReads:");
        cacheMemoryC.reportStats();
        System.out.println();
    
     } // end of main method

    /**
    * A small test of the cache memory.
    * Similar to Lab 8 Problem 3.
    * @param cache The CacheMemory object to read from.
    */
    public static void smallTest(CacheMemory cache) {
        boolean[] address;

        // Memory address in Lab 8 is the line address.
        // Add two bits to the least significant side of the Lab 8
        // address to get the memory Byte address.

        address = Binary.uDecToBin(4L, 32); // Operation 1: address 000100
        cache.readWord(address);

        address = Binary.uDecToBin(16L, 32); // Operation 2: address 010000
        cache.readWord(address);

        address = Binary.uDecToBin(24L, 32); // Operation 3: address 011000
        cache.readWord(address);

        address = Binary.uDecToBin(40L, 32); // Operation 4: address 101000
        cache.readWord(address);

        address = Binary.uDecToBin(60L, 32); // Operation 5: address 111100
        cache.readWord(address);

        address = Binary.uDecToBin(32L, 32); // Operation 6: address 100000
        cache.readWord(address);

        address = Binary.uDecToBin(0L, 32); // Operation 7: address 000000
        cache.readWord(address);

        address = Binary.uDecToBin(52L, 32); // Operation 8: address 110100
        cache.readWord(address);

        address = Binary.uDecToBin(28L, 32); // Operation 9: address 011100
        cache.readWord(address);

        address = Binary.uDecToBin(0L, 32); // Operation 10: address 000000
        cache.readWord(address);

        address = Binary.uDecToBin(32L, 32); // Operation 11: address 100000
        cache.readWord(address);

        address = Binary.uDecToBin(44L, 32); // Operation 12: address 101100
        cache.readWord(address);

        address = Binary.uDecToBin(0L, 32); // Operation 13: address 000000
        cache.readWord(address);

        address = Binary.uDecToBin(60L, 32); // Operation 14: address 111100
        cache.readWord(address);

    }

    /**
    * Calls the CacheMemory readWord method with random addresses
    *
    * @param cache The CacheMemory object to read from.
    * @param numReads The number of read operations to perform.
    * @param memSize The size of main memory
    * @param random A random number generator object.
    */
    public static void randomReads(CacheMemory cache, int numReads, int memSize, Random random) {

        int wordsInMainMem = (memSize / CacheMemory.WORD_SIZE);

        for(int i = 0; i < numReads; i++) {
            // Generate a random line address
            int wordAddress = random.nextInt(wordsInMainMem) * CacheMemory.WORD_SIZE;
            boolean[] address = Binary.uDecToBin(wordAddress, 32);
            cache.readWord(address);
        }

    }

    /**
    * Sequentially reads the memory
    *
    * @param cache The CacheMemory object to read from.
    * @param memSize The size of main memory
    */
    public static void sequentialReads(CacheMemory cache, int memSize) {

        int wordsInMainMem = memSize / CacheMemory.WORD_SIZE;
        int addressInt = 0;
        boolean[] address;

        for(int i = 0; i < memSize; i+=CacheMemory.WORD_SIZE) {
            address = Binary.uDecToBin(i, 32);
            cache.readWord(address);
        }
    }

    /**
    * Calls the CacheMemory readWord method with some repeated addresses
    *
    * @param cache The CacheMemory object to read from.
    * @param numReads The number of read operations to perform.
    * @param memSize The size of main memory
    * @param random A random number generator object.
    */
    public static void repeatReads(CacheMemory cache, int numReads, int memSize, Random random) {

        int wordsInMainMem = (memSize / CacheMemory.WORD_SIZE);
        int wordAddress;

        // Set some addresses for repeated reads
        wordAddress = random.nextInt(wordsInMainMem) * CacheMemory.WORD_SIZE;
        boolean[] addressA = Binary.uDecToBin(wordAddress, 32);

        wordAddress = random.nextInt(wordsInMainMem) * CacheMemory.WORD_SIZE;
        boolean[] addressB = Binary.uDecToBin(wordAddress, 32);

        wordAddress = random.nextInt(wordsInMainMem) * CacheMemory.WORD_SIZE;
        boolean[] addressC = Binary.uDecToBin(wordAddress, 32);

        wordAddress = random.nextInt(wordsInMainMem) * CacheMemory.WORD_SIZE;
        boolean[] addressD = Binary.uDecToBin(wordAddress, 32);


        for(int i = 0; i < numReads; i++) {

            // Every 10 reads, read the repeat addresses
            if(i % 10 == 0) {
                cache.readWord(addressA);
                cache.readWord(addressB);
                cache.readWord(addressC);
                cache.readWord(addressD);

                // Increment i by the three extra reads
                i += 3;
            } else {
                // Generate a random word address
                wordAddress = random.nextInt(wordsInMainMem) * CacheMemory.WORD_SIZE;
                boolean[] address = Binary.uDecToBin(wordAddress, 32);
                cache.readWord(address);
            }
        }
    }
}

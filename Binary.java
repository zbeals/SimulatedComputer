/**
* Methods for converting between binary and decimal.
*
* @author Zoe Beals
*/
public class Binary {

    /** Constant defines the maximum length of binary numbers. */
    private static final int MAX_LENGTH = 32;

    /** boolean value to hold if the first 1 in simple negation was copied over */
    private static boolean found = false;

    /** index variable to hold the position of first 1 in simple negation process */
    private static int index = 0;

    /** binary array to hold a binary num*/
    private static boolean binary[];

    /**
    * Converts a two's complement binary nubmer to signed decimal
    *
    * @param b The two's complement binary number
    * @return The equivalent decimal value
    * @exception IllegalArgumentException Parameter array length is longer than MAX_LENGTH.
    */
    public static long binToSDec(boolean[] b) {
        // PROGRAM 1: Student must complete this method
        // return value is a placeholder, student should replace with correct return
        boolean[] neg = new boolean[b.length]; // boolean array to hold the simple negation reresentation of binary number
        int index = 0; // index value of the first 1 of the bianry array.
        // Example of throwing an IllegalArgumentException
        // Student must write code for the required exceptions in other methods.
        // If the exception condition is true, throw the exception
        if(b.length > MAX_LENGTH) {
            // If the condition is true, the exception will be thrown
            // and the method execution will stop.
            throw new IllegalArgumentException("parameter array is longer than " + MAX_LENGTH + " bits.");
        }
        //check the sign bit
        if (b[MAX_LENGTH - 1]) { //if its negative,
          neg = simpleNegation(b);
          //invert bits
          for (int i = index; i < b.length; i++) {
            neg[i] = !b[i];
          }
          //return the neg rep of the decimal number
           return (binToUDec(neg) + 1) * -1;
        } else {
          return binToUDec(b);
        }
    }

    /**
    * Converts an unsigned binary number to unsigned decimal
    *
    * @param b The unsigned binary number
    * @return The equivalent decimal value
    * @exception IllegalArgumentException Parameter array length is longer than MAX_LENGTH.
    */
    public static long binToUDec(boolean[] b) {
        // PROGRAM 1: Student must complete this method
        // return value is a placeholder, student should replace with correct return
        //throw exception if the parameter array is invalid length
        if (b.length > MAX_LENGTH) {
          throw new IllegalArgumentException("Invalid array length");
        }
        long decimal = 0; //long to hold decimal value
        //compute the unsigned decimal of the given binary number
        for (int i = 0; i < b.length; i++) {
          if (b[i]) { //if we come across a 1,
            decimal += (long)Math.pow(2, i); // add (1 x 2^(p.v) to running decimal number
          }
        }
        return decimal; //return the decimal number
    }

    /**
    * Converts a signed decimal nubmer to two's complement binary
    *
    * @param d The decimal value
    * @param bits The number of bits to use for the binary number.
    * @return The equivalent two's complement binary representation.
    * @exception IllegalArgumentException Parameter is outside valid range that can be represented with the given number of bits.
    */
    public static boolean[] sDecToBin(long d, int bits) {
        // PROGRAM 1: Student must complete this method
        // return value is a placeholder, student should replace with correct return
        boolean[] binaryInv = new boolean[bits];
        //throw the exception if the decimal value is out of range
        if (d > 2147483647L || d < -2147483648L) {
          throw new IllegalArgumentException("Invalid length");
        }
        //if d is positive
        if (d > 0) {
          return uDecToBin(d, bits); //return unsigned binary version of decimal number
        } else { //if d is negative,
          long dPos = Math.abs(d); //get the positive rep of the decimal num
          binary = uDecToBin(dPos, bits); //get the binary rep of the positive rep of decimal number
          //begin simple negation, using invert method to avoid repetition
          binaryInv = simpleNegation(binary);
          //invert the remaining bits beginning at index after first 1
          for (int i = index; i < binaryInv.length; i++) {
            binaryInv[i] = !binary[i];
          }
        }
        //begin adding one to the inverted binary array (last part of simple negation)
        int carry = 1; //represents carry in bit
        for (int j = binaryInv.length - 1; j >= 0; j--) {
          if (!binaryInv[j] && carry == 1) { //if there is a carryin bit to add to 0
            binaryInv[j] = true; //add 0 + 1
            carry = 0; //set carry in to 0
          } else if (binaryInv[j] && carry == 1) { //if therre is a carry in bit to add to 1
            binaryInv[j] = false; //add 1 + 1
            carry = 1; //update carry in to 1
          }
        }
        return binaryInv; //return the finalized simply negated version of binary num.
    }
    /**
    * Converts an unsigned decimal nubmer to binary
    *
    * @param d The decimal value
    * @param bits The number of bits to use for the binary number.
    * @return The equivalent binary representation.
    * @exception IllegalArgumentException Parameter is outside valid range that can be represented with the given number of bits.
    */
    public static boolean[] uDecToBin(long d, int bits) {
        // PROGRAM 1: Student must complete this method
        // return value is a placeholder, student should replace with correct return
        binary = new boolean[bits]; //set binary array to size of bits
        //if d is out of range throw an exception
        if (d < 0 || d > 4294967295L) {
          throw new IllegalArgumentException("Invalid number of bits");
        }
        //working from MSB to LSB
        for (int i = bits - 1; i >= 0; i--) {
          //use the quotient and remainder system to keep track of the remainders and place them into the binary array
          if (d % 2 == 1) {
            binary[i] = true; //if there is a remainder of 1
          } else {
            binary[i] = false; //if there is no remainder
          }
          d /= 2; //divde decimal num by 2 each iteration
        }
        return binary; //once quotient reaches 0, return the binary array
    }

    /**
    * Returns a string representation of the binary number. Uses an underscore
    * to separate each group of 4 bits.
    *
    * @param b The binary number
    * @return The string representation of the binary number.
    */
    public static String toString(boolean[] b) {
        // PROGRAM 1: Student must complete this method
        // return value is a placeholder, student should replace with correct return
        int[] binNum = new int[b.length]; //integer array to save me the step of converting from boolean to integer to string
        String binaryRep = ""; //string to return
        for (int i = 0; i < b.length; i++) {
          if (b[i]) {
            binNum[i] = 1; //if index i is true, it is 1
          } else {
            binNum[i] = 0; //if index i is false, it is 0
          }
        }
        for (int j = 0; j < binNum.length; j++) {
          binaryRep += String.valueOf(binNum[j]); //get the String rep of each integer index in binNum array and add it to the string
        }
        return helperString(binaryRep); //call helperString method to add the underscore formatting and return result
    }

    /**
    * Returns a hexadecimal representation of the unsigned binary number. Uses
    * an underscore to separate each group of 4 characters.
    *
    * @param b The binary number
    * @return The hexadecimal representation of the binary number.
    */
    public static String toHexString(boolean[] b) {
        // PROGRAM 1: Student must complete this method
        // return value is a placeholder, student should replace with correct return
        //char array to hold the hexadecimal reps
        char[] hexVal = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        String hex = ""; //hexadecimal string rep to return
        long decimal = binToUDec(b); //get the decimal version of unsigned binary number
        while (decimal > 0) { //begin the / 16 tactice using quotients and remainders
          hex += hexVal[(int)(decimal % 16)]; //get the character at the position of a remainder and add it to the string rep
          decimal /= 16; //divide the decimal num by 16
        }
        return helperString(hex); //call helper String method to add the underscore formatting and return result
    }

    /**
    * Returns a formatted String representation of the converted Binary or Hexadecimal String.
    * Uses an underscore to separate each group of 4 characters.
    * Used an external method to avoid repeating code.
    * @param s the binary or hexadecimal String
    * @return the formatted string representaion with underscores.
    */
    public static String helperString(String s) {
      StringBuilder helper = new StringBuilder(s); //declare a String Builder to be able to add underscores
      int underscore = helper.length() - 4; //create an underscore index for placing an _ char every 4 binary nums
      while (underscore > 0) {
        helper.insert(underscore, "_");
        underscore -= 4; //decrement by 4 to format correctly
      }
      return helper.toString(); //return the formatted binary or hexadecimal string
    }

    /**
    * Returns the inverted binary representation of a binary array
    * Used an external method to avoid repeating code
    * @param b the binary array
    * @return the simple negation representation of b
    */
    public static boolean[] simpleNegation(boolean[] b) {
      boolean[] binaryInv = new boolean[b.length]; //boolean array to hold the inverted rep of the binary array
      for (int i = 0; i < b.length; i++) {
        //copy bits into binaryInv until a 1 is copied
        if (!found) {
          binaryInv[i] = b[i]; //copy bits
        } else if (b[i]) {
          found = true; //set found variable to true
          binaryInv[i] = b[i]; //copy 1 bit over
          index = i; //get the index of first 1
          break;
        }
      }
      return binaryInv; //return simple neg rep
    }
}

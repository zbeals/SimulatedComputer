import java.lang.Math;
import java.util.Random;

/**
* Simulates the arithmetic and logic unit (ALU) of a processor. Follows the
* ARMv8 architecture, with the exception of the number of bits used for input
* and output values. Uses the BINARY_LENGTH constant from the Binary class as
* the nubmer of bits for inputs and output.
*
* The ALU must be implemented using logic operations (AND, OR, NOT) on each
* set of input bits because the goal of this assignment is to learn about how
* a computer processor uses logic gates to perform arithmetic. The Java
* arithmetic operations should not be used in this class.
*
* @author Zoe Beals
*/
public class ALU {

    /** Number of bits used to represent an integer in this ALU */
    public static final int INT_LENGTH = 32;

    /** Input A: an INT_LENGTH bit binary value */
    private boolean[] inputA;

    /** Input B: an INT_LENGTH bit binary value */
    private boolean[] inputB;

    /** Output: an INT_LENGTH bit binary value */
    private boolean[] output;

    /** ALU Control input */
    private int control;

    /** Zero flag */
    private boolean zeroFlag;

    /** Carry flag */
    private boolean carryFlag;

    /** Overflow flag */
    private boolean overflowFlag;

    /** boolean array to hold sum and carry-out of add/sub operations */
    private boolean[] adder = new boolean[2];

    /** initialize the random instance */
    private Random random = new Random();

    /** boolean value to represent the carry-in during add/sub operations */
    private boolean cin;

    /**
    * Constructor initializes inputs and output to random binary values,
    * intializes control to 15, initializes zero flag to false.
    * Inputs and output arrays should have length INT_LENGTH.
    */
    public ALU() {
        // PROGRAM 1: Student must complete this method
        //declare input(s) and output arrays
        inputA = new boolean[INT_LENGTH];
        inputB = new boolean[INT_LENGTH];
        output = new boolean[INT_LENGTH];
        //initializes zeroFlag and control
        zeroFlag = false;
        control = 15;
        //initialize each array with random boolean values
        for (int i = 0; i < INT_LENGTH; i++) {
          inputA[i] = random.nextBoolean();
          inputB[i] = random.nextBoolean();
          output[i] = random.nextBoolean();
        }
    }
    /**
    * Sets the value of inputA.
    *
    * @param b The value to set inputA to
    *
    * @exception IllegalArgumentException if array b does not have length
    * INT_LENGTH
    */
    public void setInputA(boolean[] b) {
        // PROGRAM 1: Student must complete this method
        //if param array != INT_LENGTH, throw exception
        if (b.length != INT_LENGTH) {
          throw new IllegalArgumentException("Invalid array length");
        } else {
          //otherwise, place the parameter array's data into inputA
          for (int i = 0; i < b.length; i++) {
            inputA[i] = b[i];
          }
        }
    }

    /**
    * Sets the value of inputB.
    *
    * @param b The value to set inputB to
    *
    * @exception IllegalArgumentException if array b does not have length INT_LENGTH
    */
    public void setInputB(boolean[] b) {
        // PROGRAM 1: Student must complete this method
        //if param array != INT_LENGTH, throw exception
        if (b.length != INT_LENGTH) {
          throw new IllegalArgumentException("Invalid array length");
        } else {
          //otherwise, place the parameter array's data into inputB
          for (int i = 0; i < b.length; i++) {
            inputB[i] = b[i];
          }
        }
    }

    /**
    * Sets the value of the control line to one of the following values. Note
    * that we are not implementing all possible control line values.
    * 0 for AND;
    * 1 for OR;
    * 2 for ADD;
    * 6 for SUBTRACT;
    * 7 for PASS INPUT B.
    *
    * @param c The value to set the control to.
    * @exception IllegalArgumentException if c is not 0, 1, 2, 6, or 7.
    */
    public void setControl(int c) {
        // PROGRAM 1: Student must complete this method
        //if c is on of the accepted controls, set control to c.
        if (c == 0 || c == 1 || c == 2 || c == 6 || c == 7) {
    		control = c;
    	} else {
        //otherwise, throw exception
    		throw new IllegalArgumentException("Control value invalid");
    	}
    }

    /**
    * Returns a copy of the value in the output.
    *
    * @return The value in the output
    */
    public boolean[] getOutput() {
        // PROGRAM 1: Student must complete this method
        // return value is a placeholder, student should replace with correct return
        boolean[] outputCopy = new boolean[output.length]; //array to hold copy of output.
        for (int i = 0; i < outputCopy.length; i++) {
          outputCopy[i] = output[i]; //place output data into outputCopy
        }
        return outputCopy; //return copy of output
    }

    /**
    * Returns the value of the zero data member. The zero data member should
    * have been set to true if the result of the operation was zero.
    *
    * @return The value of the zeroFlag data member
    */
    public boolean getZeroFlag() {
        // PROGRAM 1: Student must complete this method
        // return value is a placeholder, student should replace with correct return
        int zero = 0; //integer to keep track of how many '1's are in the output array
        for (int i = 0; i < output.length; i++) {
          if (output[i]) { //when we come across a 1, add 1 to the zero integer.
            zero += 1;
          }
        }
        if (zero > 0) { //if the method caught any '1's, then the output binary number is not 0.
          zeroFlag = false;
        } else {
          zeroFlag = true; //otherwise, it is
        }
        return zeroFlag; //return the zeroFlag
    }

    /**
    * Returns the value of the carryFlag data member. The carryFlag data member
    * is set to true if the ALU addition operation has a carry out of the most
    * significant bit.
    *
    * @return The value of the carryFlag data member
    */
    public boolean getCarryFlag() {
        // PROGRAM 1: Student must complete this method
        // return value is a placeholder, student should replace with correct return
        //if index of carry out in the array holding the result from add/sub operation is true,
        if (adder[1]) {
          carryFlag = true; //there is a carry out.
        } else {
          carryFlag = false; //otherwise, there is not.
        }
        return carryFlag; //return the carryFlag
    }

    /**
    * Returns the value of the overflowFlag data member. The overflowFlag data
    * member is set to true if the ALU addition operation has a result that
    * is overflow when the operands are signed integers.
    *
    * @return The value of the overflowFlag data member
    */
    public boolean getOverflowFlag() {
        // PROGRAM 1: Student must complete this method
        // return value is a placeholder, student should replace with correct return
        if (Binary.binToSDec(inputA) > 0 && Binary.binToSDec(inputB) > 0) { //if the decimal rep of both inputs are positive,
          if (Binary.binToSDec(output) < 0) { // and the output is negative,
            overflowFlag = true; //overflow has occured
          }
        } else if (Binary.binToSDec(inputA) < 0 && Binary.binToSDec(inputB) < 0) { //if the decimal rep of both inputs are negative,
          if (Binary.binToSDec(output) > 0) { //and the output is positive
            overflowFlag = true; //overflow has occured
          }
        } else {
          overflowFlag = false; //otherwise, overflow has not occured.
        }
        return overflowFlag; //return the value of overflowFlag
    }

    /**
    * Activates the ALU so that the ALU performs the operation specified by
    * the control data member on the two input values. When this method is
    * finished, the output data member contains the result of the operation.
    *
    * @exception RuntimeException if the control data member is not set to
    * a valid operation code.
    */
    public void activate() {
        // PROGRAM 1: Student must complete this method
        if (control == 0) { // run and()
      		and();
      	} else if (control == 1) { //run or()
      		or();
      	} else if (control == 2) { //run add()
      		add();
      	} else if (control == 6) { //run sub()
      		sub();
      	} else if (control == 7) { //run passB()
      		passB();
      	} else {
      		throw new RuntimeException("invalid control"); //otherwise, there was an invalid control
      	}
    }

    /**
    * Performs the bitwise AND operation: output = inputA AND inputB
    */
    private void and() {
        // PROGRAM 1: Student must complete this method
        //loop through the output array
        for (int i = 0; i < output.length; i++) {
          //take the and of index i of inputA and inputB and place result in output[i]
          output[i] = inputA[i] & inputB[i];
        }
    }

    /**
    * Performs the bitwise OR operation: output = inputA OR inputB
    */
    private void or() {
        // PROGRAM 1: Student must complete this method
        //loop through output array
        for (int i = 0; i < output.length; i++) {
          //take the or of index i of inputA and inputB and place result in output[i]
          output[i] = (inputA[i] | inputB[i]);
        }
    }

    /**
    * Performs the addition operation using ripple-carry addition of each bit:
    * output = inputA + inputB
    */
    private void add() {
        // PROGRAM 1: Student must complete this method
        // This method must use the addBit method for bitwise addition.
        adder = addBit(inputA[0],inputB[0], false); //begin adding, no carry in
        output[0] = adder[0]; //place sum of first addBit iteration into output[0]
        //loop thru output beginning at index 1 (since we already computed index 0)
        for (int i = 1; i < output.length; i++) {
          cin = adder[1]; //set carry-out bit of addBit() iteration to cin
          adder = addBit(inputA[i], inputB[i], cin); //call addBit with index i of inputA and inputB and cin and place into adder.
          output[i] = adder[0]; //place sum into output[i]
        }
    }

    /**
    * Performs the subtraction operation using a ripple-carry adder:
    * output = inputA - inputB
    * In order to perform subtraction, set the first carry-in to 1 and invert
    * the bits of inputB.
    */
    private void sub() {
        // PROGRAM 1: Student must complete this method
        // This method must use the addBit method for bitwise subtraction.
        //invert bits of inputB
        for (int i = 0; i < inputB.length; i++) {
          if (!inputB[i]) {
            inputB[i] = true; //set each 0 bit to 1
          } else {
            inputB[i] = false; //set each 1 bit to 0
          }
        }
        adder = addBit(inputA[0], inputB[0], true); //place first iteration of addBit with a carry-in bit into adder.
        output[0] = adder[0]; //place first sum into output[0]
        //loop thru output starting at index 1 since we already computed
        for (int i = 1; i < output.length; i++) {
          cin = adder[1]; // set cin equal to carryout
          adder = addBit(inputA[i], inputB[i], cin); //call addBit with index i of inputA and inputB and cin
          output[i] = adder[0]; //place sum into output[i]
        }
    }

    /**
    * Copies inputB to the output: output = inputB
    */
    private void passB() {
        // PROGRAM 1: Student must complete this method
        //place inputB data into output
        for (int i = 0; i < output.length; i++) {
          output[i] = inputB[i];
        }
    }

    /**
    * Simulates a 1-bit adder.
    *
    * @param a Represents an input bit
    * @param b Represents an input bit
    * @param c Represents the carry in bit
    * @return An array of length 2, index 0 holds the output bit and index 1
    * holds the carry out
    */
    private boolean[] addBit(boolean a, boolean b, boolean c) {
        // PROGRAM 1: Student must complete this method
        // This method may only use the Java logic operations && (logical and),
        // || (logical or), and ! (logical not). Do not use any Java arithmetic
        // operators in this method.
        boolean[] result = new boolean[2];
        result[0] = (a ^ b) ^ c; // (a xor b) xor c -> yields the sum
        result[1] = (a && b) || (a ^ b) && c; // (a and b) or (a xor b) and c -> yields the carry-out
        // return value is a placeholder, student should replace with correct return
        return result;
    }

}

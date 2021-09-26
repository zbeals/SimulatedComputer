/**
* Represents a simple CPU based on the ARMv8 datapath.
*
* CS318 Programming Assignment 4
* Name: Zoe Beals
*
*/
import java.io.*;
import java.util.Arrays;

public class CPU {

    /** Memory unit for instructions */
    private Memory instructionMemory;

    /** Memory unit for data */
    private Memory dataMemory;

    /** Register unit */
    private Registers registers;

    /** Arithmetic and logic unit */
    private ALU alu;

    /** Adder for incrementing the program counter */
    private ALU adderPC;

    /** Adder for computing branches */
    private ALU adderBranch;

    /** Control unit */
    private SimpleControl control;

    /** Multiplexor output connects to Read Register 2 */
    private Multiplexor2 muxRegRead2;

    /** Mulitplexor ouptut connects to ALU input B */
    private Multiplexor2 muxALUb;

    /** Multiplexor output connects to Register Write Data */
    private Multiplexor2 muxRegWriteData;

    /** Multiplexor output connects to Program Counter */
    private Multiplexor2 muxPC;

    /** Program counter */
    private boolean[] pc;

    /**
    * STUDENT SHOULD NOT MODIFY THIS METHOD
    *
    * Constructor initializes all data members.
    *
    * @param iMemFile Path to the file with instruction memory contents.
    * @param dMemFile Path to the file with data memory contents.
    * @exception FileNotFoundException if a file cannot be opened.
    */
    public CPU(String iMemFile, String dMemFile) throws FileNotFoundException {

        // Create objects for all data members
        instructionMemory = new Memory(iMemFile);
        dataMemory = new Memory(dMemFile);
        registers = new Registers();
        alu = new ALU();
        control = new SimpleControl();
        muxRegRead2 = new Multiplexor2(5);
        muxALUb = new Multiplexor2(32);
        muxRegWriteData = new Multiplexor2(32);
        muxPC = new Multiplexor2(32);

        // Activate adderPC with ADD operation, and inputB set to 4
        // Send adderPC output to muxPC input 0
        adderPC = new ALU();
        adderPC.setControl(2);
        boolean[] four = Binary.uDecToBin(4L, 32);
        adderPC.setInputB(four);

        // Initalize adderBranch with ADD operation
        adderBranch = new ALU();
        adderBranch.setControl(2);

        // initialize program counter to 0
        pc = new boolean[32];
        for(int i = 0; i < 32; i++) {
            pc[i] = false;
        }
    }

    /**
    * STUDENT SHOULD NOT MODIFY THIS METHOD
    *
    * Runs the CPU in single cycle (non-pipelined) mode. Stops when a halt
    * instruction is decoded.
    *
    * This method can be used with any (assembled) assembly language program.
    */
    public void singleCycle() {

        int cycleCount = 0;

        // Start the first cycle.
        boolean[] instruction = fetch();
        boolean op = decode(instruction);

        // Loop until a halt instruction is decoded
        while(op) {
            execute();

            memoryAccess();

            writeBack();

            cycleCount++;

            // Start the next cycle
            instruction = fetch();

            op = decode(instruction);
        }

        System.out.println("CPU halt after " + cycleCount + " cycles.");
    }

    /**
    * STUDENT MUST ADD MORE TESTING CODE TO THIS METHOD AS INDICATED BY
    * COMMENTS WIHTIN THIS METHOD.
    *
    * DO NOT CHANGE the calls to the CPU private methods.
    *
    * The comments in this method indicate the minimum amount of testing code
    * that you must add. You are encouraged to add additional testing code
    * to help you develop and verify the correctness of the CPU private methods.
    * Tests for the first instruction in testProg3.s are included as an
    * example of how to test the correctness of the CPU private methods.
    *
    * Runs the CPU in single cycle (non-pipelined) mode. Stops when a halt
    * instruction is decoded.
    *
    * This method should only be used with the assembled testProg3.s
    * because this method verifies correct values based on that specific program.
    */
    public void runTestProg3() {

        int cycleCount = 0;

        // Start the first cycle.
        boolean[] instruction = fetch();

        // Example Test: Verify that when cycleCount is 0 the insruction returned by fetch is the
        // binary version of the first instruction from testProg3.s ADD R9,R31,R31
        boolean[] firstInstr = {true,false,false,true,false,true,true,true,true,true,false,false,false,false,false,false,true,true,true,true,true,false,false,false,true,true,false,true,false,false,false,true};
        if(cycleCount == 0 && !Arrays.equals(instruction,firstInstr)) {
            System.out.println("FAIL: cycle " + cycleCount + " did not fetch correct instruction:");
            System.out.println("------ fetch returned: " + Binary.toString(instruction));
            System.out.println("------ correct instruction: " + Binary.toString(firstInstr));
        }

        boolean op = decode(instruction);

        // Example Test: Verify that when cycleCount is 0 the control signals
        // are correctly set for an ADD instruction
        if(cycleCount == 0 && (control.Uncondbranch != false || control.RegWrite != true
            || control.Reg2Loc != false || control.MemWrite != false || control.MemtoReg != false
            || control.MemRead != false || control.Branch != false || control.ALUSrc != false
            || control.ALUControl != 2))
        {
            System.out.println("FAIL: cycle " + cycleCount + " after decode, control lines incorrect");
        }

        // Loop until a halt instruction is decoded
        while(op) {
            execute();

            // Example Test: Verify that when cycleCount is 0 the ALU result is zero
            boolean[] correctALU = Binary.uDecToBin(0L, 32);
            if(cycleCount == 0 && !Arrays.equals(alu.getOutput(), correctALU)) {
                System.out.println("FAIL: cycle " + cycleCount + " incorrect ALU result:");
                System.out.println("------ ALU result: " + Binary.toString(alu.getOutput()));
                System.out.println("------ correct result: " + Binary.toString(correctALU));
            }

            // ***** PROG. 4 STUDENT MUST ADD
            // Test that when cycleCount is 1, the ALU result is the correct
            // data memory address (should be 16)
            correctALU = Binary.uDecToBin(16L, 32);
            if (cycleCount == 1 && !Arrays.equals(alu.getOutput(), correctALU)) {
              System.out.println("FAIL: cycle " + cycleCount + " incorrect ALU result:");
              System.out.println("------ ALU result: " + Binary.toString(alu.getOutput()));
              System.out.println("------ correct result: " + Binary.toString(correctALU));
            }

            // ***** PROG. 4 STUDENT MUST ADD
            // Test that when cycleCount is 6, the branch adder's (adderBranch)
            // result is the offset of the branch destination instruction (should be 32)
            boolean[] correctAdderBranch = Binary.uDecToBin(32L, 32);
            if(cycleCount == 6 && !Arrays.equals(adderBranch.getOutput(), correctAdderBranch)) {
              System.out.println("FAIL: cycle " + cycleCount + " incorrect Adder Branch result:");
              System.out.println("------ Adder Branch result: " + Binary.toString(adderBranch.getOutput()));
              System.out.println("------ correct result: " + Binary.toString(correctAdderBranch));
            }

            memoryAccess();

            // ***** PROG. 4 STUDENT MUST ADD
            // Test that when cycleCount is 1, the value that was read from
            // memory (should be 6) is in the register write multiplexor
            // (muxRegWriteData) at input 1
            if (cycleCount == 1 && Binary.binToUDec(muxRegWriteData.output(true)) != 6) {
              System.out.println("FAIL: cycle " + cycleCount + " incorrect value in muxRegWriteData:");
              System.out.println("------ muxRegWriteData value: " + Binary.binToUDec(muxRegWriteData.output(true)));
              System.out.println("------ correct value: 6");
            }

            writeBack();

            cycleCount++;

            // Start the next cycle
            instruction = fetch();

            // ***** PROG. 4 STUDENT MUST ADD
            // Test that when cycleCount is 7, the instruction returned by fetch is
            // the last instruction in the program: STR R5,[R9,#8]
            boolean[] lastInstr = {true, false, true, false, false, true, false, false, true, false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true};
            if (cycleCount == 7 && !Arrays.equals(instruction, lastInstr)){
              System.out.println("Instruction: " + Binary.toString(instruction) + " lastInstr: " + Binary.toString(lastInstr));
              System.out.println("FAIL: cycle " + cycleCount + " did not fetch correct instruction:");
              System.out.println("------ fetch returned: " + Binary.toString(instruction));
              System.out.println("------ correct instruction: " + Binary.toString(lastInstr));
            }

            op = decode(instruction);

            // ***** PROG. 4 STUDENT MUST ADD
            // Test that when cycleCount is 1, the the control signals are correctly
            // set for a LDR instruction
            if (cycleCount == 1 && (control.Uncondbranch != false || control.RegWrite != true
                || control.MemWrite != false || control.MemtoReg != true
                || control.MemRead != true || control.Branch != false || control.ALUSrc != true
                || control.ALUControl != 2)) {
                System.out.println("FAIL: cycle " + cycleCount + " after decode, control lines incorrect");
            }
        }
        System.out.println("CPU halt after " + cycleCount + " cycles.");

    }

    /**
    * STUDENT MUST COMPLETE THIS METHOD
    *
    * Instruction Fetch Step
    * Fetch the instruction from the instruction memory starting at address pc.
    * Activate the PC adder and place the adder's output into muxPC input 0.
    *
    * @return The instruction starting at address pc
    */
    //done
    private boolean[] fetch() {
        boolean[] instruction = new boolean[32]; //create a new boolean array to hold the instruction to return
        instruction = instructionMemory.read32(pc); //place the instruction from memory into instruction array
        adderPC.setInputA(pc); //set adderPC's input A to be pc
        adderPC.activate(); //activate the adderPC
        muxPC.setInput0(adderPC.getOutput()); //set muxPC's input 0 to adderPC's output
        adderBranch.setInputA(pc); //set adderBranch input A to pc
        return instruction; //return instruction
    }

    /**
    * STUDENT MUST COMPLETE THIS METHOD
    *
    * Instruction Decode and Register Read
    *
    * Decode the instruction. Sets the control lines and sends appropriate bits
    * from the instruction to various inputs within the processor.
    *
    * Set the Read Register inputs so that the values to be read from
    * the registers will be available in the next phase.
    *
    * @param instruction The 32-bit instruction to decode
    * @return false if the opcode is HLT; true for any other opcode
    */
    private boolean decode(boolean[] instruction) {
        boolean opcodeVal = true; //thing to return
        boolean[] length11Op = new boolean[11]; //for opcodes of length 11
        boolean[] cbzOp = new boolean[8]; //cbz opcode
        boolean[] bOp = new boolean[6]; //b opcode
        //iterative values to get the correct indices in the arrays
        int x = length11Op.length - 1;
        int cbz = cbzOp.length - 1;
        int b = bOp.length - 1;
        //get the length 11 opcode
        for (int i = instruction.length - 1; i >= 21; i--) {
          length11Op[x] = instruction[i];
          x--;
        }
        //get the cbz opcode
        for (int i = instruction.length - 1; i >= 24; i--) {
          cbzOp[cbz] = instruction[i];
          cbz--;
        }
        //get the b opcode
        for (int i = instruction.length - 1; i >= 26; i--) {
          bOp[b] = instruction[i];
          b--;
        }
        //if the length 11 opcode is ADD
        if (Arrays.equals(length11Op, Opcode.ADD)) {
          setRTypeControl(); //call this method to set control lines
          control.ALUControl = 2; //set ALU control to 2
          sendBitsToProcess(instruction); //process the bits and send them to the right places in CPU
          //if length 11 opcode is ORR
        } else if (Arrays.equals(length11Op, Opcode.ORR)) {
          setRTypeControl(); //call this method to set control lines
          control.ALUControl = 1; //set ALU control to 1
          sendBitsToProcess(instruction); //process the bits and send them to the right places in CPU
          //if length 11 opcode is AND
        } else if (Arrays.equals(length11Op, Opcode.AND)) {
          setRTypeControl();//call this method to set control lines
          control.ALUControl = 0;//set ALU control to 0
          sendBitsToProcess(instruction); //process the bits and send them to the right places in CPU
          //if length 11 opcode is SUB
        } else if (Arrays.equals(length11Op, Opcode.SUB)) {
          setRTypeControl(); //call this method to set control lines
          control.ALUControl = 6; //set ALU control to 6
          sendBitsToProcess(instruction);//process the bits and send them to the right places in CPU
          //if length 11 opcode is LDR
        } else if (Arrays.equals(length11Op, Opcode.LDR)) {
          loadStoreCPU(instruction, Opcode.LDR); //call this method to do the required things for LDR instruction
          //if lenght 11 opcode is STR
        } else if (Arrays.equals(length11Op, Opcode.STR)) {
          loadStoreCPU(instruction, Opcode.STR); //call this method to do the required things for STR instruction
          //if opcode is CBZ
        } else if (Arrays.equals(cbzOp, Opcode.CBZ)) {
          setCBZControl(); //call this method to set control lines
          muxALUb.setInput1(signExtendCBZ(instruction)); //set muxALUb input 1 to sign extended immediate associated with CBZ instr
          adderBranch.setInputB(signExtendCBZ(instruction)); //set adderBranch input B to sign extended immdiate value associated with CBZ instr
          sendBitsToProcess(instruction); //process the bits and send them to right places in CPU
          //if opcode is B
        } else if (Arrays.equals(bOp, Opcode.B)) {
          setBControl(); //set control lines
          muxALUb.setInput1(signExtendB(instruction)); //set muxALUb input 1 to sign extended immediate associated with B instr
          adderBranch.setInputB(signExtendB(instruction)); //set adderBranch input B to sign extended immediate associated with B instruction
          sendBitsToProcess(instruction); //process the bits and send them to right places in CPU
          //if opcode is HLT
        } else if (Arrays.equals(length11Op, Opcode.HLT)) {
          sendBitsToProcess(instruction); //process the bits and send them to right places in CPU
          opcodeVal = false; //set opcodeVal = false
        }
        return opcodeVal; //return opcodeVal
    }

    /**
    * Method to help with load and store instructions to avoid repetition of code
    * @param b, The instruction
    * @param op, the opcode of the instruction
    */
    private void loadStoreCPU(boolean[] b, boolean[] op) {
      setLoadStoreControl(op); //call this method to set control lines
      muxALUb.setInput1(signExtendLoadStore(b)); //set muxALUb input 1 to the sign extended immediate value associated with LDR instr
      adderBranch.setInputB(signExtendLoadStore(b)); //set adderBranch input B to the sign extended immediate value associated with LDR inst
      sendBitsToProcess(b); //process the bits and send them to right places in CPU
    }

    /**
    * Method to get the number of source reg 1 from an instruction
    * @param b, The instruction
    * @return the boolean array associated with source reg 1
    */
    private boolean[] findReadReg1(boolean[] b) {
      boolean[] readReg1 = new boolean[5]; //create boolean array to hold the read reg 1 info
      int x = readReg1.length - 1;
      //place values from instr into readReg1 array
      for (int i = 9; i >= 5; i--) {
        readReg1[x] = b[i];
        x--;
      }
      return readReg1; //return readReg1
    }

    /**
    * Method to get the number of source reg 2 from an instruction
    * @param b, The instruction
    * @return the boolean array associated with source reg 2
    */
    private boolean[] findReadReg2(boolean[] b) {
      boolean[] readReg2 = new boolean[5]; //create boolean array to hold the read reg 2 info
      int y = readReg2.length - 1;
      //place values from instr into readReg2 array
      for (int i = 20; i >= 16; i--) {
        readReg2[y] = b[i];
        y--;
      }
      return readReg2; //return readReg2
    }

    /**
    * Method the get the number of destination reg from an instruction
    * @param b, the instruction
    * @return the boolean array associated with destination register
    */
    private boolean[] findWriteReg(boolean[] b) {
      boolean[] writeReg = new boolean[5]; //create boolean array to hold the write reg info
      int z = writeReg.length - 1;
      //place values from instr into writeReg array
      for (int i = 4; i >= 0; i--) {
        writeReg[z] = b[i];
        z--;
      }
      return writeReg; //return writeReg
    }

    /**
    * Method to sign extend the immediate value in load and store instructions
    * @param b, the instruction
    */
    private boolean[] signExtendLoadStore(boolean[] b) {
      boolean[] immediate = new boolean[32]; //create new array to hold the resulting sign extended immediate val
      boolean[] temp = new boolean[9]; //temporary array to hold the immediate val
      int x = temp.length - 1;
      //place immediate bits into temp
      for (int i = 20; i >= 12; i--) {
        temp[x] = b[i];
        x--;
      }
      long tempVal = Binary.binToSDec(temp); //convert to long
      immediate = Binary.sDecToBin(tempVal, 32); //sign extend to 32 bits
      return immediate; //retun sign extended array
    }

    /**
    * Method to sign extend the immediate value in CBZ instruction
    * @param b, the instruction
    */
    private boolean[] signExtendCBZ(boolean[] b) {
      boolean[] immediate = new boolean[32]; //create new array to hold resulting sign extended immediate val
      boolean[] temp = new boolean[19]; //temp array to hold immediate val
      int x = temp.length - 1;
      //place immediate bits into temp
      for (int i = 23; i >= 5; i--) {
        temp[x] = b[i];
        x--;
      }
      long tempVal = Binary.binToSDec(temp); //convert to long
      immediate = Binary.sDecToBin(tempVal, 32); //sign extend to 32 bits
      return immediate; //return sign extended array
    }

    /**
    * Method to sign extend immediate val associated with B instructions
    * @param b, the instruction
    */
    private boolean[] signExtendB(boolean[] b) {
      boolean[] immediate = new boolean[32]; //create new array to hold resutlting sign extended immediate val
      boolean[] temp = new boolean[26]; //temp array to hold immediate val
      int x = temp.length - 1;
      //palce immediate bits into temp
      for (int i = 25; i >= 0; i--) {
        temp[x] = b[i];
        x--;
      }
      long tempVal = Binary.binToSDec(temp); //convert to long
      immediate = Binary.sDecToBin(tempVal, 32); //sign extend to 32 bits
      return immediate; //return sign extended array
    }

    /**
    * Method to send the bits of an instruction to their applicable places in the CPU
    * @param b, the instruction
    */
    private void sendBitsToProcess(boolean[] b) {
      boolean[] readReg1 = findReadReg1(b); //get readReg1
      boolean[] readReg2 = findReadReg2(b); //get readReg2
      boolean[] writeReg = findWriteReg(b); //get writeReg
      muxRegRead2.setInput0(readReg2); //set muxRegRead2 input 0 to readReg2
      registers.setWriteRegNum(writeReg); //set writeRegNum to writeReg
      registers.setRead1Reg(readReg1); //set read1reg to readReg1
      registers.setRead2Reg(muxRegRead2.output(control.Reg2Loc)); //set read2reg to muxRegRead2 output
      muxALUb.setInput0(registers.getReadReg2()); //set muxALUb input 0 to readReg2
      alu.setInputA(registers.getReadReg1()); //set alu input A to ReadReg1
      alu.setInputB(muxALUb.output(control.ALUSrc)); //set alu input B to muxALUb output
      alu.setControl(control.ALUControl); //set alu control to ALUcontrol val
    }

    /**
    * Method to set the control lines for B instruction
    */
    private void setBControl() {
      control.Reg2Loc = false;
      control.ALUSrc = false;
      control.MemtoReg = false;
      control.RegWrite = false;
      control.MemRead = false;
      control.MemWrite = false;
      control.Branch = true;
      control.Uncondbranch = true;
      control.ALUControl = 7;
    }

    /**
    * Method to set control lines for CBZ instruction
    */
    private void setCBZControl() {
      control.Reg2Loc = true;
      control.ALUSrc = false;
      control.MemtoReg = false; //doesnt matter
      control.RegWrite = false;
      control.MemRead = false;
      control.MemWrite = false;
      control.Branch = true;
      control.Uncondbranch = false;
      control.ALUControl = 6;
    }

    /**
    * Method to set control lines for load or store instruction
    * @param op, the opcode of the instruction
    */
    private void setLoadStoreControl(boolean[] op) {
      if (Arrays.equals(op, Opcode.LDR)) { //if ldr instruction
        control.Reg2Loc = false; //doesnt matter
        control.ALUSrc = true;
        control.MemtoReg = true;
        control.RegWrite = true;
        control.MemRead = true;
        control.MemWrite = false;
        control.Branch = false;
        control.Uncondbranch = false;
      } else if (Arrays.equals(op, Opcode.STR)) { //if str instruction
        control.Reg2Loc = true;
        control.ALUSrc = true;
        control.MemtoReg = false; //doesnt matter
        control.RegWrite = false;
        control.MemRead = false;
        control.MemWrite = true;
        control.Branch = false;
        control.Uncondbranch = false;
      }
      control.ALUControl = 2;
    }

    /**
    * Method to set R type control lines
    */
    private void setRTypeControl() {
      control.Reg2Loc = false;
      control.ALUSrc = false;
      control.MemtoReg = false;
      control.RegWrite = true;
      control.MemRead = false;
      control.MemWrite = false;
      control.Branch = false;
      control.Uncondbranch = false;
    }

    /**
    * STUDENT MUST COMPLETE THIS METHOD
    *
    * Execute Phase
    * Activate the ALU to execute an arithmetic or logic operation, or to calculate
    * a memory address.
    *
    * The branch adder is activated during this phase, and the branch adder
    * result is placed into muxPC input 1.
    *
    * This method must make decisions based on the values of the control lines.
    * This method has no information about the opcode!
    *
    */
    private void execute() {
      adderBranch.activate(); //activate adderBranch
      alu.activate(); //activate ALU
    }

    /**
    * STUDENT MUST COMPLETE THIS METHOD
    *
    * Memory Access Phase
    * Read or write from/to data memory.
    *
    * This method must make decisions based on the values of the control lines.
    * This method has no information about the opcode!
    */
    private void memoryAccess() {
      //if the instruction involved memory read
      if (control.MemRead) {
        boolean[] val = dataMemory.read32(alu.getOutput()); //get the val from datamemory at the address of alu's output
        muxRegWriteData.setInput1(val); //set muxRegWriteData input 1 to val
      }
      //if instruction involved memory write
      if (control.MemWrite) {
        dataMemory.write32(alu.getOutput(), registers.getReadReg2()); //write readreg2 to dataMemory at address of alu's output
      }
    }

    /**
    * STUDENT MUST COMPLETE THIS METHOD
    *
    * Write Back Phase
    * Perform writes to registers: the PC and the processor registers.
    *
    * This method must make decisions based on the values of the control lines.
    * This method has no information about the opcode!
    */
    private void writeBack() {
      muxPC.setInput1(adderBranch.getOutput()); // set muxPC input 1 to adderBranch output
      muxRegWriteData.setInput0(alu.getOutput()); //set muxRegWriteData input 0 to alu output
      registers.setWriteRegData(muxRegWriteData.output(control.MemtoReg)); //set writeregdata to muxRegWriteData output
      //if RegWrite is true
      if (control.RegWrite) {
        registers.activateWrite(); //activate write
      }
      boolean muxVal = (control.Branch && alu.getZeroFlag()) || control.Uncondbranch; //get boolean val of the and of Branch control line and alu's zeroFlag or'd with Uncondbranch control line
      pc = muxPC.output(muxVal); //set pc  equal to the muxPC output contingent on muxVal boolean
    }
}

/**
* Assembler for the CS318 simple computer simulation
*/
import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;

public class Assembler {

    /**
    * Assembles the code file. When this method is finished, the dataFile and
    * codeFile contain the assembled data segment and code segment, respectively.
    *
    * @param inFile The pathname to the assembly language file to be assembled.
    * @param dataFile The pathname where the data segment file should be written.
    * @param codeFile The pathname where the code segment file should be written.
    */
    public static void assemble(String inFile, String dataFile, String codeFile)
                                    throws FileNotFoundException, IOException {

        // DO NOT MAKE ANY CHANGES TO THIS METHOD

        ArrayList<LabelOffset> labels = pass1(inFile, dataFile, codeFile);
        pass2(inFile, dataFile, codeFile, labels);
    }

    /**
    * First pass of the assembler. Writes the number of bytes in the data segment
    * and code segment to their respective output files. Returns a list of
    * code segment labels and thier relative offsets.
    *
    * @param inFile The pathname of the file containing assembly language code.
    * @param dataFile The pathname for the data segment binary file.
    * @param codeFile The pathname for the code segment binary file.
    * @return List of the code segment labels and relative offsets.
    * @exception RuntimeException if the assembly code file does not have the
    * correct format, or another error while processing the assembly code file.
    */

    private static ArrayList<LabelOffset> pass1(String inFile, String dataFile, String codeFile) throws FileNotFoundException, IOException {
      Scanner scan = new Scanner(new File(inFile)); //declare a scanner to be able to read the file
      ArrayList<LabelOffset> labels = new ArrayList<LabelOffset>(); //array list of LabelOffsets to return
      int dataBytes = 0; //integer to hold the size of the data lines
      int codeBytes = 4; //integer to hold the size of the code lines
      int offset = 0; //integer to hold the offset
      int numIn = 0; //integer for the number of instruction
      String[] splitLine; //String array to hold each element of the .words
      String line = ""; //String variable to hold each line
      //while there is a next line of the file
      while (scan.hasNextLine()) {
        line = scan.nextLine(); //set line equal to the current line
        line = line.trim(); //trim any white space
        String instruction = line.substring(0, 3); //create a substring to be able to check if a line is an instruction
        //if the current line starts with .word, we are in the data segment.
        if (line.startsWith(".word")) {
          //set the line equal to everything following the .word declaration
          line = line.substring(5, line.length());
          splitLine = line.split(","); //split the line by commas (separating into each data value)
          dataBytes += 4 * splitLine.length; //add the result of 4 bytes * number of elements in the .word delcaration and place into dataBytes.
          //otherwise,
        } else {
          //check if the line contains an instruction
          if (instruction.equals("ADD") || instruction.equals("AND") || instruction.equals("SUB") || instruction.equals("ORR") || instruction.equals("LDR")
            || instruction.equals("STR") || instruction.equals("CBZ") || instruction.equals("HLT") || instruction.charAt(0) == 'B') {
            numIn += 1; //if the line is an instruction, increment numIn by 1.
            //if line contains a colon, it is a label
          } else if (line.contains(":")) {
            LabelOffset item = new LabelOffset(); //temporary LabelOffset object
            item.label = line.substring(0, line.indexOf(":")); //set the label of the temp LabelOffset object to the text from start of line to the colon
            item.offset = 4 * numIn; //set the offset equal to the product of 4 bytes * numIn.
            labels.add(item);
          }
        }
      }
      codeBytes += 4 * numIn; //add the product of 4 * numIn to codeBytes
      PrintStream dataOut = new PrintStream(new File(dataFile)); // declare a PrintStream to write the data
      PrintStream codeOut = new PrintStream(new File(codeFile)); //declare a PrintStream to write the code
      dataOut.println(dataBytes); //write the dataBytes to dataOut
      codeOut.println(codeBytes); //write the codeBytes to codeOut
      dataOut.close(); //close dataOut to prevent memory leak
      codeOut.close(); //close codeOut to prevent memory leak
      return labels;
    }


    /**
    * Second pass of the assembler. Writes the binary data and code files.
    * @param inFile The pathname of the file containing assembly language code.
    * @param dataFile The pathname for the data segment binary file.
    * @param codeFile The pathname for the code segment binary file.
    * @param labels List of the code segment labels and relative offsets.
    * @exception RuntimeException if there is an error when processing the assembly
    * code file.
    */
    public static void pass2(String inFile, String dataFile, String codeFile,
                ArrayList<LabelOffset> labels) throws FileNotFoundException, IOException {
      Scanner scan = new Scanner(new File(inFile)); //scanner to read the files
      FileWriter dataWriter = new FileWriter(dataFile, true); //writer for data files
      FileWriter codeWriter = new FileWriter(codeFile, true); //writer for code files
      String line = ""; //line variable to hold each line
      String[] splitLine; //array to hold string values split by a comma
      boolean[] dataVal; //array to hold each data value
      String codeVal = ""; //string to hold each code value
      int pc = 0; //program counter
      //while there is a next line of the file
      while (scan.hasNextLine()) {
        line = scan.nextLine(); //get the current line
        line = line.trim(); //get rid of whitespace
        //data segment
        if (line.startsWith(".word")) { //if the line holds a data val
          line = line.substring(5, line.length()); //get the numerical data
          splitLine = line.split(","); //split by commas to get individual data values
          // for each data value on a line
          for (int i = 0; i < splitLine.length; i++) {
            splitLine[i] = splitLine[i].trim(); //trim white space of each value
            dataVal = Binary.sDecToBin(Long.parseLong(splitLine[i]), 32); //convert each to a boolean array
            String temp = booleanToString(dataVal); //convert to string of false and true values
            dataWriter.write(temp + "\n"); //write to dataWriter
          }
          //code segment
          //if line is an ADD instr
        } else if (line.startsWith("ADD")) {
          line = line.substring(4, line.length()); //get the registers/immediate vals
          line = line.trim(); //get rid of white space
          splitLine = line.split(","); //split by commas to get individual registers
          codeVal = formatLogic(splitLine, Opcode.ADD); //call formatLogic method to get the String associated with the ADD opcode and register values
          codeWriter.write(codeVal + "\n");
          pc += 4; //increment pc by four
          //if line is a SUB instr
        } else if (line.startsWith("SUB")) {
          line = line.substring(4, line.length()); //get the regs/imm vals
          line = line.trim(); //trim whitespace
          splitLine = line.split(","); //split by commas to get individual regs
          codeVal = formatLogic(splitLine, Opcode.SUB); //format to get String associated with SUB opcode and registers
          codeWriter.write(codeVal + "\n");
          pc += 4; //increment pc
          //if line is an AND instr
        } else if (line.startsWith("AND")) {
          line = line.substring(4, line.length()); //get regs and imm vals
          line = line.trim(); //trim whitespace
          splitLine = line.split(","); //split by commas to get individual regs
          codeVal = formatLogic(splitLine, Opcode.AND); // format to get String associated with AND opcode and registers
          codeWriter.write(codeVal + "\n");
          pc += 4; //increment pc
          //if line is ORR instr
        } else if (line.startsWith("ORR")) {
          line = line.substring(4, line.length()); //get regs and imm vals
          line = line.trim(); //trim whitespace
          splitLine = line.split(","); //split by commas to get individual regs
          codeVal = formatLogic(splitLine, Opcode.ORR); // format to get String associated with ORR opcode and registers
          codeWriter.write(codeVal + "\n");
          pc += 4; //incrememnt pc
          //if line is LDR instr
        } else if (line.startsWith("LDR")) {
          line = line.substring(4, line.length()); //get regs and imm vals
          line = line.trim(); // trim whitespace
          splitLine = line.split(","); //split by commas to get individual regs
          codeVal = formatStoreAndLoad(splitLine, Opcode.LDR); //call formatStoreAndLoad method to get String associated with LDR opcode and regs/ base vals
          codeWriter.write(codeVal + "\n");
          pc += 4; //increment pc
          //if line is STR instr
        } else if (line.startsWith("STR")) {
          line = line.substring(4, line.length()); //get regs and imm vals
          line = line.trim(); //trim whitespace
          splitLine = line.split(","); //split by commas to get individual regs
          codeVal = formatStoreAndLoad(splitLine, Opcode.STR); //format to get String associated with STR opcode and regs/base vals
          codeWriter.write(codeVal + "\n");
          pc += 4; //incrememnt pc
          //if line is CBZ PrintStream
        } else if (line.startsWith("CBZ")) {
          pc += 4;//incrememnt pc
          line = line.substring(4, line.length()); // get reg and label
          line = line.trim(); //trim whitespace
          splitLine = line.split(","); //split by commas to get label and reg separated
          String reg = splitLine[0].substring(1);
          String cbzLabel = splitLine[1]; // get the label
          cbzLabel = cbzLabel.trim(); //trim more whitespace
          long offset = getOffset(labels, cbzLabel); //get offset associated with the label
          long imm = offset - pc; //get the immediate value by adding current pc and label offset
          boolean[] immArray = Binary.sDecToBin(imm, 19); //convert imm val to boolean array
          codeVal = formatCBZ(reg, Opcode.CBZ, immArray); //format CBZ instruction to get string associated with CBZ opcode and imm value already calculated
          codeWriter.write(codeVal + "\n");
          //if line is B instr
        } else if (line.startsWith("B")) {
          pc += 4; //increment pc
          String bLabel = line.substring(1, line.length()); //get the label in the instr
          bLabel = bLabel.trim(); //trim whitespace
          long bOff = getOffset(labels, bLabel); //get offset associated with label
          long imm = bOff - pc; //get immediate value by adding current pc with the label offset
          boolean[] immArray = Binary.sDecToBin(imm, 26); //convert imm val to boolean array
          codeVal = formatB(Opcode.B, immArray); //format B instr to get string associated with B opcode and imm val
          codeWriter.write(codeVal + "\n");

          //if HLT instr
        } else if (line.startsWith(".end")) {
          String[] result = new String[32]; //result array to hold the true/false vals of HLT instruction
          String hlt = ""; //String result of result array
          //for positions 31 - 21 in result array:
          int x = Opcode.HLT.length - 1;
          for (int i = 31; i >= 21; i--) {
            //put the opcode of HLT into the result array in correct order
              if (Opcode.HLT[x]) {
                result[i] = "true";
              } else {
                result[i] = "false";
              }
            x--;
          }
          //the rest of the array is false
          for (int i = 20; i >= 0; i--) {
            result[i] = "false";
          }
          hlt = format(result); //set hlt string to the formatted result
          codeWriter.write(hlt + "\n");
        }
      }
      //close both writers to prevent memory leak
      codeWriter.close();
      dataWriter.close();
    }

    /**
    * Method to get the offset of a label passed as a parameter.
    * Loops through the ArrayList of LabelOffsets to find the offset associated with a given label
    * @param labels, the Array List of label offset
    * @param label, the label String
    * @return the offset associated with the label
    */
    private static long getOffset(ArrayList<LabelOffset> labels, String lab) {
      LabelOffset temp = new LabelOffset(); //create temporary LabelOffset object
      //loop through the labels array list
      temp.label = lab;
      temp.offset = 0;
      for (int i = 0; i < labels.size(); i++) {
        String tempo = labels.get(i).label;
        if (tempo.equals(lab)) {
          temp.offset = labels.get(i).offset;
        }
      }
      long offset = (long)temp.offset; //cast it to long
      return offset; //return the found offset
    }

    /**
    * Method that formats the B instruction from boolean values to a String
    * translates to machine code
    * @param op, the opcode of B instruction
    * @param immVal, the immediate value previously calculated
    * @return the string representation of the machine code associated with the B opcode and immediate value
    */
    private static String formatB(boolean[] op, boolean[] immVal) {
      String[] result = new String[32]; //String array to hold the result of the conversion
      //copy B opcode into result array
      int y = op.length - 1;
      for (int i = result.length - 1; i >= 26; i--) { //loop thru result array
          //check values of opcode array and set result index accordingly
          if (op[y]) {
            result[i] = "true";
          } else {
            result[i] = "false";
          }
        y--;
      }
      //copy immediate value into result array
      int x = 0;
      for (int i = 25; i >= 0; i--) { //loop thru result array
          //check values of immVal array and set result index accordingly
          if (immVal[x]) {
            result[i] = "true";
          } else {
            result[i] = "false";
          }
        x++;
      }
      return format(result); //return the String formatted result
    }

    /**
    * Method to format CBZ instruction from a boolean val to a String
    * translates to machine code
    * @param reg, the String representation of the register associated with the CBZ instruction
    * @param op, the CBZ Opcode
    * @param immVal, the immediate value previously calculated
    * @return the string representation of the machine code associated with the CBZ instruction
    */
    private static String formatCBZ(String reg, boolean[] op, boolean[] immVal) {
      String[] result = new String[32]; //string array to hold the result //get the register value
      boolean[] regArray = Binary.sDecToBin(Long.parseLong(reg), 5); //translate the register string into a boolean arraylist
      //copy CBZ opcode into result array
      int c = op.length - 1;
      for (int i = result.length - 1; i >= 24; i--) { //loop thru result array
          //check vals of opcode array and set result index as applicable
          if (op[c]) {
            result[i] = "true";
          } else {
            result[i] = "false";
          }
        c--;
      }
      //copy immVal into result array
      int y = 0;
      for (int i = 23; i >= 5; i--) { //for these indexes
          //check vals of immVal array and set result index as applicable
          if (immVal[y]) {
            result[i] = "true";
          } else {
            result[i] = "false";
          }
        y++;
      }
      //copy register val into result array
      int x = 0;
      for (int i = 4; i >= 0; i--) { //for these indexes
          //check vals of register array and set result index as applicable
          if (regArray[x]) {
            result[i] = "true";
          } else {
            result[i] = "false";
          }
        x++;
      }
      return format(result); //return the string formatted result
    }

    /**
    * Method to format the STR and LDR instruction
    * translates to machine code based on either STR or LDR opcode
    * @param instr, String array that holds the array of regs/imm vals split by commas
    * @param op, applicable opcode (STR or LDR)
    * @return the string representation of machine code associated with either STR or LDR opcode
    */
    private static String formatStoreAndLoad(String[] instr, boolean[] op) {
      String[] result = new String[32]; //result string array
      String value = instr[0]; //first index of instr array holds the value register
      value = value.substring(1, value.length()); //get the numerical information of the register
      boolean[] valueReg = Binary.sDecToBin(Long.parseLong(value), 5); //convert to boolean array with length 5
      String base = instr[1]; //second index of instr array holds the base register
      base = base.substring(2, base.length()); //get the numerical information of the register
      boolean[] baseReg = Binary.sDecToBin(Long.parseLong(base), 5); //conver to boolean array with length 5
      String imm = instr[2]; //third index of instr array holds the immediate value
      imm = imm.substring(1, imm.length() - 1); // get the numerical information of the immediate value
      long immVal = Long.parseLong(imm); //parse the immediate as a Long
      boolean[] immResult = Binary.sDecToBin(immVal, 9); //convert the immediate val (immVal + baseVal) to a boolean array of length 9
      //copy opcode to result array
      int a = op.length - 1;
      for (int i = result.length - 1; i >= 21; i--) { //loop thru result array
          //check vals of opcode and set result index as applicable
          if (op[a]) {
            result[i] = "true";
          } else {
            result[i] = "false";
          }
        a--;
      }
      //copy immediate val into result array
      int z = 0;
      for (int i = 20; i >= 12; i--) { //for these indexes
          //check vals of immResult and set result index as applicable
          if (immResult[z]) {
            result[i] = "true";
          } else {
            result[i] = "false";
          }
        z++;
      }
      //these indexes are the shift vals
      for (int i = 11; i >= 10; i--) {
        result[i] = "false";
      }
      //copy baseReg into result array
      int y = 0;
      for (int i = 9; i >= 5; i--) { //for these indexes
          //check values of baseREg and set result index as applicable
          if (baseReg[y]) {
            result[i] = "true";
          } else {
            result[i] = "false";
          }
        y++;
      }
      //copy valueReg into result array
      int x = 0;
      for (int i = 4; i >= 0; i--) { // for these indexes
          //check values of valueReg and set result index as applicable
          if (valueReg[x]) {
            result[i] = "true";
          } else {
            result[i] = "false";
          }
        x++;
      }
      return format(result); //return the string formatted result
    }

    /**
    * Method to format ADD, SUB, AND, ORR instructions
    * translates to machine code based on opcode
    * @param logic, String array to hold the split by commas representation of regs/ immvals
    * @param op, applicable opcode (ADD, AND, SUB, ORR)
    * @return String representation of machine code associated with ADD, SUB, ORR, AND opcode
    */
    private static String formatLogic(String[] logic, boolean[] op) {
      String[] result = new String[32]; //result array to hold String vals
      String dest = logic[0]; //first index of logic holds the destination register
      dest = dest.substring(1, dest.length()); //get the numerical info of destination register
      boolean[] destReg = Binary.sDecToBin(Long.parseLong(dest), 5); //convert to boolean array of length 5
      String s1 = logic[1]; //second index of logic holds the 1st source register
      s1 = s1.substring(1, s1.length()); //get the numerical info of 1st source register
      boolean[] s1Reg = Binary.sDecToBin(Long.parseLong(s1), 5); //convert to boolean array of length 5
      String s2 = logic[2]; //third index of logic holds the 2nd source register
      s2 = s2.substring(1, s2.length()); //get numerical info of 2nd source register
      boolean[] s2Reg = Binary.sDecToBin(Long.parseLong(s2), 5); //convert to boolean array of length 5
      //copy opcode to result array
      int j = op.length - 1;
      for (int i = result.length - 1; i >= 21; i--) { //loop thru result array
          //check vals of opcode and set result index as applicable
        if (op[j]) {
          result[i] = "true";
        } else {
          result[i] = "false";
        }
        j--;
      }
      //copy 2nd source reg into result array
      int x = 0;
      for (int i = 20; i >= 16; i--) { //for these indexes
          //check vals of s2Reg array and set result index as applicable
        if (s2Reg[x]) {
          result[i] = "true";
        } else {
          result[i] = "false";
        }
        x++;
      }
      //shift values are false
      for (int i = 15; i >= 10; i--) {
        result[i] = "false";
      }
      int y = 0;
      //copy 1st source reg into result array
      for (int i = 9; i >= 5; i--) { //for these indexes
          //check vals of s1Reg array and set result index as applicable
        if (s1Reg[y]) {
          result[i] = "true";
        } else {
          result[i] = "false";
        }
        y++;
      }
      //copy destination reg into result array
      int z = 0;
      for (int i = 4; i >= 0; i--) { //for these indexes
          //check vals of destReg and set result index as applicable
        if (destReg[z]) {
          result[i] = "true";
        } else {
          result[i] = "false";
        }
        z++;
      }
      return format(result); //return the String formatted result
    }

    /**
    * Helper method to fomat a String array into what needs to be written to each file
    * @param assemble, the String array to be converted
    * @return the formatted String to be written to a file
    */
    private static String format(String[] assemble) {
      String result = ""; //result string
      //loop thru first 8 bits and add each index of assemble to the result string
      for (int i = 0; i <= 7 ; i++) {
        result += assemble[i] + " ";
      }
      result += "\n"; //add a new line
      //loop thru second 8 bits and add each index of assemble to the result string
      for (int i = 8; i <= 15; i++) {
        result += assemble[i] + " ";
      }
      result += "\n"; //add a new line
      //loop thru third 8 bits and add each index of assemble to the result string
      for (int i = 16; i <= 23; i++) {
        result += assemble[i] + " ";
      }
      result += "\n"; //add a new line
      //loop thru the last 8 bits and add each index of assemble to the result string
      for (int i = 24; i <= 31; i++) {
        result += assemble[i] + " ";
      }
      return result; //return the formatted string
    }

    /**
    * method to convert a boolean array to a string
    * @param b, the boolean array to convert to a String
    * @return the String representation of the boolean array
    */
    private static String booleanToString(boolean[] b) {
      String result = ""; //result string
      //loop thru b array
      for (int i = b.length - 1; i >= 0; i--) {
        //check vals and set as applicable
        if (b[i]) {
          result += "true ";
        } else {
          result += "false ";
        }
        //every 8th bit, make a new line
        if (i % 8 == 0 && i > 0) {
          result += "\n";
        }
      }
      return result; //return formatted result
    }
}

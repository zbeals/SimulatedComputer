// Zoe Beals
// CS318 Programming Assignment 3
// Spring 2019
// A64 implementation of Binary Search Tree
	.align 2
	.data
	// Assume that the BST's are full and complete
	// (every node other than the leaves has exactly two children, leaves are all
	// at the same depth).
	// Structure of the tree data:
	// First value is the number of nodes in the tree.
	// This is followed by the values stored in each node. The BST is stored as
	// an array where the childern of the node at index i are located at indexes
	// (2i+1) and (2i+2).
treeA: // height is 3
	.dword 15 // number of nodes in treeA
	.dword 57,39,72,23,50,62,87,20,27,49,53,60,63,81,95 // BST represented as an array
treeB: // height is 5
	.dword 63 // number of nodes in treeB
	// BST represented as an array
	.dword 2941,1836,3400,1418,2176,3298,4199,1128,1472,2143
    .dword 2552,3060,3310,3598,4280,1020,1150,1438,1713,2037
    .dword 2154,2219,2634,2987,3104,3305,3362,3487,3674,4242
    .dword 4733,1009,1057,1146,1223,1426,1453,1663,1755,1962
    .dword 2079,2145,2175,2189,2379,2602,2654,2974,3012,3095
    .dword 3162,3300,3307,3325,3373,3458,3511,3632,3912,4222
    .dword 4278,4673,4947
treeC: // empty tree, height procedure should return -1
	.dword 0 // number of nodes in treeC
treeD: // tree has one node, height procedure should return 0
	.dword 1 // number of nodes in treeD
	.dword 12345 // single node in the tree
	.text
	.global main
main:

	////////////////////
	// Test 1: treeA
	// Call the height procedure
	ADR X1,treeA // Put the base memory adress of the tree into X1
	ADD X1,X1,#8 // before calling the procedure, put address of first array element into X1
	BL p_height

	// Call the search procedure
	ADR X1,treeA // base memory address of the tree
	ADD X1,X1,#8 // before calling the procedure, put address of first array element into X1
	MOV X2,#87 // key value to search for
	MOV X3,#0 // offset of root node
	MOV X4,#0 // array index of root node
	BL p_search

	////////////////////
	// Test 2: treeB
	// Call the height procedure
	ADR X1,treeB // Put the base memory adress of the tree into X1
	ADD X1,X1,#8 // before calling the procedure, put address of first array element into X1
	BL p_height

	// Call the search procedure
	ADR X1,treeB // base memory address of the tree
	ADD X1,X1,#8 // before calling the procedure, put address of first array element into X1
	MOV X2,#2189 // key value to search for
	MOV X3,#0 // offset of root node
	MOV X4,#0 // array index of root node
	BL p_search

	////////////////////
	// Test 3: treeC
	// Call the height procedure
	ADR X1,treeC // Put the base memory adress of the tree into X1
	ADD X1,X1,#8 // before calling the procedure, put address of first array element into X1
	BL p_height

	// Call the search procedure
	ADR X1,treeC // base memory address of the tree
	ADD X1,X1,#8 // before calling the procedure, put address of first array element into X1
	MOV X2,#987 // key value to search for
	MOV X3,#0 // offset of root node
	MOV X4,#0 // array index of root node
	BL p_search

	////////////////////
	// Test 4: treeD
	// Call the height procedure
	ADR X1,treeD // Put the base memory adress of the tree into X1
	ADD X1,X1,#8 // before calling the procedure, put address of first array element into X1
	BL p_height

	// Call the search procedure
	ADR X1,treeD // base memory address of the tree
	ADD X1,X1,#8 // before calling the procedure, put address of first array element into X1
	MOV X2,#12345 // key value to search for
	MOV X3,#0 // offset of root node
	MOV X4,#0 // array index of root node
	BL p_search

	// End of main procedure, branch to end of program
	B program_end

p_height:
	// Height Procedure (iterative implementation)
	// X0: Returns the height of the tree (number of edges from root to deepest leaf).
	// If the tree is empty, returns -1; if the tree contains 1 node, returns 0.
	// X1: The memory base address of the binary search tree. Assumes the value before this
	// memory address is the number of nodes in the BST, followed by the values in each node
	// of the BST. Assumes the BST is full and complete (procedure will not alter)
	//
	// This procedure must use an iterative (non-recursive) algorithm. The performance
	// of the solution must be O(log n), where n is the number of nodes in the tree.
	//
	// Temporary registers used by this procedure:
	// <student must list the registers; start with X9, and use registers in number order
	// as needed up to X15>
	// Values of the temporary registers used by this procedure must be preserved.
	// When procedure returns, only X0 should have a different value than it did at the start.
    //X9 holds the current index of the array
    //X10 holds the length of the array
    //X11 is a temporary register
    //X12 is a temporary register to hold the current height
    //X13 is a temporary register

	//******* Write your code for the height procedure here ******/
    SUB SP,SP,#48 //move stack pointer down by 48 bytes
    STR X9,[SP,#40] //copy val from X9 to stack
    STR X10,[SP,#32] //copy val from X10 to stack
    STR X11,[SP,#24] //copy val from X11 to stack
    STR X12,[SP,#16] //copy val from X12 to stack
    STR X13,[SP,#8] //copy val from X13 to stack

    MOV X9,#0 //initialize X9 to 0 (first index in array)
    LDR X10,[X1,#-8] //load the length of the array into X10.
    CBZ X10, empty_tree
    MOV X12,#1 //initialize X12 to 1 (since we took care of empty tree case)

loop_beg:
    //compute 2*X9 + 1 to find left child index
    ADD X13,X9,X9 //2* X9
    ADD X9,X13,#1 //+ 1
    
    SUB X13,X10,X9 //check if we are past the length of array
    CBZ X13, loop_end //if we are: end loop
    
    ADD X12,X12,#1 //otherwise increment height by 1
    B loop_beg //branch back to the beginning of the loop

loop_end:
    ADD X0,X12,XZR //place height from X12 into X0
    B end_height //branch to the end of height procedure

empty_tree:
    MOV X0,#0 //height is zero if tree is empty
    B end_height //branch to the end of height procedure

end_height:
    LDR X13,[SP,#8] //copy value from stack to X13
    LDR X12,[SP,#16] //copy value from stack to X12
    LDR X11,[SP,#24] //copy value from stack to X11
    LDR X10,[SP,#32] //copy value from stack to X10
    LDR X9,[SP,#40] //copy value from stack to X9
    ADD SP,SP,#48 //move stack pointer up by 32
   
    BR X30 //return statement
    
p_search:
	// Search Procedure (recursive implementation)
	// X0: Returns the array index where the key is found, or -1 if the key is not found.
	// X1: The memory base address of the binary search tree. Assumes the value before this
	// memory address is the number of nodes in the BST, followed by the values in each node
	// of the BST. Assumes the BST is full and complete (procedure will not alter)
	// X2: The key value to search for (procedure will not alter)
	// X3: The memory offset of the current sub-tree root (procedure may alter)
	// X4: The index of the current sub-tree root (procedure may alter)
	//
	// This procedure must use a recursive algorithm that has worst case
	// performance O(tree height).
	//
	// Temporary registers used by this procedure:
	// <student must list the registers; start with X9, and use registers in number order
	// as needed up to X15>
	// Values of the temporary registers used by this procedure must be preserved.
	// When procedure returns, only X0, X3, and X4 may have a different value than they did at the start.

	//******* Write your code for the search procedure here ******/
    //X10 holds the length of the array
    //X9 holds the current value in the array
    //X11 is a temporary register
    //X12 is a temporary register
    //allocate space on stack
    SUB SP,SP,#32
    STR X9,[SP,#24]
    STR X10,[SP,#16]
    STR X11,[SP,#8]
    STR X12,[SP,#0]

    LDR X10,[X1,#-8] //load length of array into X10
    CBZ X10, empty_tree //branch to empty tree if length is zero

    SUB X12,X10,X4 //check if we are at the end of array by subtracking current index from length
    CBZ X12, end_search //end search if we are at end of array

    LDR X9,[X1,X3] //load the curr val of the array into X9
    
    SUB X11,X9,X2 //check if current val of array is the key
    CBZ X11, key_is_current_node //branch here if true

    SUBS X11,X2,X9 //subtract current val and key
    B.GT go_right //if key is less
    B.LT go_left //if key is more

go_right:
    //compute 2i+2 in offset terms
    ADD X12,X3,X3
    ADD X3,X12,#16
    //compute 2i+2 in index
    ADD X12,X4,X4
    ADD X4,X12,#2
   
    BL p_search

go_left:
    //compute 2i+1 in offset terms
    ADD X12,X3,X3
    ADD X3,X12,#8
    //computer 2i+1 in index terms
    ADD X12,X4,X4
    ADD X4,X12,#1

    BL p_search
    
key_is_current_node:
    ADD X0,X4,XZR //place X4 into X0 to return
    B end_search
    
empty_tree:
    MOV X4,#-1 //key not found if empty
    B end_search

end_search:
    ADD X0,X4,XZR //place current index into X0
    //put space back on stack
    LDR X9,[SP,#0]
    LDR X10,[SP,#8]
    LDR X11,[SP,#16]
    LDR X12,[SP,#24]
    ADD SP,SP,#32

    BR X30 //branch and return
	// End of search procedure

program_end:
	MOV X7,#0 // placeholder at end of program
	.end

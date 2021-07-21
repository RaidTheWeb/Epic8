```as

MOV 'H', D ; Store ascii char in D register (display) (Chars are automatically replaced with their Hex version, for obvious reasons)

CMD 0x10 ; Call INTERRUPT 0x10 (Common write of D register to display at index I)

; Rest of Hello Sequence
MOV 'E', D
CMD 0x10
MOV 'L', D
CMD 0x10
MOV 'L', D
CMD 0x10
MOV 'O', D
CMD 0x10


DB "0123456789!" ; Declare String

.printChar: ; Peclare label

	POPL D ; Pop first item in stack into display register
	CMD 0x10 ; print to screen

	PEEK D ; Peek to see if there is any data left in the stack and store it in D
	JNULL D ; Check if D is equal to 0x00, if so, skip next instruction
	JMP printChar ; Jump back to label (will be skipped if there is no data left on the stack)



HALT ; Stop CPU (otherwise system will keep running at idle, idk why, just felt like it lol)
```
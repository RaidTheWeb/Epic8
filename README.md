# Epic 8

Custom CPU architecture emulator with a custom Assembler

## Building assembly files

To build an assembly file run the jar file with the arguments `java <jarfilename>.jar -asm <input> <outputbinfile>`.

The assembler will compile the input Epic8 Assembly file into a flat binary that the CPU can read. Assembling a file will automatically run it afterwards.

## Running a ROM

To run a rom file, just run the jar `java <jarfilename>.jar <inputbinfile>` with the input binary ROM.

When running another ROM file you can press **FILE > OPEN** to open a new ROM file via the GUI.

By default the emulator will attempt to run a `./rom.bin` file if no arguments are supplied, if this file does not exist the emulator will throw an error, as such it is recommended to run the emulator with arguments supplied.

## Loading/Saving a state

Loading or saving a state can be done by using the Load/Save State buttons under the `Emulator` menu on the GUI menu bar. States saved are full serialized dumps of memory, registers, the display buffer, stack, stack pointer, program pointer etc. in one MemoryObject file.

## Pause/Resume

Pausing and resuming the execution of the emulator can be achieved through the **EMULATOR** menu via the GUI, this can be helpful for debugging as no registers or anything in memory is modified.

## Debugging

A few debugging tools can be found under **EMULATOR > DEBUG**, these include Dumping Memory and Dumping the Registers (also includes the Porgram Pointer PC) these two debug tools dump the memory or registers while giving a live update of the current values of memory or registers. More debugging tools may be included in the future.

## Execution Speed

Execution speed can be changed under **EMULATOR > EXECUTION SPEED**, these options allow the user to slow down the curent execution if it is going too fast.

## Instruction Set

```
JMP     - hex/$/label       = Jump to point in memory indicated by first argument
CMD     - hex               = Call an interrupt indicated by the hex value'
MOV     - hex/char, reg     = Move hex/char into reg
PUSH    - hex/char/reg      = Push hex/char/reg value onto next free space on stack
POP     - reg               = Pop latest value on stack into reg
POPL    - reg               = Pop first value on stack into reg
PEEK    - reg               = Get latest value on stack and put into reg without popping
JNULL   - reg               = Check if reg's value is equal to 0x00 (NULL)
WAIT    - hex               = Wait hex clock cycles (Dependant on current execution speed)
ADD     - reg, hex/reg2     = Set reg to reg + hex/reg2
SUB     - reg, hex/reg2     = Set reg to reg - hex/reg2
DIV     - reg, hex/reg2     = Set reg to reg / hex/reg2
MUL     - reg, hex/reg2     = Set reg to reg * hex/reg2
INB     - hex               = Read 4 bytes on port hex into register K
OUTB    - hex, hex2         = Write 4 bytes (hex2) to port hex
```

## Registers

Go to validregisters.md

## CPU Info

### Program Offset

Programs are loaded at 0x400 and can take all the way up to 0xFFAA in the 64KB of memory.

### Opcodes
Opcodes take 4 bytes each in memory.

The first byte of an opcode is the identifier that the CPU uses to tell what operation the next 3 bytes will be apart of.
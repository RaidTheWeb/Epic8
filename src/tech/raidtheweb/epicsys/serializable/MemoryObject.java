package tech.raidtheweb.epicsys.serializable;

import java.io.Serializable;

public class MemoryObject implements Serializable {

	private static final long serialVersionUID = 2808390709548554209L;

	private char[] memory;
	private char D;
	private short I;
	private short X;
	private short Y;
	private short Z;
	private short P;
	private short F;
	private short E;
	private short C;
	private short A;
	private short K; 
	private short R;
	private short S;
	private short pc;
	private int sp;
	private short[] stack;
	private boolean redrawRequired;
	private char[] displayBuffer;
	private int displayPointer;
	
	public MemoryObject(char[] memory, char D, short I, short X, short Y, short Z, short P, 
			short F, short E, short C, short A, short K, short R, short S, short pc, int sp, short[] stack, boolean redrawRequired, char[] displayBuffer, int displayPointer) {
		this.memory = memory;
		this.D = D;
		this.I = I;
		this.X = X;
		this.Y = Y;
		this.Z = Z;
		this.P = P;
		this.F = F;
		this.E = E;
		this.C = C;
		this.A = A;
		this.K = K;
		this.R = R;
		this.S = S;
		this.pc = pc;
		this.sp = sp;
		this.stack = stack;
		this.redrawRequired = redrawRequired;
		this.displayBuffer = displayBuffer;
		this.displayPointer = displayPointer;
	}
	
	public char[] getMemory() {
		return this.memory;
	}

	public char getD() {
		return D;
	}

	public short getI() {
		return I;
	}

	public short getX() {
		return X;
	}

	public short getY() {
		return Y;
	}

	public short getZ() {
		return Z;
	}

	public short getF() {
		return F;
	}

	public short getP() {
		return P;
	}

	public short getE() {
		return E;
	}

	public short getC() {
		return C;
	}

	public short getA() {
		return A;
	}

	public short getK() {
		return K;
	}

	public short getR() {
		return R;
	}

	public short getS() {
		return S;
	}

	public short getPc() {
		return pc;
	}

	public int getSp() {
		return sp;
	}

	public boolean getRedrawRequired() {
		return redrawRequired;
	}

	public short[] getStack() {
		return stack;
	}

	public char[] getDisplayBuffer() {
		return displayBuffer;
	}

	public int getDisplayPointer() {
		return displayPointer;
	}
}

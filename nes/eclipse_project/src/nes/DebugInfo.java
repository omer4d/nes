package nes;

import static nes.Util.printf;

public class DebugInfo {
	public int pc, a, x, y, flags, sp;
	public int instr, byte1, byte2;
	public int cyc;
	public String asm;
	
	private static String maybeByte(int b)
	{
		return b >= 0 ? String.format("%02X", b) : "--";
	}
	
	private static String formatFlags(int flags)
	{
		return "" + ((flags & 128) == 0 ? "-" : "N")
				  + ((flags & 64)  == 0 ? "-" : "V")
				  + ((flags & 32)  == 0 ? "-" : "?")
				  + ((flags & 16)  == 0 ? "-" : "B")
				  + ((flags & 8)   == 0 ? "-" : "D")
				  + ((flags & 4)   == 0 ? "-" : "I")
				  + ((flags & 2)   == 0 ? "-" : "Z")
				  + ((flags & 1)   == 0 ? "-" : "C");
	}
	
	public String toString()
	{
		return String.format("%X  %02X %s %s   A=%02X, X=%02X, Y=%02X, [%s], SP=%02X, CYC=%d",
					pc, instr,
					maybeByte(byte1), maybeByte(byte2),
					a, x, y, formatFlags(flags), sp, cyc);
	}
	
	public boolean equals(Object otherObj)
	{
		if(!(otherObj instanceof DebugInfo))
			return false;
		
		DebugInfo other = (DebugInfo)otherObj;
		
		return this.a == other.a &&
				this.x == other.x &&
				this.y == other.y &&
				this.flags == other.flags &&
				this.sp == other.sp &&
				this.cyc == other.cyc &&
				this.instr == other.instr &&
				this.pc == other.pc &&
				(this.byte1 == -1 || other.byte1 == -1 || this.byte1 == other.byte1) &&
				(this.byte2 == -1 || other.byte2 == -1 || this.byte2 == other.byte2);
	}
};
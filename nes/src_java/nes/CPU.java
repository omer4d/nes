package nes;

public abstract class CPU {
	public interface ByteLoc {
		public int read();
		public void write(int n);
	}
	
	static final int CARRY_MASK = 1;
	static final int ZERO_MASK = 2;
	static final int IRQ_DISABLED_MASK = 4;
	static final int BRK_MASK = 16;
	static final int OVERFLOW_MASK = 64;
	static final int NEG_MASK = 128;
	
	static final int STACK_START = 0x1FF;
	static final int STACK_END = STACK_START - 0xFF;
	
	int acc, x, y, sp = STACK_START, flags;
	int pc, cycles;
	byte ram[] = new byte[1024 * 100];
	
	ByteLoc locAcc = new ByteLoc() {
		public int read() { return acc; }
		public void write(int n) { acc = n; }
	};
	
	ByteLoc locMem = new ByteLoc() {
		public int addr = -1;
		
		public int read()
		{
			return readMemByte(addr);
		}
		
		public void write(int n)
		{
			verifyByte(n);
			ram[addr] = (byte)n;
		}
	};
	
	static void verifyByte(int x)
	{
		if(x < 0 || x > 0xFF)
			throw new IllegalArgumentException("Byte out of range: " + x);
	}
	
	static void verifyShort(int x)
	{
		if(x < 0 || x > 0xFFFF)
			throw new IllegalArgumentException("Short out of range: " + x);
	}
	
	int carry()
	{
		return flags & CARRY_MASK;//(p & CARRY_MASK) != 0 ? 1 : 0;
	}
	
	void flagOn(int mask)
	{
		flags |= mask;
	}
	
	void flagOff(int mask)
	{
		flags &= ~mask;
	}
	
	void flagSet(int mask, boolean on)
	{
		flags = on ? (flags | mask) : (flags & ~mask);
	}
	
	void push(int v)
	{
		verifyByte(v);
		ram[sp] = (byte)v;
		--sp;
	}
	
	int pop()
	{
		++sp;
		return ram[sp] & 0xFF;
	}
	
	boolean flagGet(int mask)
	{
		return (flags & mask) != 0;
	}
	
	static boolean isNeg(int n)
	{
		return (n & 128) != 0;
	}
	
	static int lsb(int n)
	{
		return n & 0xFF;
	}
	
	static int msb(int n)
	{
		return (n >> 8) & 0xFF;
	}
	
	static int makeShort(int low, int high)
	{
		return low | (high << 8);
	}
	
	void branchHelper(int offs, boolean cond)
	{
		if(cond)
		{
			cycles += (pc & 0xFF00) == ((pc + offs) & 0xFF00) ? 1 : 2;
			pc += offs;
		}
	}
	
	int readMemByte(int addr)
	{
		verifyShort(addr);
		return ram[addr] & 0xFF;
	}
	
	int readMemShort(int addr)
	{
		verifyShort(addr);
		return makeShort(ram[addr] & 0xFF, ram[addr + 1] & 0xFF);
	}
	
	// ****************
	// * Instructions *
	// ****************
	
	// All instructions assume an argument in the correct unsigned range!
	
	void adc(int src)
	{
		int sum = acc + src + carry();
		
		flagSet(CARRY_MASK, sum > 255);
		flagSet(OVERFLOW_MASK, ((sum ^ acc) & (sum ^ src) & 128) != 0);
		flagSet(NEG_MASK, isNeg(sum));
		flagSet(ZERO_MASK, sum == 0);
		
		acc = sum & 0xFF;
	}
	
	void and(int src)
	{
		acc &= src;
		flagSet(ZERO_MASK, acc == 0);
		flagSet(NEG_MASK, isNeg(acc));
	}
	
	void asl(ByteLoc src)
	{
		int v = src.read() << 1;
		flagSet(CARRY_MASK, (v & 256) != 0);
		flagSet(ZERO_MASK, v == 0);
		flagSet(NEG_MASK, isNeg(v));
		src.write(v & 0xFF);
	}
	
	void bcc(int offs)
	{
		branchHelper(offs, !flagGet(CARRY_MASK));
	}
	
	void bcs(int offs)
	{
		branchHelper(offs, flagGet(CARRY_MASK));
	}
	
	void beq(int offs)
	{
		branchHelper(offs, flagGet(ZERO_MASK));
	}
	
	void bit(int src)
	{
		int tmp = src & acc;
		flagSet(OVERFLOW_MASK, (src & 64) == 0);
		flagSet(NEG_MASK, (src & 128) == 0);
		flagSet(ZERO_MASK, tmp == 0);
	}
	
	void bmi(int offs)
	{
		branchHelper(offs, flagGet(NEG_MASK));
	}
	
	void bne(int offs)
	{
		branchHelper(offs, !flagGet(ZERO_MASK));
	}
	
	void bpl(int offs)
	{
		branchHelper(offs, !flagGet(NEG_MASK));
	}
	
	void brk()
	{
		push(msb(pc));
		push(lsb(pc));
		flagOn(BRK_MASK);
		push(flags);
		flagOn(IRQ_DISABLED_MASK);
		pc = readMemShort(0xFFFE);
	}
	
	void bvc(int offs)
	{
		branchHelper(offs, !flagGet(OVERFLOW_MASK));
	}
	
	void bvs(int offs)
	{
		branchHelper(offs, flagGet(OVERFLOW_MASK));
	}
	
	void clc()
	{
		flagOff(CARRY_MASK);
	}
	
	void cld()
	{
	}
	
	void cli()
	{
		flagOff(IRQ_DISABLED_MASK);
	}
	
	void clv()
	{
		flagOff(OVERFLOW_MASK);
	}
	
	void cmp(int src)
	{
		int diff = acc - src;
		flagSet(ZERO_MASK, diff == 0);
		flagSet(NEG_MASK, isNeg(diff));
		flagSet(CARRY_MASK, diff >= 0);
	}
	
	void cpx(int src)
	{
		int diff = x - src;
		flagSet(ZERO_MASK, diff == 0);
		flagSet(NEG_MASK, isNeg(diff));
		flagSet(CARRY_MASK, diff >= 0);
	}
	
	void cpy(int src)
	{
		int diff = y - src;
		flagSet(ZERO_MASK, diff == 0);
		flagSet(NEG_MASK, isNeg(diff));
		flagSet(CARRY_MASK, diff >= 0);
	}
	
	void dec(ByteLoc loc)
	{
		int tmp = loc.read() - 1;
		flagSet(ZERO_MASK, tmp == 0);
		flagSet(NEG_MASK, isNeg(tmp));
		loc.write(tmp & 0xFF);
	}
	
	void dex()
	{
		x = (x - 1) & 0xFF;
		flagSet(ZERO_MASK, x == 0);
		flagSet(NEG_MASK, isNeg(x));
	}
	
	void dey()
	{
		y = (y - 1) & 0xFF;
		flagSet(ZERO_MASK, y == 0);
		flagSet(NEG_MASK, isNeg(y));
	}
	
	void eor(int src)
	{
		acc &= src;
		flagSet(ZERO_MASK, acc == 0);
		flagSet(NEG_MASK, isNeg(acc));
	}
	
	void inc(ByteLoc loc)
	{
		int tmp = loc.read() + 1;
		flagSet(ZERO_MASK, tmp == 0);
		flagSet(NEG_MASK, isNeg(tmp));
		loc.write(tmp & 0xFF);
	}
	
	void inx()
	{
		x = (x + 1) & 0xFF;
		flagSet(ZERO_MASK, x == 0);
		flagSet(NEG_MASK, isNeg(x));
	}
	
	void iny()
	{
		y = (y + 1) & 0xFF;
		flagSet(ZERO_MASK, y == 0);
		flagSet(NEG_MASK, isNeg(y));
	}
	
	void jmp(int addr)
	{
		pc = addr;
	}
	
	void jsr(int addr)
	{
		int ret = pc - 1;
		push(msb(ret));
		push(lsb(ret));
		pc = addr;
	}
	
	void lda(int src)
	{
		acc = src;
		flagSet(ZERO_MASK, acc == 0);
		flagSet(NEG_MASK, isNeg(acc));
	}
	
	void ldx(int src)
	{
		x = src;
		flagSet(ZERO_MASK, x == 0);
		flagSet(NEG_MASK, isNeg(x));
	}
	
	void ldy(int src)
	{
		y = src;
		flagSet(ZERO_MASK, y == 0);
		flagSet(NEG_MASK, isNeg(y));
	}
	
	void lsr(ByteLoc loc)
	{
		int src = loc.read();
		flagSet(CARRY_MASK, (src & 1) != 0);
		flagOff(NEG_MASK);
		flagSet(ZERO_MASK, (src >> 1) == 0);
		loc.write(src >> 1);
	}
	
	void nop()
	{
	}
	
	void ora(int src)
	{
		acc |= src;
		flagSet(ZERO_MASK, acc == 0);
		flagSet(NEG_MASK, isNeg(acc));
	}
	
	void pha()
	{
		push(acc);
	}
	
	void php()
	{
		push(flags);
	}
	
	void pla()
	{
		acc = pop();
		flagSet(ZERO_MASK, acc == 0);
		flagSet(NEG_MASK, isNeg(acc));
	}
	
	void plp()
	{
		flags = pop();
	}
	
	void ror(ByteLoc loc)
	{
		int tmp = loc.read();
		int c = carry();
		flagSet(CARRY_MASK, (tmp & 1) != 0);
		loc.write((tmp >> 1) | (c << 7));
		flagSet(NEG_MASK, c != 0);
	}
	
	void rti()
	{
		flags = pop();
		pc = makeShort(pop(), pop());
	}
	
	void rts()
	{
		pc = makeShort(pop(), pop()) + 1;
	}
	
	void sbc(int src)
	{
		int diff = acc - src - (1 - carry());
		
		flagSet(CARRY_MASK, diff >= 0);
		flagSet(OVERFLOW_MASK, ((src ^ acc) & (src ^ diff) & 128) != 0);
		flagSet(NEG_MASK, isNeg(diff));
		flagSet(ZERO_MASK, diff == 0);
		
		acc = diff & 0xFF;
	}
	
	void sec()
	{
		flagOn(CARRY_MASK);
	}
	
	void sed()
	{
		
	}
	
	void sei()
	{
		flagOn(IRQ_DISABLED_MASK);
	}
	
	void sta(ByteLoc loc)
	{
		loc.write(acc);
	}
	
	void stx(ByteLoc loc)
	{
		loc.write(x);
	}
	
	void sty(ByteLoc loc)
	{
		loc.write(y);
	}
	
	void tax()
	{
		x = acc;
		flagSet(ZERO_MASK, x == 0);
		flagSet(NEG_MASK, isNeg(x));
	}
	
	void tay()
	{
		y = acc;
		flagSet(ZERO_MASK, y == 0);
		flagSet(NEG_MASK, isNeg(y));
	}
	
	void tsx()
	{
		x = sp - STACK_END;
		flagSet(ZERO_MASK, x == 0);
		flagSet(NEG_MASK, isNeg(x));
	}
	
	void txa()
	{
		acc = x;
		flagSet(ZERO_MASK, acc == 0);
		flagSet(NEG_MASK, isNeg(acc));
	}
	
	void txs()
	{
		sp = STACK_END + x;
	}
	
	void tya()
	{
		acc = y;
		flagSet(ZERO_MASK, acc == 0);
		flagSet(NEG_MASK, isNeg(acc));
	}
	
	public abstract void run();
}
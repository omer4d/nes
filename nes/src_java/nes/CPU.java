package nes;

public final class CPU {	
	static final int CARRY_MASK = 1;
	static final int ZERO_MASK = 2;
	static final int IRQ_DISABLED_MASK = 4;
	static final int BRK_MASK = 16;
	static final int OVERFLOW_MASK = 64;
	static final int NEG_MASK = 128;
	
	static final int STACK_LOWEST = 0x100;
	static final int STACK_HIGHEST = STACK_LOWEST + 0xFF;
    static final int RESET_ADDR = 0xFFFC;
	
	int acc, x, y, sp = 0xFF, flags;
	int pc, cycles;
	byte ram[] = new byte[0x10000];
	
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
		return flags & CARRY_MASK;
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
		ram[STACK_LOWEST + sp] = (byte)v;
		sp = (sp - 1) & 0xFF;
	}
	
	int pop()
	{
		sp = (sp + 1) & 0xFF;
		return ram[STACK_LOWEST + sp] & 0xFF;
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
	
	void writeMemByte(int addr, int b)
	{
		verifyShort(addr);
		verifyByte(b);
		ram[addr] = (byte)b;
	}
	
	int readMemShort(int addr)
	{
		verifyShort(addr);
		return makeShort(ram[addr] & 0xFF, ram[addr + 1] & 0xFF);
	}
	
	// ************************
	// * Address Calculations *
	// ************************
	
	int absAddr(int low, int high)
	{
		return makeShort(low, high);
	}
	
	int indAddr(int low, int high)
	{
		return readMemShort(makeShort(low, high));
	}
	
	int absxAddr(int low, int high)
	{
		return makeShort(low, high) + x;
	}
	
	int absyAddr(int low, int high)
	{
		return makeShort(low, high) + y;
	}
	
	int zpxAddr(int addr)
	{
		return addr + x;
	}
	
	int zpyAddr(int addr)
	{
		return addr + y;
	}
	
	int indxAddr(int lo, int hi)
	{
		return readMemShort(makeShort(lo, hi) + x);
	}
	
	int indyAddr(int lo, int hi)
	{
		return readMemShort(makeShort(lo, hi)) + y;
	}
	
	int relAddr(int offs)
	{
		return pc + (byte)offs;
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
	
	void asl(int loc)
	{
		int v = readMemByte(loc) << 1;
		flagSet(CARRY_MASK, (v & 256) != 0);
		flagSet(ZERO_MASK, v == 0);
		flagSet(NEG_MASK, isNeg(v));
		writeMemByte(loc, v & 0xFF);
	}
	
	void asla()
	{
		int v = acc << 1;
		flagSet(CARRY_MASK, (v & 256) != 0);
		flagSet(ZERO_MASK, v == 0);
		flagSet(NEG_MASK, isNeg(v));
		acc = v & 0xFF;
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
	
	void dec(int loc)
	{
		int tmp = readMemByte(loc) - 1;
		flagSet(ZERO_MASK, tmp == 0);
		flagSet(NEG_MASK, isNeg(tmp));
		writeMemByte(loc, tmp & 0xFF);
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
	
	void inc(int loc)
	{
		int tmp = readMemByte(loc) + 1;
		flagSet(ZERO_MASK, tmp == 0);
		flagSet(NEG_MASK, isNeg(tmp));
		writeMemByte(loc, tmp & 0xFF);
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
	
	void lsr(int loc)
	{
		int src = readMemByte(loc);
		flagSet(CARRY_MASK, (src & 1) != 0);
		flagOff(NEG_MASK);
		flagSet(ZERO_MASK, (src >> 1) == 0);
		writeMemByte(loc, src >>> 1);
	}
	
	void lsra()
	{
		flagSet(CARRY_MASK, (acc & 1) != 0);
		acc >>>= 1;
		flagSet(ZERO_MASK, acc == 0);
		flagOff(NEG_MASK);
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
	
	void ror(int loc)
	{
		int tmp = readMemByte(loc);
		int c = carry();
		flagSet(CARRY_MASK, (tmp & 1) != 0);
		writeMemByte(loc, (tmp >> 1) | (c << 7));
		flagSet(NEG_MASK, c != 0);
	}
	
	void rora()
	{
		int c = carry();
		flagSet(CARRY_MASK, (acc & 1) != 0);
		acc = (acc >> 1) | (c << 7);
		flagSet(NEG_MASK, c != 0);
		flagSet(ZERO_MASK, acc == 0);
	}
	
	void rol(int loc)
	{
		int tmp = readMemByte(loc);
		int c = carry();
		flagSet(CARRY_MASK, (tmp & 128) != 0);
		tmp = ((tmp << 1) & 0xFF) | c;
		writeMemByte(loc, tmp);
		flagSet(NEG_MASK, isNeg(tmp));
	}
	
	void rola()
	{
		int c = carry();
		flagSet(CARRY_MASK, (acc & 128) != 0);
		acc = ((acc << 1) & 0xFF) | c;
		flagSet(NEG_MASK, isNeg(acc));
		flagSet(ZERO_MASK, acc == 0);
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
	
	void sta(int loc)
	{
		writeMemByte(loc, acc);
	}
	
	void stx(int loc)
	{
		writeMemByte(loc, x);
	}
	
	void sty(int loc)
	{
		writeMemByte(loc, y);
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
		x = sp;
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
		sp = x;
	}
	
	void tya()
	{
		acc = y;
		flagSet(ZERO_MASK, acc == 0);
		flagSet(NEG_MASK, isNeg(acc));
	}
	
	public void run(int cycleNum)
	{
		while(cycles < cycleNum)
		{
			switch(readMemByte(pc++))
			{
			case 105:
				adc(readMemByte(pc++));
				cycles += 2;
				break;
			case 101:
				adc(readMemByte(readMemByte(pc++)));
				cycles += 3;
				break;
			case 117:
				adc(readMemByte(zpxAddr(readMemByte(pc++))));
				cycles += 4;
				break;
			case 109:
				adc(readMemByte(absAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 125:
				adc(readMemByte(absxAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 121:
				adc(readMemByte(absyAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 97:
				adc(readMemByte(indxAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 6;
				break;
			case 113:
				adc(readMemByte(indyAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 5;
				break;
			case 41:
				and(readMemByte(pc++));
				cycles += 2;
				break;
			case 37:
				and(readMemByte(readMemByte(pc++)));
				cycles += 3;
				break;
			case 53:
				and(readMemByte(zpxAddr(readMemByte(pc++))));
				cycles += 4;
				break;
			case 45:
				and(readMemByte(absAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 61:
				and(readMemByte(absxAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 57:
				and(readMemByte(absyAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 33:
				and(readMemByte(indxAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 6;
				break;
			case 49:
				and(readMemByte(indyAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 5;
				break;
			case 10:
				asla();
				cycles += 2;
				break;
			case 6:
				asl(readMemByte(pc++));
				cycles += 5;
				break;
			case 22:
				asl(zpxAddr(readMemByte(pc++)));
				cycles += 6;
				break;
			case 14:
				asl(absAddr(readMemByte(pc++), readMemByte(pc++)));
				cycles += 6;
				break;
			case 30:
				asl(absxAddr(readMemByte(pc++), readMemByte(pc++)));
				cycles += 7;
				break;
			case 144:
				bcc(readMemByte(relAddr(readMemByte(pc++))));
				cycles += 2;
				break;
			case 176:
				bcs(readMemByte(relAddr(readMemByte(pc++))));
				cycles += 2;
				break;
			case 240:
				beq(readMemByte(relAddr(readMemByte(pc++))));
				cycles += 2;
				break;
			case 36:
				bit(readMemByte(readMemByte(pc++)));
				cycles += 3;
				break;
			case 44:
				bit(readMemByte(absAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 48:
				bmi(readMemByte(relAddr(readMemByte(pc++))));
				cycles += 2;
				break;
			case 208:
				bne(readMemByte(relAddr(readMemByte(pc++))));
				cycles += 2;
				break;
			case 16:
				bpl(readMemByte(relAddr(readMemByte(pc++))));
				cycles += 2;
				break;
			case 0:
				brk();
				cycles += 7;
				break;
			case 80:
				bvc(readMemByte(relAddr(readMemByte(pc++))));
				cycles += 2;
				break;
			case 112:
				bvs(readMemByte(relAddr(readMemByte(pc++))));
				cycles += 2;
				break;
			case 24:
				clc();
				cycles += 2;
				break;
			case 216:
				cld();
				cycles += 2;
				break;
			case 88:
				cli();
				cycles += 2;
				break;
			case 184:
				clv();
				cycles += 2;
				break;
			case 201:
				cmp(readMemByte(pc++));
				cycles += 2;
				break;
			case 197:
				cmp(readMemByte(readMemByte(pc++)));
				cycles += 3;
				break;
			case 213:
				cmp(readMemByte(zpxAddr(readMemByte(pc++))));
				cycles += 4;
				break;
			case 205:
				cmp(readMemByte(absAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 221:
				cmp(readMemByte(absxAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 217:
				cmp(readMemByte(absyAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 193:
				cmp(readMemByte(indxAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 6;
				break;
			case 209:
				cmp(readMemByte(indyAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 5;
				break;
			case 224:
				cpx(readMemByte(pc++));
				cycles += 2;
				break;
			case 228:
				cpx(readMemByte(readMemByte(pc++)));
				cycles += 3;
				break;
			case 236:
				cpx(readMemByte(absAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 192:
				cpy(readMemByte(pc++));
				cycles += 2;
				break;
			case 196:
				cpy(readMemByte(readMemByte(pc++)));
				cycles += 3;
				break;
			case 204:
				cpy(readMemByte(absAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 198:
				dec(readMemByte(pc++));
				cycles += 5;
				break;
			case 214:
				dec(zpxAddr(readMemByte(pc++)));
				cycles += 6;
				break;
			case 206:
				dec(absAddr(readMemByte(pc++), readMemByte(pc++)));
				cycles += 6;
				break;
			case 222:
				dec(absxAddr(readMemByte(pc++), readMemByte(pc++)));
				cycles += 7;
				break;
			case 202:
				dex();
				cycles += 2;
				break;
			case 136:
				dey();
				cycles += 2;
				break;
			case 73:
				eor(readMemByte(pc++));
				cycles += 2;
				break;
			case 69:
				eor(readMemByte(readMemByte(pc++)));
				cycles += 3;
				break;
			case 85:
				eor(readMemByte(zpxAddr(readMemByte(pc++))));
				cycles += 4;
				break;
			case 77:
				eor(readMemByte(absAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 93:
				eor(readMemByte(absxAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 89:
				eor(readMemByte(absyAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 65:
				eor(readMemByte(indxAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 6;
				break;
			case 81:
				eor(readMemByte(indyAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 5;
				break;
			case 230:
				inc(readMemByte(pc++));
				cycles += 5;
				break;
			case 246:
				inc(zpxAddr(readMemByte(pc++)));
				cycles += 6;
				break;
			case 238:
				inc(absAddr(readMemByte(pc++), readMemByte(pc++)));
				cycles += 6;
				break;
			case 254:
				inc(absxAddr(readMemByte(pc++), readMemByte(pc++)));
				cycles += 7;
				break;
			case 232:
				inx();
				cycles += 2;
				break;
			case 200:
				iny();
				cycles += 2;
				break;
			case 76:
				jmp(absAddr(readMemByte(pc++), readMemByte(pc++)));
				cycles += 3;
				break;
			case 108:
				jmp(indAddr(readMemByte(pc++), readMemByte(pc++)));
				cycles += 5;
				break;
			case 32:
				jsr(absAddr(readMemByte(pc++), readMemByte(pc++)));
				cycles += 6;
				break;
			case 169:
				lda(readMemByte(pc++));
				cycles += 2;
				break;
			case 165:
				lda(readMemByte(readMemByte(pc++)));
				cycles += 3;
				break;
			case 181:
				lda(readMemByte(zpxAddr(readMemByte(pc++))));
				cycles += 4;
				break;
			case 173:
				lda(readMemByte(absAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 189:
				lda(readMemByte(absxAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 185:
				lda(readMemByte(absyAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 161:
				lda(readMemByte(indxAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 6;
				break;
			case 177:
				lda(readMemByte(indyAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 5;
				break;
			case 162:
				ldx(readMemByte(pc++));
				cycles += 2;
				break;
			case 166:
				ldx(readMemByte(readMemByte(pc++)));
				cycles += 3;
				break;
			case 182:
				ldx(readMemByte(zpyAddr(readMemByte(pc++))));
				cycles += 4;
				break;
			case 174:
				ldx(readMemByte(absAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 190:
				ldx(readMemByte(absyAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 160:
				ldy(readMemByte(pc++));
				cycles += 2;
				break;
			case 164:
				ldy(readMemByte(readMemByte(pc++)));
				cycles += 3;
				break;
			case 180:
				ldy(readMemByte(zpxAddr(readMemByte(pc++))));
				cycles += 4;
				break;
			case 172:
				ldy(readMemByte(absAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 188:
				ldy(readMemByte(absxAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 74:
				lsra();
				cycles += 2;
				break;
			case 70:
				lsr(readMemByte(pc++));
				cycles += 5;
				break;
			case 86:
				lsr(zpxAddr(readMemByte(pc++)));
				cycles += 6;
				break;
			case 78:
				lsr(absAddr(readMemByte(pc++), readMemByte(pc++)));
				cycles += 6;
				break;
			case 94:
				lsr(absxAddr(readMemByte(pc++), readMemByte(pc++)));
				cycles += 7;
				break;
			case 234:
				nop();
				cycles += 2;
				break;
			case 9:
				ora(readMemByte(pc++));
				cycles += 2;
				break;
			case 5:
				ora(readMemByte(readMemByte(pc++)));
				cycles += 3;
				break;
			case 21:
				ora(readMemByte(zpxAddr(readMemByte(pc++))));
				cycles += 4;
				break;
			case 13:
				ora(readMemByte(absAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 29:
				ora(readMemByte(absxAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 25:
				ora(readMemByte(absyAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 1:
				ora(readMemByte(indxAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 6;
				break;
			case 17:
				ora(readMemByte(indyAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 5;
				break;
			case 72:
				pha();
				cycles += 3;
				break;
			case 8:
				php();
				cycles += 3;
				break;
			case 104:
				pla();
				cycles += 4;
				break;
			case 40:
				plp();
				cycles += 4;
				break;
			case 42:
				rola();
				cycles += 2;
				break;
			case 38:
				rol(readMemByte(pc++));
				cycles += 5;
				break;
			case 54:
				rol(zpxAddr(readMemByte(pc++)));
				cycles += 6;
				break;
			case 46:
				rol(absAddr(readMemByte(pc++), readMemByte(pc++)));
				cycles += 6;
				break;
			case 62:
				rol(absxAddr(readMemByte(pc++), readMemByte(pc++)));
				cycles += 7;
				break;
			case 106:
				rora();
				cycles += 2;
				break;
			case 102:
				ror(readMemByte(pc++));
				cycles += 5;
				break;
			case 118:
				ror(zpxAddr(readMemByte(pc++)));
				cycles += 6;
				break;
			case 110:
				ror(absAddr(readMemByte(pc++), readMemByte(pc++)));
				cycles += 6;
				break;
			case 126:
				ror(absxAddr(readMemByte(pc++), readMemByte(pc++)));
				cycles += 7;
				break;
			case 64:
				rti();
				cycles += 6;
				break;
			case 96:
				rts();
				cycles += 6;
				break;
			case 233:
				sbc(readMemByte(pc++));
				cycles += 2;
				break;
			case 229:
				sbc(readMemByte(readMemByte(pc++)));
				cycles += 3;
				break;
			case 245:
				sbc(readMemByte(zpxAddr(readMemByte(pc++))));
				cycles += 4;
				break;
			case 237:
				sbc(readMemByte(absAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 253:
				sbc(readMemByte(absxAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 249:
				sbc(readMemByte(absyAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 4;
				break;
			case 225:
				sbc(readMemByte(indxAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 6;
				break;
			case 241:
				sbc(readMemByte(indyAddr(readMemByte(pc++), readMemByte(pc++))));
				cycles += 5;
				break;
			case 56:
				sec();
				cycles += 2;
				break;
			case 248:
				sed();
				cycles += 2;
				break;
			case 120:
				sei();
				cycles += 2;
				break;
			case 133:
				sta(readMemByte(pc++));
				cycles += 3;
				break;
			case 149:
				sta(zpxAddr(readMemByte(pc++)));
				cycles += 4;
				break;
			case 141:
				sta(absAddr(readMemByte(pc++), readMemByte(pc++)));
				cycles += 4;
				break;
			case 157:
				sta(absxAddr(readMemByte(pc++), readMemByte(pc++)));
				cycles += 5;
				break;
			case 153:
				sta(absyAddr(readMemByte(pc++), readMemByte(pc++)));
				cycles += 5;
				break;
			case 129:
				sta(indxAddr(readMemByte(pc++), readMemByte(pc++)));
				cycles += 6;
				break;
			case 145:
				sta(indyAddr(readMemByte(pc++), readMemByte(pc++)));
				cycles += 6;
				break;
			case 134:
				stx(readMemByte(pc++));
				cycles += 3;
				break;
			case 150:
				stx(zpyAddr(readMemByte(pc++)));
				cycles += 4;
				break;
			case 142:
				stx(absAddr(readMemByte(pc++), readMemByte(pc++)));
				cycles += 4;
				break;
			case 132:
				sty(readMemByte(pc++));
				cycles += 3;
				break;
			case 148:
				sty(zpxAddr(readMemByte(pc++)));
				cycles += 4;
				break;
			case 140:
				sty(absAddr(readMemByte(pc++), readMemByte(pc++)));
				cycles += 4;
				break;
			case 170:
				tax();
				cycles += 2;
				break;
			case 168:
				tay();
				cycles += 2;
				break;
			case 186:
				tsx();
				cycles += 2;
				break;
			case 138:
				txa();
				cycles += 2;
				break;
			case 154:
				txs();
				cycles += 2;
				break;
			case 152:
				tya();
				cycles += 2;
				break;
			default:
				throw new RuntimeException("Illegal operation!");
			}
		}
	}
}
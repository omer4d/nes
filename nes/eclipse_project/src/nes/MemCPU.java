package nes;

public class MemCPU implements Memory {
	private BufferWrapper chunks[] = new BufferWrapper[8];
	
	public MemCPU(byte[] prg)
	{
		chunks[0] = new BufferWrapper(new byte[0x800], 0, 0x800);   // Work ram
		chunks[1] = new BufferWrapper(new byte[0x8], 0, 0x8); 	    // PPU regs
		chunks[2] = new BufferWrapper(new byte[0x2000], 0, 0x2000); // APU regs + cartridge exp
		chunks[3] = new BufferWrapper(new byte[0x2000], 0, 0x2000); // SRAM
		
		if(prg.length == 0x4000)
		{
			chunks[4] = new BufferWrapper(prg, 0x0000, 0x2000);
			chunks[5] = new BufferWrapper(prg, 0x2000, 0x2000);
			chunks[6] = new BufferWrapper(prg, 0x0000, 0x2000);
			chunks[7] = new BufferWrapper(prg, 0x2000, 0x2000);
		}
		
		else if(prg.length == 0x8000)
		{
			chunks[4] = new BufferWrapper(prg, 0x0000, 0x2000);
			chunks[5] = new BufferWrapper(prg, 0x2000, 0x2000);
			chunks[6] = new BufferWrapper(prg, 0x4000, 0x2000);
			chunks[7] = new BufferWrapper(prg, 0x6000, 0x2000);
		}
		
		else
		{
			throw new RuntimeException("Weird PRG ROM size: " + prg.length);
		}
	}
	
	int chunkIdx(int addr)
	{
		return (addr >> 13);
	}
	
	@Override
	public int read(int addr)
	{
		addr &= 0xFFFF;
		return chunks[chunkIdx(addr)].read(addr);
	}

	@Override
	public void write(int addr, int b)
	{
		Util.verifyShort(addr);
		Util.verifyByte(b);
		chunks[chunkIdx(addr)].write(addr, b);
	}
}

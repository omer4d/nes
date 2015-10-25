package nes;

public class MemoryIO {
	public static final int LOGICAL_CHUNK_LEN = 0x2000;
	
	private byte[] buffer;
	private int start, len;
	
	private static boolean isPow2(int n)
	{
		int i = 1;
		while(i < n) { i *= 2; }
		return i == n;
	}
	
	public MemoryIO(byte[] buffer, int start, int len)
	{
		if(!isPow2(len))
			throw new RuntimeException("Non-pow2 MemoryIO buffer length: " + len);
		
		this.buffer = buffer;
		this.start = start;
		this.len = len;
	}
	
	private int idx(int addr)
	{
		return start + (addr & (LOGICAL_CHUNK_LEN - 1) & (len - 1));
	}
	
	public int read(int addr)
	{
		return buffer[idx(addr)] & 0xFF;
	}
	
	public void write(int addr, int b)
	{
		buffer[idx(addr)] = (byte)b;
	}
	
	public void test()
	{
		// Set LOGICAL_CHUNK_LEN to 16!!!
		
//		byte[] p1 = {1, 2, 3, 4};
//		
//		byte[] p2 = {7, 8, 7, 8,
//					 7, 8, 7, 8,
//					 7, 8, 7, 8,
//					 7, 8, 7, 8};
//		
//		byte[] p3 = {5, 6, 5, 6,
//					 5, 6, 5, 6,
//					 5, 6, 5, 6,
//					 5, 6, 5, 6,
//					 3, 4, 3, 4,
//					 3, 4, 3, 4,
//					 3, 4, 3, 4,
//					 3, 4, 3, 4};
//		
//		byte[] p4 = {9, 8, 9, 8,
//				 	 9, 8, 9, 8,
//				 	 9, 8, 9, 8,
//				 	 9, 8, 9, 8};
//		
//		MemoryIO[] chunks = {new MemoryIO(p1, 0, p1.length),
//							 new MemoryIO(p2, 0, p2.length),
//							 new MemoryIO(p3, 0, 16),
//							 new MemoryIO(p3, 16, 16),
//							 new MemoryIO(p4, 0, p4.length),
//							 new MemoryIO(p4, 0, p4.length)};
//		
//		for(int addr = 0; addr < chunks.length * 16; ++addr)
//		{
//			System.out.print(chunks[(addr >> 4)].read8(addr));
//			if(addr % 4 == 3)
//				System.out.println();
//		}
	}
}

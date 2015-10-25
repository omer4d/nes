package nes;

import java.io.IOException;

public class Main {	
	static void printf(String fmt, Object... args)
	{
		System.out.print(String.format(fmt, args));
	}
	
	public static void main(String[] args) throws IOException
	{
		byte[] p1 = {1, 2, 3, 4};
		
		byte[] p2 = {7, 8, 7, 8,
					 7, 8, 7, 8,
					 7, 8, 7, 8,
					 7, 8, 7, 8};
		
		byte[] p3 = {5, 6, 5, 6,
					 5, 6, 5, 6,
					 5, 6, 5, 6,
					 5, 6, 5, 6,
					 3, 4, 3, 4,
					 3, 4, 3, 4,
					 3, 4, 3, 4,
					 3, 4, 3, 4};
		
		byte[] p4 = {9, 8, 9, 8,
				 	 9, 8, 9, 8,
				 	 9, 8, 9, 8,
				 	 9, 8, 9, 8};
		
		MemoryIO[] chunks = {new MemoryIO(p1, 0, p1.length),
							 new MemoryIO(p2, 0, p2.length),
							 new MemoryIO(p3, 0, 16),
							 new MemoryIO(p3, 16, 16),
							 new MemoryIO(p4, 0, p4.length),
							 new MemoryIO(p4, 0, p4.length)};
		
		for(int addr = 0; addr < chunks.length * 16; ++addr)
		{
			System.out.print(chunks[(addr >> 4)].read8(addr));
			if(addr % 4 == 3)
				System.out.println();
		}
		
		Main.class.getResourceAsStream("/nestest.nes").read();
	}
}

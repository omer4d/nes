package nes;

import java.io.IOException;
import java.io.InputStream;

public class Main {	
	static void printf(String fmt, Object... args)
	{
		System.out.print(String.format(fmt, args));
	}
	
	public static void main(String[] args) throws IOException
	{
		InputStream in = Main.class.getResourceAsStream("/nestest.nes");
		ROM rom = new ROM(in);
		CPU cpu = new CPU(rom.prg);
		CPU.DebugInfo info = new CPU.DebugInfo();
		
		cpu.pc = 0xC000;
		cpu.flags = 0x24;
		
		for(int i = 0; i < 100; ++i)
		{
			cpu.debugStep(info);
			info.print();
		}
		
		//cpu.run(0xC000, 333);
	}
}

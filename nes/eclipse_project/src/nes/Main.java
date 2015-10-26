package nes;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import static nes.Util.printf;

public class Main {	
	public static void test1() throws IOException
	{
		InputStream logInputStream = Main.class.getResourceAsStream("/nestest-bus-cycles.log");
		ArrayList<DebugInfo> debugInfos = LogParser.parse(logInputStream);
		logInputStream.close();
		
		InputStream in = Main.class.getResourceAsStream("/nestest.nes");
		ROM rom = new ROM(in);
		CPU cpu = new CPU(rom.prg);
		DebugInfo info = new DebugInfo();
		
		cpu.pc = 0xC000;
		cpu.flags = 0x24;
		
		for(int i = 0; i < 6000; ++i)
		{
			boolean die = false;
			
			try {
				cpu.debugStep(info);
			}catch(Exception e)
			{
				printf("**EXCEPTION**: %s\n", e.getMessage());
				die = true;
			}
			
			if(!info.equals(debugInfos.get(i)) || die)
			{
				for(int j = Math.max(i - 10, 0); j < i; ++j)
				{
					printf("%s | %s\n", debugInfos.get(j).asm, debugInfos.get(j));
				}
					
				printf("--------------------\n");
				printf("EXP: %s\n", debugInfos.get(i));
				printf("GOT: %s\n", info);
				
				//printf("%04X\n", cpu.readMemByte(cpu.buggyReadMemShort(0x89)));
				
				break;
			}
		}
		
		System.out.println("Ended!");
	}
	
	/*
	public static void test2() throws IOException
	{
		InputStream in = Main.class.getResourceAsStream("/official_only.nes");
		ROM rom = new ROM(in);
		CPU cpu = new CPU(rom.prg);
		DebugInfo info = new DebugInfo();
		
		for(int i = 0; i < 1000; ++i)
		{
			cpu.step();
		}
		
		System.out.println("Ended!");
	}*/
	
	public static void main(String[] args) throws IOException
	{
		test1();
	}
}

package nes;

import java.io.IOException;
import java.io.InputStream;
import static nes.Util.printf;

public class ROM {
	public byte prg[];
	public final int prgSize;
	
	public ROM(InputStream in) throws IOException
	{
		byte[] header = new byte[16];
		in.read(header, 0, 16);
		
		printf("Tag: %x %x %x %x\n", (header[0] & 0xFF),
								   	 (header[1] & 0xFF),
								     (header[2] & 0xFF),
								     (header[3] & 0xFF));
		
		prgSize = (header[4] & 0xFF) * 0x4000;
		printf("PRG rom size: %d KiB\n", prgSize / 1024);
		
		prg = new byte[prgSize];
		
		in.read(prg, 0, prgSize);
	}
}

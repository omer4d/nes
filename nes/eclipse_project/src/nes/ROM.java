package nes;

import java.io.IOException;
import java.io.InputStream;
import static nes.Util.printf;

public class ROM {
	public byte prg[];
	public byte chr[];

	public ROM(InputStream in) throws IOException
	{
		byte[] header = new byte[16];
		in.read(header, 0, 16);
		
		printf("Tag: %x %x %x %x\n", (header[0] & 0xFF),
								   	 (header[1] & 0xFF),
								     (header[2] & 0xFF),
								     (header[3] & 0xFF));
		
		int prgSize = (header[4] & 0xFF) * 0x4000;
		int chrSize = (header[5] & 0xFF) * 0x2000;
		
		printf("PRG rom size: %d KiB\n", prgSize / 1024);
		printf("CHR rom size: %d KiB\n", chrSize / 1024);
		
		prg = new byte[prgSize];
		in.read(prg, 0, prgSize);
		
		chr = new byte[chrSize];
		in.read(chr, 0, chrSize);
	}
	
	public void drawTile(Bitmap bmp, int idx, int x0, int y0)
	{
		int pal[] = {Bitmap.makecol(64, 64, 64),
					  Bitmap.makecol(255, 128, 128),
					  Bitmap.makecol(128, 255, 128),
					  Bitmap.makecol(128, 128, 255)};
		 
		for(int y = 0; y < 8; ++y)
		{
			int bpl1 = chr[idx * 16 + y];
			int bpl2 = chr[idx * 16 + 8 + y];
			
			for(int x = 0; x < 8; ++x)
			{
				int col = ((bpl1 & (1 << x)) == 0 ? 0 : 1) |
						  ((bpl2 & (1 << x)) == 0 ? 0 : 2);
				bmp.putpixel(x0 + 7 - x, y0 + y, pal[col]);
			}
		}
	}
	
	public void drawTiles(Bitmap bmp)
	{
		for(int i = 0; i < chr.length / 16; ++i)
		{
			drawTile(bmp, i, (i % 16) * 8, (i / 16) * 8);
		}
	}
}

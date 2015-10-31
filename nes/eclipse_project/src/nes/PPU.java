package nes;

public class PPU {
	final static int PATTERN_TABLE_START = 0x0000;
	final static int NAMETABLE_START = 0x2000;
	final static int PAL_START = 0x3F00;
	
	byte[] memChunk1 = new byte[0x3000]; // 2 pts (0x2000) + 4 nts (0x1000) 
	byte[] memChunk2 = new byte[0x20]; // pal
	byte[] oam = new byte[0xFF];
	
	int[] colors;
	int[] zbuff = new int[256];
	int scanline = 0;
	
	public PPU(int[] colors, byte[] chr)
	{
		this.colors = colors;
		
		// Copy pattern tables 0 and 1:
		for(int i = 0; i < 0x2000; ++i)
			memChunk1[i] = chr[(i + 0x1000) & 0x1FFF];
		
		// Fill nametables:
		
		int[] nt0 = {
			0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,
			0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,

			0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,
			0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,

			0x24,0x24,0x24,0x24,0x45,0x45,0x24,0x24,0x45,0x45,0x45,0x45,0x45,0x45,0x24,0x24,
			0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x53,0x54,0x24,0x24,

			0x24,0x24,0x24,0x24,0x47,0x47,0x24,0x24,0x47,0x47,0x47,0x47,0x47,0x47,0x24,0x24,
			0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x24,0x55,0x56,0x24,0x24};
		
		int[] attrib = {0x00, 0x10, 0x50, 0x10, 0x00, 0x00, 0x00, 0x30};
		
		int[] pal = {0x22,0x29,0x1A,0x0F,  0x22,0x36,0x17,0x0F, // BG
					 0x22,0x30,0x21,0x0F,  0x22,0x27,0x17,0x0F, 
					 
					 0x22,0x1C,0x15,0x14,  0x22,0x02,0x38,0x3C, // Sprites
					 0x22,0x1C,0x15,0x14,  0x22,0x02,0x38,0x3C};
		
		// Copy nametable 0:
		for(int i = 0; i < nt0.length; ++i)
			memChunk1[NAMETABLE_START + i] = (byte)nt0[i];
		
		
		for(int i = 0; i < attrib.length; ++i)
			memChunk1[NAMETABLE_START + 32*30 + i] = (byte)attrib[i];
		
		// Copy pal:
		for(int i = 0; i < pal.length; ++i)
			memChunk2[i] = (byte)pal[i];
		
		int[] sprites = {0x80, 0x32, 0x00, 0x80,   //sprite 0
				   		 0x80, 0x33, 0x00, 0x88,   //sprite 1
				   		 0x88, 0x34, 0x00, 0x80,   //sprite 2
				   		 0x88, 0x35, 0x00, 0x88};   //sprite 3
		
		for(int i = 0; i < sprites.length; ++i)
			oam[i] = (byte)sprites[i];
		for(int i = sprites.length; i < oam.length; ++i)
			oam[i] = (byte)0xFF; // Push unused sprites off the screen to get them clipped.
	}
	
	public void renderScanline(Bitmap target)
	{
		int tileRowStart = NAMETABLE_START + (scanline / 8) * 32;
		int tileLine = scanline % 8;
		
		
		int pal[] = {Bitmap.makecol(64, 64, 64),
				  Bitmap.makecol(255, 128, 128),
				  Bitmap.makecol(128, 255, 128),
				  Bitmap.makecol(128, 128, 255),
				  Bitmap.makecol(32, 32, 32),
				  Bitmap.makecol(128, 64, 64),
				  Bitmap.makecol(64, 128, 64),
				  Bitmap.makecol(64, 64, 128),
				  Bitmap.makecol(255, 255, 255)
				  };
		
		for(int x = 0; x < 256; ++x)
		{
			int tileIdx = memChunk1[tileRowStart + x / 8] & 0xFF;
			int bpl1 = memChunk1[PATTERN_TABLE_START + tileIdx * 16 + tileLine] & 0xFF;
			int bpl2 = memChunk1[PATTERN_TABLE_START + tileIdx * 16 + 8 + tileLine] & 0xFF;
			
			int cellIdx = (scanline/32)*8 + x/32; 
			
			int attr = memChunk1[NAMETABLE_START + 32*30 + cellIdx] & 0xFF;
			
			int subcell = (x / 16) % 2 + ((scanline / 16) % 2) * 2;
			
			attr = (attr >>> (subcell*2)) & 3;
			
			int col = ((bpl1 & (1 << (7 - (x % 8))   )) == 0 ? 0 : 1) |
					  ((bpl2 & (1 << (7 - (x % 8))   )) == 0 ? 0 : 2);
			
			target.putpixel(x, scanline, colors[memChunk2[attr * 4 + col] & 0xFF]);
			zbuff[x] = 0xFF;
		}
		
		int spriteCounter = 0;
		
		for(int i = 0; i < oam.length / 4; ++i)
		{
			int spriteX = oam[i * 4 + 3] & 0xFF;
			int spriteY = oam[i * 4] & 0xFF;
			
			if(scanline >= spriteY && scanline < spriteY + 8)
			{
				int tileIdx = oam[i * 4 + 1] & 0xFF;
				int bpl1 = memChunk1[PATTERN_TABLE_START + 0x1000 + tileIdx * 16 + scanline - spriteY] & 0xFF;
				int bpl2 = memChunk1[PATTERN_TABLE_START + 0x1000 + tileIdx * 16 + 8 + scanline - spriteY] & 0xFF;
				int ps = oam[i * 4 + 2] & 3;
				
				for(int x = 0; x < 8; ++x)
					if(spriteCounter < zbuff[spriteX + x])
					{
						int col = ((bpl1 & (1 << (7 - (x % 8))   )) == 0 ? 0 : 1) |
								  ((bpl2 & (1 << (7 - (x % 8))   )) == 0 ? 0 : 2);
						
						if(col != 0)
							target.putpixel(spriteX + x, scanline, colors[memChunk2[16 + ps * 4 + col] & 0xFF]);
						zbuff[x] = spriteCounter;
					}
				
				++spriteCounter;
			}
		}
		
		scanline = (scanline + 1) % 240;
	}
}

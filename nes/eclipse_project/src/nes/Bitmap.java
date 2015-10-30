package nes;

public class Bitmap {
	public final int[] data;
	public final int w, h;
	
	public Bitmap(int w, int h)
	{
		if(!Util.isPow2(w))
			throw new RuntimeException("Bitmap width must be a power of 2! (Actual: " + w + ").");
		
		this.data = new int[w * h];
		this.w = w;
		this.h = h;
	}
	
	public static int makecol(int r, int g, int b)
	{
		return r | g << 8 | b << 16;
	}
	
	public void putpixel(int x, int y, int col)
	{
		data[y * w + x] = col;
	}
}

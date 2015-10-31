package nes;

public class Util {
	public static void printf(String fmt, Object... args)
	{
		System.out.print(String.format(fmt, args));
	}
	
	public static boolean isPow2(int n)
	{
		int i = 1;
		while(i < n) { i *= 2; }
		return i == n;
	}
	
	public static void verifyByte(int x)
	{
		if(x < 0 || x > 0xFF)
			throw new IllegalArgumentException("Byte out of range: " + x);
	}
	
	public static void verifyShort(int x)
	{
		if(x < 0 || x > 0xFFFF)
			throw new IllegalArgumentException("Short out of range: " + x);
	}
}

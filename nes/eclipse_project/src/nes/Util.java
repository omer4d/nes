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
}

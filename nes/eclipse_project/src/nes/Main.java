package nes;

public class Main {	
	static void printf(String fmt, Object... args)
	{
		System.out.print(String.format(fmt, args));
	}
	
	public static void main(String[] args)
	{
		printf("%x", 0xFFFF);
	}
}

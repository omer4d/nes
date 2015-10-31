package nes;

public interface Memory {
	public int read(int addr);
	public void write(int addr, int b);
}

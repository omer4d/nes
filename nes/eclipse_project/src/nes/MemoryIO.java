package nes;

public class MemoryIO {
	private static final int LOGICAL_CHUNK_LEN = 16;
	
	private byte[] buffer;
	private int start, len;
	
	public MemoryIO(byte[] buffer, int start, int len)
	{
		this.buffer = buffer;
		this.start = start;
		this.len = len;
	}
	
	private int idx(int addr)
	{
		return start + (addr & (LOGICAL_CHUNK_LEN - 1) & (len - 1));
	}
	
	public int read8(int addr)
	{
		return buffer[idx(addr)];
	}
}

package nes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogParser {
	private static Pattern pattern = Pattern.compile("([^\\s]{4})[\\s]*((?:[^\\s]{2} ){1,3})[\\s]*(.*)A:(..) X:(..) Y:(..) P:(..) SP:(..) CPUC:(.+)[\\s]*");
	
	private static int hex(String str)
	{
		return Integer.parseInt(str, 16);
	}
	
	private static DebugInfo parseLine(String line)
	{
		DebugInfo dbi = new DebugInfo();
		
		Matcher m = pattern.matcher(line);
		
		if(!m.matches())
			throw new RuntimeException("Invalid log line: " + line);
		
		String[] mcParts = m.group(2).split(" ");
		
		dbi.pc = hex(m.group(1));
		dbi.instr = hex(mcParts[0]);
		dbi.byte1 = mcParts.length > 1 ? hex(mcParts[1]) : -1;
		dbi.byte2 = mcParts.length > 2 ? hex(mcParts[2]) : -1;
		dbi.asm = m.group(3);
		dbi.a = hex(m.group(4));
		dbi.x = hex(m.group(5));
		dbi.y = hex(m.group(6));
		dbi.flags = hex(m.group(7));
		dbi.sp = hex(m.group(8));
		dbi.cyc = Integer.parseInt(m.group(9));
		
		return dbi;
	}
	
	public static ArrayList<DebugInfo> parse(InputStream in) throws IOException
	{
		ArrayList<DebugInfo> out = new ArrayList<DebugInfo>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		
	    String line;
	    Pattern ignorePatt = Pattern.compile(".*READ.*|.*WRITE.*");
	    
	    while ((line = reader.readLine()) != null)
	    {
	    	Matcher m = ignorePatt.matcher(line);
	    	if(!m.matches())
	    		out.add(parseLine(line));
	    }
		
		reader.close();
		
		return out;
	}
}

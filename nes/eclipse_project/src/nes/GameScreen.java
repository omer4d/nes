package nes;

import static engine.Keys.KEY_ESCAPE;
import static engine.Keys.MOUSE_2;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

import engine.Window;
import engine.WindowListenerAdapter;

public class GameScreen extends WindowListenerAdapter {
   	static final int WIDTH = 256;
	static final int HEIGHT = 256;
	
	int texId;
	IntBuffer buff = BufferUtils.createIntBuffer(WIDTH * HEIGHT);
	Bitmap tmp = new Bitmap(WIDTH, HEIGHT);
	//int[] tmp = new int[WIDTH*HEIGHT];
	
	ROM rom;
	PPU ppu;
	
	private static ROM loadRom(String path)
	{
		InputStream in = Main.class.getResourceAsStream(path);
		try {
			return new ROM(in);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static int[] loadPal(String path)
	{
		InputStream in = Main.class.getResourceAsStream(path);
		int[] pal = new int[64];
		
		try {
			for(int i = 0; i < 64; ++i)
				pal[i] = Bitmap.makecol(in.read(), in.read(), in.read());
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return pal;
	}
	
	public void create(Window win)
	{
		rom = loadRom("/background.nes");
		ppu = new PPU(loadPal("/nespalette.pal"), rom.chr);
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, 512, 512, 0, 1, -1);

		glEnable(GL_TEXTURE_2D);
		
		texId = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, texId);
		
		 glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
         glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
         
         GLFW.glfwSwapInterval(1);
	}
	
	public void resize(Window win, int w, int h)
	{
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, 512, 512, 0, 1, -1);
		glViewport(0, 0, w, h);
	}
	
	@Override
	public void render(Window win, double dt)
	{
		if(win.justReleased(MOUSE_2))
		{ 
			System.out.println(win.getMouseX() + ", " + win.getMouseY());
		}
		
		if(win.justPressed(KEY_ESCAPE))
			win.close();
		
        buff.clear();
		
        //tmp.putpixel(0, 255, Bitmap.makecol(255, 0, 255));
        
        //rom.drawTiles(tmp);
        
        //for(int y = 0; y < 256; ++y)
        	//for(int x = 0; x < 256; ++x)
        		//tmp.putpixel(x, y, ppu.colors[x/4]);
        
        for(int i = 0; i < 240; ++i)
        	ppu.renderScanline(tmp);
        //rom.drawTiles(tmp);
        
		buff.put(tmp.data);
		buff.flip();
		
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, WIDTH, HEIGHT, 0,
	             GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, buff);
		
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
        
		glBegin(GL_QUADS);
			glTexCoord2f(0, 0);
			glVertex2f(0, 0);
			
			glTexCoord2f(1, 0);
			glVertex2f(512, 0);
			
			glTexCoord2f(1, 1);
			glVertex2f(512, 512);
			
			glTexCoord2f(0, 1);
			glVertex2f(0, 512);
		glEnd();
		
		//System.out.println(win.getFps());
	}
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package animationproject;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.io.xml.Dom4JDriver;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import org.lwjgl.LWJGLException;
import  org.lwjgl.opengl.ARBMultitexture;
import static org.lwjgl.opengl.ARBMultitexture.GL_TEXTURE0_ARB;
import static org.lwjgl.opengl.ARBMultitexture.GL_TEXTURE1_ARB;
import static org.lwjgl.opengl.ARBMultitexture.glActiveTextureARB;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glFramebufferTexture2DEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glGenFramebuffersEXT;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_INT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glFlush;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameterf;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL11.glViewport;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

/**
 *
 * @author ivko0314
 */
public class TextureAtlasTestFBO {

    static int width = 512;
    static int height = 512;
    static String title = "test";

    static Texture texture = null;

    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        init();
        loadTexture();

        boolean done = false;
        int levelTextureID = loadLevel();

        // Основной цикл.
        while (!done) {
            glClear(GL_COLOR_BUFFER_BIT);
            render(levelTextureID);
            /*for (Ground g : gr) {
             drawGround(g, (int) (Math.random()*width), (int) (Math.random()*height));
             }*/
            Display.update();
            Display.sync(100);

            if (Display.isCloseRequested()) {
                done = true;
            }
            //Thread.sleep(50);
        }

        Display.destroy();

    }
    
    private static void render(int colorTextureID) {
        glViewport(0, 0, width, height);									// set The Current Viewport to the fbo size
        glEnable(GL_TEXTURE_2D);										// enable texturing
        //glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);					// switch to rendering on the framebuffer

        glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
        glClear(GL_COLOR_BUFFER_BIT );			// Clear Screen And Depth Buffer on the framebuffer to black

        glBindTexture(GL_TEXTURE_2D, colorTextureID);					// bind our FBO texture

        glViewport(0, 0, width, height);									// set The Current Viewport

        glLoadIdentity();												// Reset The Modelview Matrix
        
        glColor3f(1, 1, 1);												// set the color to white
        glTranslatef(-10.0f, -10.0f, 10.0f);								// Translate 6 Units Into The Screen and then rotate
        glBegin(GL_QUADS);
        {
            glTexCoord2f(0, 0);
            glVertex2f(0, 0);
            glTexCoord2f(1, 0);
            glVertex2f(width, 0);
            glTexCoord2f(1, 1);
            glVertex2f(width, height);
            glTexCoord2f(0, 1);
            glVertex2f(0, height);
        }
        glEnd();

        glDisable(GL_TEXTURE_2D);
        glFlush();
    }

    static int loadLevel() throws FileNotFoundException {
        XStream xstream = new XStream(new PureJavaReflectionProvider(), new Dom4JDriver());
        // new PureJavaReflectionProvider() - будет использоваться конструктор по умолчанию, чтобы отсутствующие в xml поля не были null

        xstream.processAnnotations(Ground.class);
        xstream.processAnnotations(Level.class);
        Ground[] gr = new Ground[0];
        xstream.alias("Grounds", gr.getClass());
        Reader reader = new FileReader("Grounds.xml");
        gr = (Ground[]) xstream.fromXML(reader);
        for (Ground g : gr) {

            System.out.println(g.id);
        }
        int[] x = new int[0];
        xstream.alias("row", x.getClass());
        reader = new FileReader("level.xml");
        Level level = (Level) xstream.fromXML(reader);

        int framebufferID = glGenFramebuffersEXT();
        int colorTextureID = glGenTextures();
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, framebufferID); 						// switch to the new framebuffer

        // initialize color texture
        glActiveTextureARB(GL_TEXTURE0_ARB);
        glBindTexture(GL_TEXTURE_2D, colorTextureID);									// Bind the colorbuffer texture
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);				// make it linear filterd
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 64*5, 64*5, 0, GL_RGBA, GL_INT, (java.nio.ByteBuffer) null);	// Create the texture data
        glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D, colorTextureID, 0); // attach it to the framebuffer

        glViewport(0, 0, 512, 512);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT /*| GL_DEPTH_BUFFER_BIT*/);			// Clear Screen And Depth Buffer on the fbo to red
        glLoadIdentity();

        //glActiveTextureARB(colorTextureID);
        //////////////
        int i = 0;
        int j = 0;
        for (int[] col : level.grounds) {
            for (int row : col) {
                Ground ground = gr[row];
                drawGround(ground, j * 64, i * 64);
                j++;
            }
            i++;
            j = 0;
        }
        //glActiveTextureARB(GL_TEXTURE0_ARB);
        /////////////
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
        //glActiveTextureARB(GL_TEXTURE0_ARB);
        return colorTextureID;
    }

    static void drawGround(Ground ground, float x, float y) {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        //GL11.glPushMatrix();
/*x+=100;
         y+=100;*/
        //texture.bind();
        //glActiveTextureARB(GL_TEXTURE1_ARB);
        //ARBMultitexture.glClientActiveTextureARB(width);
        GL11.glBindTexture(GL_FRAMEBUFFER_EXT, texture.getTextureID());
        //glActiveTextureARB(GL_TEXTURE0_ARB);
        GL11.glViewport(0, 0, width, height);
        final int a = texture.getImageHeight() / 64;
        final float varX = 1f / a;
        final float varY = 1f / a;

        int textureWidth = texture.getTextureWidth();
        int textureHeight = texture.getTextureHeight();

        float row = varX * (ground.col - 1);
        float col = varY * (ground.row - 1);
        
        GL11.glBegin(GL11.GL_QUADS);
        {
            GL11.glTexCoord2f(row, col);
            GL11.glVertex2f(x, y);
            GL11.glTexCoord2f(row + varX, col);
            GL11.glVertex2f(x + textureWidth / a, y);
            GL11.glTexCoord2f(row + varX, col + varY);
            GL11.glVertex2f(x + textureWidth / a, y + textureHeight / a);
            GL11.glTexCoord2f(row, col + varY);
            GL11.glVertex2f(x, y + textureHeight / a);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_QUADS);
    }

    static void loadTexture() {
        try {
            texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("resources\\grounds.png"));

            /*System.out.println("Texture loaded: " + texture);
             System.out.println(">> Image width: " + texture.getImageWidth());
             System.out.println(">> Image height: " + texture.getImageHeight());
             System.out.println(">> Texture width: " + texture.getTextureWidth());
             System.out.println(">> Texture height: " + texture.getTextureHeight());
             System.out.println(">> Texture ID: " + texture.getTextureID());*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void init() {
        try {
            DisplayMode displayMode = new DisplayMode(width, height);
            Display.setDisplayMode(displayMode);
            Display.setTitle(title);
            Display.create();
            Display.setVSyncEnabled(true);
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(0);
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Включить Alpha blending
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glViewport(0, 0, width, height);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, width, height, 0, 1, -1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }
}

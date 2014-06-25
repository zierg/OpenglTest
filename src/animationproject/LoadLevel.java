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
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL11.*;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

/**
 *
 * @author ivko0314
 */
public class LoadLevel {

    private final static int width = 512;
    private final static int height = 512;
    private static Texture groundsTexture;
    private static Ground[] grounds;
    private static int levelTextureID;
    private static int framebufferID;
    private static int levelWidth;
    private static int levelHeight;

    public static void main(String[] args) throws IOException {
        initOpenGL();
        loadGroundTexture();
        loadAndDrawLevel();
        boolean done = false;
        while (!done) {

            render();

            Display.update();
            Display.sync(100);

            if (Display.isCloseRequested()) {
                done = true;
            }
        }

        Display.destroy();
    }

    private static void initOpenGL() throws IOException {
        try {
            DisplayMode displayMode = new DisplayMode(width, height);
            Display.setDisplayMode(displayMode);
            Display.setTitle("FBOtest");
            Display.create();
            Display.setVSyncEnabled(true);
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(0);
        }

        glViewport(0, 0, width, height);								// Reset The Current Viewport
        glMatrixMode(GL_PROJECTION);								// Select The Projection Matrix
        glLoadIdentity();
        glOrtho(0, width, height, 0, 1, -1);// Reset The Projection Matrix
        //GLU.gluPerspective(45.0f, 512f / 512f, 1.0f, 100.0f);		// Calculate The Aspect Ratio Of The Window	
        glMatrixMode(GL_MODELVIEW);								// Select The Modelview Matrix
        glLoadIdentity();
    }

    private static void loadGroundTexture() throws IOException {
        groundsTexture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("resources\\grounds.png"));
        XStream xstream = new XStream(new PureJavaReflectionProvider(), new Dom4JDriver());
        // new PureJavaReflectionProvider() - будет использоваться конструктор по умолчанию, чтобы отсутствующие в xml поля не были null
        xstream.processAnnotations(Ground.class);
        Ground[] gr = new Ground[0];
        xstream.alias("Grounds", gr.getClass());
        Reader reader = new FileReader("Grounds.xml");
        grounds = (Ground[]) xstream.fromXML(reader);
    }

    private static void loadAndDrawLevel() throws FileNotFoundException {
        // Загрузка уровня
        XStream xstream = new XStream(new PureJavaReflectionProvider(), new Dom4JDriver());
        // new PureJavaReflectionProvider() - будет использоваться конструктор по умолчанию, чтобы отсутствующие в xml поля не были null
        xstream.processAnnotations(Level.class);
        int[] x = new int[0];
        xstream.alias("row", x.getClass());
        Reader reader = new FileReader("level.xml");
        Level level = (Level) xstream.fromXML(reader);

        // Рисование уровня
        framebufferID = glGenFramebuffersEXT();
        levelTextureID = glGenTextures();												// and a new texture used as a color buffer
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, framebufferID); 						// switch to the new framebuffer

        final int GROUND_SIZE = 64;
        levelWidth = level.grounds.length * GROUND_SIZE;
        levelHeight = level.grounds.length * GROUND_SIZE;
        // initialize color texture
        glBindTexture(GL_TEXTURE_2D, levelTextureID);									// Bind the colorbuffer texture
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);				// make it linear filterd
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, levelWidth, levelHeight, 0, GL_RGBA, GL_INT, (java.nio.ByteBuffer) null);	// Create the texture data
        glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D, levelTextureID, 0); // attach it to the framebuffer

        glViewport(0, 0, levelWidth, levelHeight);
        glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);			// Clear Screen And Depth Buffer on the fbo to red
        glLoadIdentity();

        glEnable(GL_TEXTURE_2D);
        glColor3f(1, 1, 1);
        //glBegin(GL_QUADS);

        glBindTexture(GL_TEXTURE_2D, groundsTexture.getTextureID());
        glViewport(0, 0, levelWidth, levelHeight);
        
        int i = 0;
        int j = 0;
        for (int[] col : level.grounds) {
            for (int row : col) {
                Ground ground = grounds[row];
                drawGround(ground, j * 64, i * 64);
                j++;
            }
            i++;
            j = 0;
        }

        //glEnd();
        glDisable(GL_QUADS);
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
    }

    private static void drawGround(Ground ground, float x, float y) {
        //texture.bind();
        //glViewport(0, 0, width, height);
        final int a = groundsTexture.getImageHeight() / 64;
        final float varX = 1f / a;
        final float varY = 1f / a;

        int textureWidth = groundsTexture.getTextureWidth();
        int textureHeight = groundsTexture.getTextureHeight();
        System.out.println("textureWidth = " + textureWidth);
        System.out.println("textureHeight = " + textureHeight);

        
        float row = varX * (ground.col - 1);
        float col = varY * (ground.row - 1);
        
        float ccol = x/64;
        
        System.out.println("ccol=" + ccol);
        float ww = width/5.0f;
        System.out.println("ww = " + ww);
        System.out.println("x = " + x);
        x+=38.3f*ccol;
        System.out.println("x+x*ww = " + x);
        float wScale = width/levelWidth;
        float hScale = levelHeight/height;

        glBegin(GL_QUADS);
        {
            glTexCoord2f(row, col);
            glVertex2f(x, levelHeight-y);
            glTexCoord2f(row + varX, col);
            glVertex2f((x + ww), levelHeight-y);
            glTexCoord2f(row + varX, col + varY);
            glVertex2f((x + ww), levelHeight-(y + textureHeight / a));
            glTexCoord2f(row, col + varY);
            glVertex2f(x, levelHeight-(y + textureHeight / a));
        }
        glEnd();
    }
    
    private static void render() {
        //glViewport(0, 0, width, height);									// set The Current Viewport to the fbo size
        glEnable(GL_TEXTURE_2D);		
        //glBindTexture(GL_TEXTURE_2D, 0);// enable texturing
        //glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);					// switch to rendering on the framebuffer

        glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
        glClear(GL_COLOR_BUFFER_BIT );			// Clear Screen And Depth Buffer on the framebuffer to black

        glBindTexture(GL_TEXTURE_2D, levelTextureID);					// bind our FBO texture

        glViewport(0, 0, width, height);									// set The Current Viewport

        glLoadIdentity();												// Reset The Modelview Matrix
        
        glColor3f(1, 1, 1);												// set the color to white
        //glTranslatef(-10.0f, -10.0f, 10.0f);								// Translate 6 Units Into The Screen and then rotate
        glBegin(GL_QUADS);
        {
            glTexCoord2f(0, 0);
            glVertex2f(0, 0);
            glTexCoord2f(1, 0);
            glVertex2f(levelWidth, 0);
            glTexCoord2f(1, 1);
            glVertex2f(levelWidth, levelHeight);
            glTexCoord2f(0, 1);
            glVertex2f(0, levelHeight);
        }
        glEnd();

        glDisable(GL_TEXTURE_2D);
        glFlush();
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package animationproject;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.EXTFramebufferObject.*;
import org.lwjgl.util.glu.GLU;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

/**
 *
 * @author ivko0314
 */
public class FBOTest {

    private static final int width = 512;
    private static final int height = 512;

    // переменные для хранения индексов текстур
    private static int colorTexture = 0;

    // переменная для хранения идентификатора FBO
    private static int renderFBO = 0;

    // переменная для хранения состояния FBO
    private static int fboStatus;
    private static int depthTexture;
    private static int framebufferID;
    private static int colorTextureID;

    public static void main(String args[]) throws IOException {
        init();

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

    static void init() throws IOException {
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

        glViewport(0, 0, 512, 512);								// Reset The Current Viewport
        glMatrixMode(GL_PROJECTION);								// Select The Projection Matrix
        glLoadIdentity();	
        glOrtho(0, width, height, 0, 1, -1);// Reset The Projection Matrix
        //GLU.gluPerspective(45.0f, 512f / 512f, 1.0f, 100.0f);		// Calculate The Aspect Ratio Of The Window	
        glMatrixMode(GL_MODELVIEW);								// Select The Modelview Matrix
        glLoadIdentity();

        framebufferID = glGenFramebuffersEXT();											// create a new framebuffer
        colorTextureID = glGenTextures();												// and a new texture used as a color buffer

        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, framebufferID); 						// switch to the new framebuffer

        // initialize color texture
        glBindTexture(GL_TEXTURE_2D, colorTextureID);									// Bind the colorbuffer texture
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);				// make it linear filterd
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 512, 512, 0, GL_RGBA, GL_INT, (java.nio.ByteBuffer) null);	// Create the texture data
        glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D, colorTextureID, 0); // attach it to the framebuffer

        glViewport(0, 0, 512, 512);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT /*| GL_DEPTH_BUFFER_BIT*/);			// Clear Screen And Depth Buffer on the fbo to red
        glLoadIdentity();

        Texture texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("resources\\grounds.png"));
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, texture.getTextureID());	
        glColor3f(1, 1, 1);	
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex2f(50, 512-50);
        glTexCoord2f(1, 0);
        glVertex2f(150, 512-50);
        glTexCoord2f(1, 1);
        glVertex2f(150, 512-150);
        glTexCoord2f(0, 1);
        glVertex2f(50, 512-150);
        glEnd();
        glDisable(GL_QUADS);
        glBindTexture(GL_TEXTURE_2D, 0);
        glColor3f(1, 1, 0);
        glBegin(GL_QUADS);
        glVertex2f(350, 512-50);
        glVertex2f(450, 512-50);
        glVertex2f(450, 512-150);
        glVertex2f(350, 512-150);
        glEnd();
        
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);									// Swithch back to normal framebuffer rendering
    }

    private static void render() {
        //glViewport(0, 0, 512, 512);									// set The Current Viewport to the fbo size
        glEnable(GL_TEXTURE_2D);										// enable texturing
        //glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);					// switch to rendering on the framebuffer

        glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
        glClear(GL_COLOR_BUFFER_BIT );			// Clear Screen And Depth Buffer on the framebuffer to black

        glBindTexture(GL_TEXTURE_2D, colorTextureID);					// bind our FBO texture

        glViewport(0, 0, 512, 512);									// set The Current Viewport

        glLoadIdentity();												// Reset The Modelview Matrix
        
        glColor3f(1, 1, 1);												// set the color to white
        //glTranslatef(-10.0f, -10.0f, 10.0f);								// Translate 6 Units Into The Screen and then rotate
        glBegin(GL_QUADS);
        {
            glTexCoord2f(0, 0);
            glVertex2f(0, 0);
            glTexCoord2f(1, 0);
            glVertex2f(512, 0);
            glTexCoord2f(1, 1);
            glVertex2f(512, 512);
            glTexCoord2f(0, 1);
            glVertex2f(0, 512);
        }
        glEnd();

        glDisable(GL_TEXTURE_2D);
        glFlush();
    }
}

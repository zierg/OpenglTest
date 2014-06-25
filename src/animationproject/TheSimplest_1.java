/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package animationproject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.EXTFramebufferObject;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glFramebufferTexture2DEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glGenFramebuffersEXT;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author ivko0314
 */
public class TheSimplest_1 {

    static int width = 800;
    static int height = 600;
    static String title = "test";
    private static int framebufferID;
    private static int colorTextureID;
    private static int depthRenderBufferID;
    static int fboID;

    public static void main(String[] args) throws LWJGLException {
        //Create FBO
        DisplayMode displayMode = new DisplayMode(width, height);
        Display.setDisplayMode(displayMode);
        Display.setTitle(title);
        Display.create();
        Display.setVSyncEnabled(true);
        DisplayMode display = Display.getDisplayMode();

        int fboWidth = display.getWidth();
        int fboHeight = display.getHeight();

        int fboTextureID = glGenTextures();
        fboID = glGenFramebuffersEXT();

        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fboID);

        glBindTexture(GL_TEXTURE_2D, fboTextureID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, fboWidth, fboHeight, 0, GL_RGBA, GL_INT,
                (java.nio.IntBuffer) null);

        glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT,
                GL_TEXTURE_2D, fboTextureID, 0);

        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
        glBindTexture(GL_TEXTURE_2D, 0);

        while (!Display.isCloseRequested()) {
            renderFBO();

            glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glEnable(GL_TEXTURE_2D);
            glBindTexture(GL_TEXTURE_2D, fboTextureID);
            glBegin(GL_QUADS);

            glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

            int x = 0;
            int y = 0; //FBO position
            int w = fboWidth;
            int h = fboHeight; //FBO size

            glTexCoord2f(0, 1);
            glVertex2f(x, y);
            glTexCoord2f(1, 1);
            glVertex2f(x + w, y);
            glTexCoord2f(1, 0);
            glVertex2f(x + w, y + h);
            glTexCoord2f(0, 0);
            glVertex2f(x, y + h);

            glEnd();
            glBindTexture(GL_TEXTURE_2D, 0);

            glDisable(GL_TEXTURE_2D);
            Display.update(); //Update the screen
            Display.sync(60); //Cap the framerate to 60fps
        }
    }

    private static void renderFBO() {
        glClearColor(1.0f, 1.0f, 1.0f, 0.0f);

        //Start FBO drawing
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fboID);
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        //Here I draw some textures using quads
        //Stop FBO drawing
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    static void render() {
        /*EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, framebufferID);
         GL11.glPushAttrib(GL11.GL_VIEWPORT_BIT);
         GL11.glViewport(0, 0, width, height);
         GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

         GL11.glTranslatef(0, 0, -6f);
         GL11.glBegin(GL11.GL_QUADS);
         GL11.glVertex3f(0, 0, 0);
         GL11.glVertex3f(2, 0, 0);
         GL11.glVertex3f(2, 2, 0);
         GL11.glVertex3f(0, 2, 0);
         GL11.glEnd();

         EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
         GL11.glPopAttrib();
         GL11.glDrawBuffer(GL11.GL_NONE);*/
        /*GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
         GL11.glClear(GL11.GL_COLOR);
         GL11.glColor3f(0.5f, 0.5f, 1.0f);*/
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex2f(0, 0);
        GL11.glTexCoord2f(1, 0);
        GL11.glVertex2f(200, 0);
        GL11.glTexCoord2f(1, 1);
        GL11.glVertex2f(200, 200);
        GL11.glTexCoord2f(0, 1);
        GL11.glVertex2f(0, 200);
        /*GL11.glVertex2f(100, 100);
         GL11.glVertex2f(100 + 100, 100 + 100);
         GL11.glVertex2f(100 - 100, 100 + 100);*/
        GL11.glEnd();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
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
        GL11.glClear(GL11.GL_COLOR);

        // Включить Alpha blending
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glViewport(0, 0, width, height);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, width, height, 0, 1, -1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        int textID = GL11.glGenTextures();
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);				// make it linear filterd
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 200, 200, 0, GL_RGBA, GL_INT, (java.nio.ByteBuffer) null);	// Create the texture data
        framebufferID = EXTFramebufferObject.glGenFramebuffersEXT();
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, framebufferID);
        glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D, colorTextureID, 0);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // устанавливаем цвет quad. 
        GL11.glViewport(0, 0, 200, 200);

        GL11.glColor3f(1.0f, 0.5f, 1.0f);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(50, 50);
        GL11.glVertex2f(150, 50);
        GL11.glVertex2f(150, 150);
        GL11.glVertex2f(50, 150);
        //GL11.glVertex2f(100, 100 + 200);
        GL11.glEnd();

        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
        glBindTexture(GL11.GL_TEXTURE_2D, textID);
        GL11.glViewport(0, 0, width, height);
        /*EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, framebufferID);
         GL11.glPushAttrib(GL11.GL_VIEWPORT_BIT);
         GL11.glViewport(0, 0, width, height);
         GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

         GL11.glTranslatef(0, 0, -6f);
         GL11.glBegin(GL11.GL_QUADS);
         GL11.glVertex3f(0, 0, 0);
         GL11.glVertex3f(2, 0, 0);
         GL11.glVertex3f(2, 2, 0);
         GL11.glVertex3f(0, 2, 0);
         GL11.glEnd();

         EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
         GL11.glPopAttrib();*/
    }
}

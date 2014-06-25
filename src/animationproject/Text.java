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
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import static org.lwjgl.opengl.GL11.glClear;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureImpl;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

/**
 *
 * @author ivko0314
 */
public class Text {

    static int width = 800;
    static int height = 600;
    static String title = "test";

    static Texture texture = null;
    private static int framebufferID;
    static int textureId;

    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        init();
        loadTexture();
//EXTFramebufferObject.glF
        boolean done = false;
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

        final int w = 64 * 5;
        final int h = 64 * 5;

        textureId = createTextureRGBA8(w, h, true);
        framebufferID = EXTFramebufferObject.glGenFramebuffersEXT();
        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, framebufferID);
        EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
                EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT,
                GL11.GL_TEXTURE_2D, textureId, 0);
        //--------
        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);

        while (!done) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

            fboRender();
            Display.update();
            Display.sync(100);

            if (Display.isCloseRequested()) {
                done = true;
            }
            
            //Thread.sleep(50);
        }
        Display.destroy();
        freeFBO();
        
        /*//framebufferID = glGenFramebuffersEXT();
         Texture levelText = new TextureImpl("asd", GL11.GL_TEXTURE_2D, 123);
         GL11.glEnable(GL11.GL_TEXTURE_2D); 
         GL11.glBindTexture(GL11.GL_TEXTURE_2D, levelText.getTextureID());
         int i=0;
         int j=0;
         for (int[] col: level.grounds) {
         for (int row : col) {
         Ground ground =gr[row];
         drawGround(ground, j*64, i*64);
         j++;
         }
         i++;
         j=0;
         }
         GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0); 
         GL11.glDisable(GL11.GL_TEXTURE_2D);
         // Основной цикл.
         while (!done) {
         glClear(GL_COLOR_BUFFER_BIT);
            
         /*for (Ground g : gr) {
         drawGround(g, (int) (Math.random()*width), (int) (Math.random()*height));
         }*/
        //////////////
        /*drawImage(levelText, 0, 0);
         /////////////
         Display.update();
         Display.sync(100);

         if (Display.isCloseRequested()) {
         done = true;
         }
         //Thread.sleep(50);
         }

         Display.destroy();*/
    }

    static void freeFBO() {
        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL11.glDeleteTextures(textureId);
        EXTFramebufferObject.glDeleteFramebuffersEXT(framebufferID);
    }

    static void fboRender() {
        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, framebufferID);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        drawCube();
        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        EXTFramebufferObject.glGenerateMipmapEXT(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        drawScreenQuad(textureId);
    }

    static void drawScreenQuad(int tex) {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        GL11.glOrtho(0, 1, 0, 1, -1, 1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glDisable(GL11.GL_LIGHTING);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex2f(0, 0);
        GL11.glTexCoord2f(1, 0);
        GL11.glVertex2f(1, 0);
        GL11.glTexCoord2f(1, 1);
        GL11.glVertex2f(1, 1);
        GL11.glTexCoord2f(0, 1);
        GL11.glVertex2f(0, 1);
        GL11.glEnd();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_LIGHTING);
    }

    static void drawCube() {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4f(1, 1, 1, 1);

        // face v0-v1-v2-v3
        GL11.glNormal3f(0, 0, 1);
        GL11.glTexCoord2f(1, 1);
        GL11.glVertex3f(1, 1, 1);
        GL11.glTexCoord2f(0, 1);
        GL11.glVertex3f(-1, 1, 1);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex3f(-1, -1, 1);
        GL11.glTexCoord2f(1, 0);
        GL11.glVertex3f(1, -1, 1);

        // face v0-v3-v4-v5
        GL11.glNormal3f(1, 0, 0);
        GL11.glTexCoord2f(0, 1);
        GL11.glVertex3f(1, 1, 1);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex3f(1, -1, 1);
        GL11.glTexCoord2f(1, 0);
        GL11.glVertex3f(1, -1, -1);
        GL11.glTexCoord2f(1, 1);
        GL11.glVertex3f(1, 1, -1);

        // face v0-v5-v6-v1
        GL11.glNormal3f(0, 1, 0);
        GL11.glTexCoord2f(1, 0);
        GL11.glVertex3f(1, 1, 1);
        GL11.glTexCoord2f(1, 1);
        GL11.glVertex3f(1, 1, -1);
        GL11.glTexCoord2f(0, 1);
        GL11.glVertex3f(-1, 1, -1);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex3f(-1, 1, 1);

        // face  v1-v6-v7-v2
        GL11.glNormal3f(-1, 0, 0);
        GL11.glTexCoord2f(1, 1);
        GL11.glVertex3f(-1, 1, 1);
        GL11.glTexCoord2f(0, 1);
        GL11.glVertex3f(-1, 1, -1);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex3f(-1, -1, -1);
        GL11.glTexCoord2f(1, 0);
        GL11.glVertex3f(-1, -1, 1);

        // face v7-v4-v3-v2
        GL11.glNormal3f(0, -1, 0);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex3f(-1, -1, -1);
        GL11.glTexCoord2f(1, 0);
        GL11.glVertex3f(1, -1, -1);
        GL11.glTexCoord2f(1, 1);
        GL11.glVertex3f(1, -1, 1);
        GL11.glTexCoord2f(0, 1);
        GL11.glVertex3f(-1, -1, 1);

        // face v4-v7-v6-v5
        GL11.glNormal3f(0, 0, -1);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex3f(1, -1, -1);
        GL11.glTexCoord2f(1, 0);
        GL11.glVertex3f(-1, -1, -1);
        GL11.glTexCoord2f(1, 1);
        GL11.glVertex3f(-1, 1, -1);
        GL11.glTexCoord2f(0, 1);
        GL11.glVertex3f(1, 1, -1);
        GL11.glEnd();
    }

    static int createTextureRGBA8(int width, int height, boolean genMipmap) {
        int result = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, result);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        if (genMipmap) {
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
        } else {
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_GENERATE_MIPMAP, GL11.GL_FALSE);
        }
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        return result;
    }

    static void drawGround(Ground ground, float x, float y) {
        //GL11.glEnable(GL11.GL_TEXTURE_2D);
        //GL11.glPushMatrix();

        //texture.bind();
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
        //GL11.glDisable(GL11.GL_QUADS);
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

    public static void drawImage(Texture textture, float x, float y) {
        int textureWidth = textture.getTextureWidth();
        int textureHeight = textture.getTextureHeight();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        //GL11.glPushMatrix();

        textture.bind();
        //GL11.glBindTexture(GL11.GL_TEXTURE_2D, TextureMap.getTextureID());

        final int a = 1;
        GL11.glBegin(GL11.GL_QUADS);
        {
            GL11.glTexCoord2f(0, 0);
            GL11.glVertex2f(x, y);
            GL11.glTexCoord2f(1, 0);
            GL11.glVertex2f(x + textureWidth / a, y);
            GL11.glTexCoord2f(1, 1);
            GL11.glVertex2f(x + textureWidth / a, y + textureHeight / a);
            GL11.glTexCoord2f(0, 1);
            GL11.glVertex2f(x, y + textureHeight / a);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_QUADS);

        //GL11.glPopMatrix();
    }
}

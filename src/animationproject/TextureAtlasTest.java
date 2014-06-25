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
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

/**
 *
 * @author ivko0314
 */
public class TextureAtlasTest {

    static int width = 800;
    static int height = 600;
    static String title = "test";

    static Texture texture = null;

    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        init();
        loadTexture();

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

        // Основной цикл.
        while (!done) {
            glClear(GL_COLOR_BUFFER_BIT);
            
            /*for (Ground g : gr) {
                drawGround(g, (int) (Math.random()*width), (int) (Math.random()*height));
            }*/
            
            
            //////////////
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
            /////////////
            Display.update();
            Display.sync(100);

            if (Display.isCloseRequested()) {
                done = true;
            }
            //Thread.sleep(50);
        }

        Display.destroy();

    }

    static void drawGround(Ground ground, float x, float y) {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        //GL11.glPushMatrix();
/*x+=100;
y+=100;*/
        texture.bind();
GL11.glViewport(0, 0, width, height);
        final int a = texture.getImageHeight()/64;
        final float varX = 1f/a;
        final float varY = 1f/a;

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

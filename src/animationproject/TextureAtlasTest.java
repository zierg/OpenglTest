/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package animationproject;

import static animationproject.FBOExample.getTime;
import static animationproject.FBOExample.lastFPS;
import static animationproject.FBOExample.updateFPS;
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
 * Класс рисует уровень заново на каждой итерации цикла, поэтому тормозит.
 * @author ivko0314
 */
public class TextureAtlasTest {

    // Настройки для окна
    static int width = 1000;
    static int height = 1000;
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
        
        // Читаем файл с описаниями участков земли
        Ground[] gr = new Ground[0];
        xstream.alias("Grounds", gr.getClass());
        Reader reader = new FileReader("Grounds.xml");
        gr = (Ground[]) xstream.fromXML(reader);
        short[] x = new short[0];
        
        // Читаем файл с уровнем
        xstream.alias("row", x.getClass());
        reader = new FileReader("level.xml");
        Level level = (Level) xstream.fromXML(reader);
        lastFPS = getTime();
        // Основной цикл.
        while (!done) {
            glClear(GL_COLOR_BUFFER_BIT);

            int i = 0;
            int j = 0;
            for (short[] col : level.grounds) {
                for (short row : col) {
                    Ground ground = gr[row];
                    drawGround(ground, j * 64, i * 64);
                    j++;
                }
                i++;
                j = 0;
            }
            updateFPS();
            Display.update();
            Display.sync(100);

            if (Display.isCloseRequested()) {
                done = true;
            }
        }

        Display.destroy();

    }

    /**
     * Рисует участок земли
     * @param ground участок
     * @param x координата x
     * @param y координата y
     */
    static void drawGround(Ground ground, float x, float y) {
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        texture.bind(); // Закрепляем текстуру, которую будем рисовать текстуру
        final int a = texture.getImageHeight() / 64; // 64 - размер одного участка земли, переменная a - количество участков земли по вериткали (в данном случае и по горизонтали)
        final float varX = 1f / a; // ширина участка земли относительно ширины текстуры
        final float varY = 1f / a; // высота участка земли относительно ширины текстуры

        // Размер текстуры
        int textureWidth = texture.getTextureWidth();
        int textureHeight = texture.getTextureHeight();

        // Координаты строки и столбца текстуры, в которых находится нужный участок земли 
        float row = varX * (ground.col - 1);
        float col = varY * (ground.row - 1);

        // Рисуем участок
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Инициализация OpenGL
     */
    static void init() {
        try {
            DisplayMode displayMode = new DisplayMode(width, height);
            Display.setDisplayMode(displayMode);
            Display.setTitle(title);
            Display.create();
            Display.setVSyncEnabled(false);
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

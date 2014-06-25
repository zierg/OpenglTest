package animationproject;

/**
 *
 * @author Michael
 */
import java.io.IOException;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.*;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

public class GraphicsHelper {

    /**
     * Конструктор.
     *
     * @param width Ширина окна.
     * @param height Высота окна.
     * @param title Заголовок окна.
     */
    public GraphicsHelper(int width, int height, String title) {
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

    /**
     * Загружает изображение в формате PNG из файла
     *
     * @param fileName Имя файла
     * @return объект типа Texture
     */
    public Texture loadImage(String fileName) {
        Texture texture = null;

        try {
            // Загрузка текстуры из файла PNG
            texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream(fileName));

            System.out.println("Texture loaded: " + texture);
            System.out.println(">> Image width: " + texture.getImageWidth());
            System.out.println(">> Image height: " + texture.getImageHeight());
            System.out.println(">> Texture width: " + texture.getTextureWidth());
            System.out.println(">> Texture height: " + texture.getTextureHeight());
            System.out.println(">> Texture ID: " + texture.getTextureID());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return texture;
    }

    /**
     * Выводит изображение на экран.
     *
     * @param texture Ссылка на текстуру.
     * @param x Координата X изображения.
     * @param y Координата Y.
     */
    public void drawImage(Texture texture, float x, float y) {
        int textureWidth = texture.getTextureWidth();
        int textureHeight = texture.getTextureHeight();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        //GL11.glPushMatrix();

        texture.bind();
        //GL11.glBindTexture(GL11.GL_TEXTURE_2D, TextureMap.getTextureID());

        final int a = 1;
        GL11.glBegin(GL11.GL_QUADS);
        {
            GL11.glTexCoord2f(0, 0);
            GL11.glVertex2f(x, y);
            GL11.glTexCoord2f(1, 0);
            GL11.glVertex2f(x + textureWidth/a, y);
            GL11.glTexCoord2f(1, 1);
            GL11.glVertex2f(x + textureWidth/a, y + textureHeight/a);
            GL11.glTexCoord2f(0, 1);
            GL11.glVertex2f(x, y + textureHeight/a);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_QUADS);

        //GL11.glPopMatrix();
    }

    /**
     * Выводит и поворачивает изображение на экране.
     *
     * @param texture Ссылка на текстуру.
     * @param x Координата X изображения.
     * @param y Координата Y.
     * @param angle Угол поворота в радианах.
     */
    public void drawImage(Texture texture, float x, float y, float angle) {
        GL11.glPushMatrix();

        float viewPointX = texture.getImageWidth() / 2 + x;
        float viewPointY = texture.getImageHeight() / 2 + y;

        GL11.glTranslatef(viewPointX, viewPointY, 0);
        GL11.glRotatef(angle, 0, 0, 1);
        GL11.glTranslatef(-viewPointX, -viewPointY, 0);

        drawImage(texture, x, y);

        GL11.glPopMatrix();
    }

    /*public void drawLine(float startX, float startY, float endX, float endY)
     {
     //Color.green.bind();
     glDisable(GL_TEXTURE_2D);
     glPushMatrix();
        
     glBegin(GL_LINES);
     {
     glLineWidth(2.5f);
     glColor3f(1.0f, 0.0f, 0.0f);
     glVertex2f(startX, startY);
     glVertex2f(endX, endY);
     }
     glEnd();
     glPopMatrix();
     }*/
}

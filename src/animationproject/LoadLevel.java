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
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL11.*;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

/**
 * Класс рисует уровень один раз на текстуру-буфер, затем на каждой итерации
 * цикла отрисовывает это текстуру, поэтому не тормозит.
 *
 * @author ivko0314
 */
public class LoadLevel {

    private static boolean done = false;

    /**
     * Поток, прослушивающий события и управляющий игрой независимо от
     * обновления изображения и FPS. Таким образом, скорость игры не меняется
     * при низком/высоком FPS.
     */
    private static class GameHandler extends Thread {
        // Интервал в миллисекундах, через который нужно обновлять игру

        private static final int INTERVAL = 10;
        // Последнее время обновления игры
        private long lastTime = getTime();

        public GameHandler() {
            start();
        }

        @Override
        public void run() {
            while (!done) {
                long time = getTime();
                if (time - lastTime >= INTERVAL) {
                    listenMouse();
                    lastTime = time;
                }
            }
        }

        /**
         * Получение точного времени в миллисекундах
         *
         * @return
         */
        private long getTime() {
            return (Sys.getTime() * 1000) / Sys.getTimerResolution();
        }
    }
    //-------------------------------------------------------------------------------------------
    // Скорость перемещения по уровню при помощи мыши по вертикали/горизонтали
    private static final int VERTICAL_MOUSE_SPEED = 10;
    private static final int HORIZONTAL_MOUSE_SPEED = 10;
    private static final int MOUSE_OFFSET = 100; // Максимальное смещение "камеры" при достижении края уровня
    private static boolean isLeftButtonPressed = false;
    // Размеры окна
    private final static int width = 400;
    private final static int height = 400;
    private static Texture groundsTexture; // Текстура с участками земли
    private static Ground[] grounds; // Массив участков земли
    private static Level level = null; // Уровень.
    private static int levelTextureID; // ID для текстуры, куда будет рисоваться уровень.
    private static int framebufferID; // ID буфера, к которому прикрепится текстура уровня.
    // Размеры уровня (в пикселях)
    private static int levelWidth;
    private static int levelHeight;
    private static float levelXPosition;
    private static float levelYPosition;

    public static void main(String[] args) throws IOException {
        //LevelCreator.main(args); // Генерируем случайный уровень.
        initOpenGL();
        loadGroundTexture();
        loadAndDrawLevel();
        lastFPS = getTime();
        new GameHandler();
        while (!done) {
            render();
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
     * Метод для прослушивания событий мыши
     */
    private static void listenMouse() {
        int x = Mouse.getX();
        int y = Mouse.getY();

        if (x <= 0 && levelXPosition < MOUSE_OFFSET) {
            levelXPosition += HORIZONTAL_MOUSE_SPEED;
        } else if (x >= width - 1 && levelXPosition > width - levelWidth - MOUSE_OFFSET) {
            levelXPosition -= HORIZONTAL_MOUSE_SPEED;
        }

        if (y <= 0 && levelYPosition > height - levelHeight - MOUSE_OFFSET) {
            levelYPosition -= VERTICAL_MOUSE_SPEED;
        } else if (y >= height - 1 && levelYPosition < MOUSE_OFFSET) {
            levelYPosition += VERTICAL_MOUSE_SPEED;
        }

        if (Mouse.isButtonDown(0)) {
            if (!isLeftButtonPressed) {
                System.out.println("x = " + x + ", y = " + y); // Вывод координат
                isLeftButtonPressed = true;

                // Вывод столбца и строки нажатой ячейки (только в том случае, когда клик был внутри уровня)
                if (height - y >= levelYPosition && height - y <= levelYPosition + levelHeight
                        && x >= levelXPosition && x <= levelXPosition + levelWidth) {
                    System.out.println("Нажатая ячейка:");
                    int row = (int) (height - y - levelYPosition) / 64;
                    int col = (int) (x - levelXPosition) / 64;
                    System.out.println("row = " + row);
                    System.out.println("col = " + col);
                    System.out.println("Color = " + grounds[level.grounds[row][col]].name);
                }
            }
        } else {
            isLeftButtonPressed = false;
        }
    }

    /**
     * Инициализация OpenGL
     *
     * @throws IOException
     */
    private static void initOpenGL() throws IOException {
        try {
            DisplayMode displayMode = new DisplayMode(width, height);
            Display.setDisplayMode(displayMode);
            Display.setTitle("LoadLevel");
            Display.create();
            Display.setVSyncEnabled(false);
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

        Mouse.setGrabbed(true); // Захватываем мышь.

    }

    /**
     * Загрузка текстуры участков земли, загрузка массива участков земли
     *
     * @throws IOException
     */
    private static void loadGroundTexture() throws IOException {
        groundsTexture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("resources\\grounds.png"));
        XStream xstream = new XStream(new PureJavaReflectionProvider(), new Dom4JDriver());
        // new PureJavaReflectionProvider() - будет использоваться конструктор по умолчанию, чтобы отсутствующие в xml поля не были null
        xstream.processAnnotations(Ground.class);
        Ground[] gr = new Ground[0];
        xstream.alias("Grounds", gr.getClass());
        Reader reader = new FileReader("Grounds.xml");
        grounds = (Ground[]) xstream.fromXML(reader);
        for (Ground g : grounds) {
            System.out.println("id = " + g.id + ", color = " + g.name);
        }
    }

    /**
     * Загрузка уровня и рисование его на текстуре levelTextureID
     *
     * @throws FileNotFoundException
     */
    private static void loadAndDrawLevel() throws FileNotFoundException {
        // Загрузка уровня
        XStream xstream = new XStream(new PureJavaReflectionProvider(), new Dom4JDriver());
        // new PureJavaReflectionProvider() - будет использоваться конструктор по умолчанию, чтобы отсутствующие в xml поля не были null
        xstream.processAnnotations(Level.class);
        int[] x = new int[0];
        xstream.alias("row", x.getClass());
        Reader reader = new FileReader("level.xml");
        level = (Level) xstream.fromXML(reader);
        

        // Рисование уровня
        framebufferID = glGenFramebuffersEXT();
        levelTextureID = glGenTextures();												// and a new texture used as a color buffer
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, framebufferID); 						// switch to the new framebuffer

        final int GROUND_SIZE = 64; // Размер участка земли
        levelWidth = level.grounds.length * GROUND_SIZE;
        levelHeight = level.grounds.length * GROUND_SIZE;
        // Координаты задаём так, чтобы в середине окна был центр уровня
        levelXPosition = width / 2 - levelWidth / 2;
        levelYPosition = height / 2 - levelHeight / 2;

// initialize color texture
        glBindTexture(GL_TEXTURE_2D, levelTextureID);									// Bind the colorbuffer texture
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);				// make it linear filterd
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, levelWidth, levelHeight, 0, GL_RGBA, GL_INT, (java.nio.ByteBuffer) null);	// Create the texture data
        glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D, levelTextureID, 0); // attach it to the framebuffer

        glViewport(0, 0, levelWidth, levelHeight);
        glClearColor(0.5f, 0.5f, 0.5f, 1.0f); // Фоновый серый цвет. Для теста, полностью ли текстура заполняется уровнем (если нет, будет виден фон)
        glClear(GL_COLOR_BUFFER_BIT);			// Clear Screen And Depth Buffer on the fbo to red
        glLoadIdentity();

        glEnable(GL_TEXTURE_2D);
        glColor3f(1, 1, 1);

        glBindTexture(GL_TEXTURE_2D, groundsTexture.getTextureID()); // Закрепляем текстуру с участками земли
        glViewport(0, 0, levelWidth, levelHeight); // Переключаемся на размеры уровня (иначе всё, что по координатам не входит в размер окна, не отрисуется на текстуре)

        // Устанавливаем масштаб, чтобы участки рисовались в полный размер.
        float w = levelWidth;
        float www = width;
        float h = levelHeight;
        float hhh = height;
        glScalef(www / w, hhh / h, 1.0f);

        // Отрисовка уровня
        int i = 0;
        int j = 0;
        for (int[] col : level.grounds) {
            for (int row : col) {
                Ground ground = grounds[row];
                drawGround(ground, j * GROUND_SIZE, i * GROUND_SIZE);
                j++;
            }
            i++;
            j = 0;
        }
        glScalef(1.0f, 1.0f, 1.0f); // Меняем масштаб обратно

        // Отключаем рисование, переключаемся с буфера обратно
        glDisable(GL_QUADS);
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
        glViewport(0, 0, width, height);
    }

    /**
     * Рисование участка земли
     *
     * @param ground участок
     * @param x координата x
     * @param y координата y
     */
    private static void drawGround(Ground ground, float x, float y) {
        final int a = groundsTexture.getImageHeight() / 64; // 64 - размер одного участка земли, переменная a - количество участков земли по вериткали (в данном случае и по горизонтали)
        final float varX = 1f / a; // ширина участка земли относительно ширины текстуры
        final float varY = 1f / a; // высота участка земли относительно высоты текстуры

        // Размер текстуры
        int textureWidth = groundsTexture.getTextureWidth();
        int textureHeight = groundsTexture.getTextureHeight();

        // Координаты строки и столбца текстуры, в которых находится нужный участок земли 
        float row = varX * (ground.col);
        float col = varY * (ground.row);

        // Ширина и высота участка земли в пикселях
        float ww = textureWidth / a;
        float hh = textureHeight / a;

        // Смещение по вертикали в пикселях (т.к. отсчёт y начинается снизу, а рисовать нужно сверху, то происходит глюк)
        final int yOffset = height - levelHeight;

        // Рисуем участок
        glBegin(GL_QUADS);
        {
            glTexCoord2f(row, col);
            glVertex2f(x, height - y - yOffset);
            glTexCoord2f(row + varX, col);
            glVertex2f((x + ww), height - y - yOffset);
            glTexCoord2f(row + varX, col + varY);
            glVertex2f((x + ww), height - (y + hh) - yOffset);
            glTexCoord2f(row, col + varY);
            glVertex2f(x, height - (y + hh) - yOffset);
        }
        glEnd();
    }

    /**
     * Отрисовка созданной ранее текстуры с уровнем.
     */
    private static void render() {
        glEnable(GL_TEXTURE_2D);

        glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
        glClear(GL_COLOR_BUFFER_BIT);			// Clear Screen And Depth Buffer on the framebuffer to black

        glBindTexture(GL_TEXTURE_2D, levelTextureID);	// переключаемся на созданную ранее текстуру уровня				// bind our FBO texture

        glLoadIdentity();												// Reset The Modelview Matrix

        glColor3f(1, 1, 1);												// set the color to white

        // Рисуем уровень
        glBegin(GL_QUADS);
        {
            glTexCoord2f(0, 0);
            glVertex2f(levelXPosition, levelYPosition);
            glTexCoord2f(1, 0);
            glVertex2f(levelXPosition + levelWidth, levelYPosition);
            glTexCoord2f(1, 1);
            glVertex2f(levelXPosition + levelWidth, levelYPosition + levelHeight);
            glTexCoord2f(0, 1);
            glVertex2f(levelXPosition, levelYPosition + levelHeight);
        }
        glEnd();
        glBindTexture(GL_TEXTURE_2D, 0);
        drawMouse();
        glDisable(GL_TEXTURE_2D);
        glFlush();
    }

    private static void drawMouse() {
        glColor3f(1, 1, 1);
        final int size = 10;// set the color to white
        int x = Mouse.getX();
        int y = Mouse.getY() - size;
        // Рисуем уровень

        glBegin(GL_QUADS);
        {
            //glTexCoord2f(0, 0);
            glVertex2f(x, height - y);
            //glTexCoord2f(1, 0);
            glVertex2f(x + size, height - y);
            //glTexCoord2f(1, 1);
            glVertex2f(x + size, height - (y + size));
            //glTexCoord2f(0, 1);
            glVertex2f(x, height - (y + size));
        }
        glEnd();
    }
}

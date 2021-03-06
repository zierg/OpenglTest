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
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL11.*;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;
import org.newdawn.slick.openal.Audio;
import org.newdawn.slick.openal.AudioLoader;
import org.newdawn.slick.openal.SoundStore;

/**
 * Класс рисует уровень один раз на текстуру-буфер, затем на каждой итерации
 * цикла отрисовывает это текстуру, поэтому не тормозит.
 *
 * @author ivko0314
 */
public class LoadLevel1 {

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
                    listenKeyboard();
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
    // Окна
    /*private static final List<Window> windowList = new ArrayList<>();
    private static final int BAR_HEIGHT = 20; // Высота заголовка
    private static int windowXPosBeforePress;
    private static int windowYPosBeforePress;
    private static Window draggingWindow = null; // Перетаскиваемое окно*/
        
    private static final int GROUND_SIZE = 32; // Размер участка земли
    private static final int FILE_GROUND_SIZE = 64; // Размер участка земли в файле grounds.png

    // Звуки
    /*private static Audio oggStream; // Канал ogg-файла
    private static Audio wavEffect; // Канал wav-файла
    private static Audio groundSound; // Канал wav-файла

    private static UnicodeFont popupFont;  // Шрифт для всплывающего окна
    private static UnicodeFont otherFont;  // Другой шрифт*/
    private static final int FPS = 100;  // Количество кадров в секунду
    // Скорость перемещения по уровню при помощи мыши по вертикали/горизонтали
    /*private static final int VERTICAL_MOUSE_SPEED = 20;
    private static final int HORIZONTAL_MOUSE_SPEED = 20;
    private static final int MOUSE_OFFSET = 100; // Максимальное смещение "камеры" при достижении края уровня
    private static boolean isLeftButtonPressed = false;
    private static boolean isRightButtonPressed = false;
    private static int mouseXBeforePress;
    private static int mouseYBeforePress;*/
    // Размеры окна
    /*private final static int width = Display.getDesktopDisplayMode().getWidth();
     private final static int height = Display.getDesktopDisplayMode().getHeight();*/
    private final static int width = 800;
    private final static int height = 600;
    private static final boolean fullscreen = false;

    private static Texture groundsTexture; // Текстура с участками земли
    private static Ground[] grounds; // Массив участков земли
    //private static Level level = null; // Уровень.
    private static int levelTextureID; // ID для текстуры, куда будет рисоваться уровень.
    private static int framebufferID; // ID буфера, к которому прикрепится текстура уровня.
    // Размеры уровня (в пикселях)
    private static int levelWidth;
    private static int levelHeight;
    private static float levelXPosition;
    private static float levelYPosition;

    public static void main(String[] args) throws IOException {
        //LevelCreator.main(args); // Генерируем случайный уровень.
        /*Window window = new Window();
        window.x = 20;
        window.y = 20;
        windowList.add(window);
        window = new Window(300,50);
        window.x = 300;
        window.y = 500;
        windowList.add(window);*/
        initOpenGL();
        initFonts();
        initSound();
        loadGroundTexture();
        loadAndDrawLevel();
        lastFPS = getTime();
        new GameHandler();
        while (!done) {
            render();
            SoundStore.get().poll(0);
            updateFPS();
            Display.update();
            Display.sync(FPS);

            if (Display.isCloseRequested()) {
                done = true;
            }
        }

        AL.destroy();
        Display.destroy();
    }

    /**
     * Метод для прослушивания событий клавиатуры (enter, пробел - звуки, escape
     * - выход)
     */
    private static void listenKeyboard() {
        /*while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                if (Keyboard.getEventKey() == Keyboard.KEY_RETURN) {
                    if (oggStream.isPlaying()) {
                        oggStream.stop();
                        SoundStore.get().stopSoundEffect(SoundStore.get().getSource(0)); // Без этого останавливает не мгновенно
                    } else {
                        oggStream.playAsMusic(1.0f, 0.5f, false);
                        AL10.alSourcef(SoundStore.get().getSource(0), AL10.AL_GAIN, 0.5f); // 0.5f - громкость
                    }
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
                    wavEffect.playAsSoundEffect(1.0f, 0.3f, false, 2.0f, 0.0f, 0.5f);
                    AL10.alSourcef(SoundStore.get().getSource(1), AL10.AL_GAIN, 0.3f); // 0.3f - громкость
                    FloatBuffer sourcePos1 = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[]{1.0f, 0.0f, 0.5f}).rewind();
                    AL10.alSource(SoundStore.get().getSource(1), AL10.AL_POSITION, sourcePos1);
                }
                if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
                    done = true;
                }
            }
        }*/
    }

    private static void initSound() throws IOException {
        /*oggStream = AudioLoader.getStreamingAudio("OGG", ResourceLoader.getResource("resources/09 Blackheart.ogg"));
        wavEffect = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("resources/EndOfLevel.wav"));
        groundSound = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("ping.wav"));
*/
    }

    /**
     * Загрузка шрифтов
     */
    private static void initFonts() {
        /*java.awt.Font awtFont = new java.awt.Font("Times New Roman", java.awt.Font.BOLD, 43);
        popupFont = new UnicodeFont(awtFont);
        popupFont.addGlyphs("АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя");
        popupFont.getEffects().add(new ColorEffect(java.awt.Color.black));
        popupFont.addAsciiGlyphs();
        try {
            popupFont.loadGlyphs();
        } catch (SlickException e) {
            e.printStackTrace();
            //cleanUp();
        }

        java.awt.Font awtFont2 = new java.awt.Font("Times New Roman", java.awt.Font.BOLD, 14);
        otherFont = new UnicodeFont(awtFont2);
        otherFont.addGlyphs("АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя");
        otherFont.getEffects().add(new ColorEffect(java.awt.Color.red));
        otherFont.addAsciiGlyphs();
        try {
            otherFont.loadGlyphs();
        } catch (SlickException e) {
            e.printStackTrace();
            //cleanUp();
        }*/
    }

    /**
     * Метод для прослушивания событий мыши
     */
    private static void listenMouse() {
        /*int x = Mouse.getX();
        int y = Mouse.getY();
        boolean insideLevel = isPointInsideLevel(x, y);
        if (insideLevel) {
            // ПКМ
            if (Mouse.isButtonDown(1)) {
                if (!isRightButtonPressed) {
                    mouseXBeforePress = x;
                    mouseYBeforePress = y;
                    isRightButtonPressed = true;
                }
                return;
            } else {
                isRightButtonPressed = false;
            }

            // ЛКМ
            Window window = null;
            if (Mouse.isButtonDown(0)) {
                if (draggingWindow != null) {
                    draggingWindow.x = windowXPosBeforePress + (x - mouseXBeforePress);
                    draggingWindow.y = windowYPosBeforePress - (y - mouseYBeforePress);
                } else {
                    for (Window w : windowList) {
                        if (isCursorOnWindow(w, x, y)) {
                            window = w;
                        }
                    }
                    if (window == null) {
                        if (!isLeftButtonPressed) {
                            System.out.println("x = " + x + ", y = " + y); // Вывод координат

                            // Проигрывание звука
                            final float SCALE = 2; // Домножается на позицию источника звука. Чем больше, тем сильнее смещается звук.
                            float xSound = ((x - (float) width / 2) / ((float) width / 2)) * SCALE; // Определяем позицию по горизонтали относительно центра.
                            float ySound = ((y - (float) height / 2) / ((float) height / 2)) * SCALE; // Определяем позицию по вертикали относительно центра.
                            final float zSound = 0.5f; // по оси Oz
                            groundSound.playAsSoundEffect(1.0f, 0.3f, false, xSound, ySound, zSound); // Проигрываем звук. Параметры: высота (и скорость), громкость, зацикленность, позиция источника звука
                            AL10.alSourcef(SoundStore.get().getSource(2), AL10.AL_GAIN, 0.3f); // 0.3f - громкость. без этой строки не работает

                            isLeftButtonPressed = true;

                            // Вывод столбца и строки нажатой ячейки (только в том случае, когда клик был внутри уровня)
                            System.out.println("Нажатая ячейка:");
                            System.out.println("Color = " + getGroundUnderCursor(x, y).name);
                        }
                    } else {
                        if (!isLeftButtonPressed && isCursorOnWindowBar(window, x, y)) {
                            draggingWindow = window;
                            mouseXBeforePress = x;
                            mouseYBeforePress = y;
                            isLeftButtonPressed = true;
                            windowXPosBeforePress = window.x;
                            windowYPosBeforePress = window.y;
                        } else {

                        }
                        // обработать нажатие на окно
                    }
                }
            } else {
                isLeftButtonPressed = false;
                draggingWindow = null;
            }
        } else {
            if (!Mouse.isButtonDown(1) && isRightButtonPressed) {
                isRightButtonPressed = false;
            }
        }

        if (isRightButtonPressed || isLeftButtonPressed) {
            return;
        }

        if (x <= 0 && levelXPosition < MOUSE_OFFSET) {
            levelXPosition += HORIZONTAL_MOUSE_SPEED;
        } else if (x >= width - 1 && levelXPosition > width - levelWidth - MOUSE_OFFSET) {
            levelXPosition -= HORIZONTAL_MOUSE_SPEED;
        }

        if (y <= 0 && levelYPosition > height - levelHeight - MOUSE_OFFSET) {
            levelYPosition -= VERTICAL_MOUSE_SPEED;
        } else if (y >= height - 1 && levelYPosition < MOUSE_OFFSET) {
            levelYPosition += VERTICAL_MOUSE_SPEED;
        }*/

    }

    /**
     * Set the display mode to be used
     *
     * @param width The width of the display required
     * @param height The height of the display required
     * @param fullscreen True if we want fullscreen mode
     */
    private static void setDisplayMode(int width, int height, boolean fullscreen) {

        // return if requested DisplayMode is already set
        if ((Display.getDisplayMode().getWidth() == width)
                && (Display.getDisplayMode().getHeight() == height)
                && (Display.isFullscreen() == fullscreen)) {
            return;
        }

        try {
            DisplayMode targetDisplayMode = null;

            if (fullscreen) {
                DisplayMode[] modes = Display.getAvailableDisplayModes();
                int freq = 0;

                for (int i = 0; i < modes.length; i++) {
                    DisplayMode current = modes[i];

                    if ((current.getWidth() == width) && (current.getHeight() == height)) {
                        if ((targetDisplayMode == null) || (current.getFrequency() >= freq)) {
                            if ((targetDisplayMode == null) || (current.getBitsPerPixel() > targetDisplayMode.getBitsPerPixel())) {
                                targetDisplayMode = current;
                                freq = targetDisplayMode.getFrequency();
                            }
                        }

                        // if we've found a match for bpp and frequence against the 
                        // original display mode then it's probably best to go for this one
                        // since it's most likely compatible with the monitor
                        if ((current.getBitsPerPixel() == Display.getDesktopDisplayMode().getBitsPerPixel())
                                && (current.getFrequency() == Display.getDesktopDisplayMode().getFrequency())) {
                            targetDisplayMode = current;
                            break;
                        }
                    }
                }
            } else {
                targetDisplayMode = new DisplayMode(width, height);
            }

            if (targetDisplayMode == null) {
                System.out.println("Failed to find value mode: " + width + "x" + height + " fs=" + fullscreen);
                return;
            }

            Display.setDisplayMode(targetDisplayMode);
            Display.setFullscreen(fullscreen);

        } catch (LWJGLException e) {
            System.out.println("Unable to setup mode " + width + "x" + height + " fullscreen=" + fullscreen + e);
        }
    }

    /**
     * Проверка, находится ли точка внутри уровня
     *
     * @param x
     * @param y
     * @return
     */
    /*private static boolean isPointInsideLevel(float x, float y) {
        /*return (height - y >= levelYPosition && height - y <= levelYPosition + levelHeight
                && x >= levelXPosition && x <= levelXPosition + levelWidth);
    }*/

    /**
     * Инициализация OpenGL
     *
     * @throws IOException
     */
    private static void initOpenGL() throws IOException {
        try {
            //DisplayMode displayMode = new DisplayMode(width, height);
            //Display.setDisplayMode(displayMode);
            setDisplayMode(width, height, fullscreen);
            Display.create();
            //Display.setFullscreen(true);
            //Display.setTitle("LoadLevel");

            Display.setVSyncEnabled(false);
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(0);
        }

        glViewport(0, 0, width, height);								// Reset The Current Viewport
        glMatrixMode(GL_PROJECTION);								// Select The Projection Matrix
        glLoadIdentity();
        glOrtho(0, width, height, 0, 1, -1);// Reset The Projection Matrix
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        //GLU.gluPerspective(45.0f, 512f / 512f, 1.0f, 100.0f);		// Calculate The Aspect Ratio Of The Window	
        glMatrixMode(GL_MODELVIEW);								// Select The Modelview Matrix
        glLoadIdentity();

        //Mouse.setGrabbed(true); // Захватываем мышь.

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
    }

    /**
     * Загрузка уровня и рисование его на текстуре levelTextureID
     *
     * @throws FileNotFoundException
     */
    private static void loadAndDrawLevel() throws FileNotFoundException {
        // Загрузка уровня
        /*XStream xstream = new XStream(new PureJavaReflectionProvider(), new Dom4JDriver());
        // new PureJavaReflectionProvider() - будет использоваться конструктор по умолчанию, чтобы отсутствующие в xml поля не были null
        xstream.processAnnotations(Level.class);
        short[] x = new short[0];
        xstream.alias("row", x.getClass());
        Reader reader = new FileReader("level.xml");
        level = (Level) xstream.fromXML(reader);*/

        // Рисование уровня
        framebufferID = glGenFramebuffersEXT();
        levelTextureID = glGenTextures();												// and a new texture used as a color buffer
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, framebufferID); 						// switch to the new framebuffer

        levelWidth = /*level.grounds.length*/256 * GROUND_SIZE;
        levelHeight = /*level.grounds.length*/256 * GROUND_SIZE;
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
        /*int i = 0;
        int j = 0;*/
        /*for (short[] col : level.grounds) {
            for (short row : col) {
                Ground ground = grounds[row];
                drawGround(ground, j * GROUND_SIZE, i * GROUND_SIZE);
                j++;
            }
            i++;
            j = 0;
        }*/
        for (int i =0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                drawGround(grounds[(int)Math.random()*16], j * GROUND_SIZE, i * GROUND_SIZE);
            }
        }
        glScalef(1.0f, 1.0f, 1.0f); // Меняем масштаб обратно

        // Отключаем рисование, переключаемся с буфера обратно
        glDisable(GL_QUADS);
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
        glViewport(0, 0, width, height);
        //org.lwjgl.opengl.EXTFramebufferObject.glDeleteFramebuffersEXT(framebufferID);
    }

    /**
     * Рисование участка земли
     *
     * @param ground участок
     * @param x координата x
     * @param y координата y
     */
    private static void drawGround(Ground ground, float x, float y) {
        final int a = groundsTexture.getImageHeight() / FILE_GROUND_SIZE;
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
        /*drawWindows();
        glBindTexture(GL_TEXTURE_2D, 0);
        otherFont.drawString(10, 10, "Enter - вкл/выкл музыку,\nпробел - звуковой эффект,\nescape - выход,\nПКМ - всплывающее окно,\nЛКМ - тест позиции источника звука\n(зависит от координат курсора).");
        glBindTexture(GL_TEXTURE_2D, 0);
        if (!isRightButtonPressed) {
            drawMouse();
        } else {
            drawPopupWindow(getGroundUnderCursor(mouseXBeforePress, mouseYBeforePress).name, mouseXBeforePress, mouseYBeforePress);
        }*/
        glDisable(GL_TEXTURE_2D);
        glFlush();

    }

    private static void drawMouse() {
        /*final int size = 10;
        int x = Mouse.getX();
        int y = Mouse.getY() - size;

        glColor3f(0, 0, 0); // сначала чёрный квадрат
        glBegin(GL_QUADS);
        {
            glVertex2f(x, height - y);
            glVertex2f(x + size, height - y);
            glVertex2f(x + size, height - (y + size));
            glVertex2f(x, height - (y + size));
        }
        glEnd();

        // Затем белый поверх него
        glColor3f(1, 1, 1);
        glBegin(GL_QUADS);
        {
            glVertex2f(x + 1, height - y - 1);
            glVertex2f(x + size - 1, height - y - 1);
            glVertex2f(x + size - 1, height - (y + size - 1));
            glVertex2f(x + 1, height - (y + size - 1));
        }
        glEnd();*/
    }

    /**
     * Нарисовать всплывающее окно
     *
     * @param text
     * @param x
     * @param y
     */
    /*private static void drawPopupWindow(String text, float x, float y) {
        final int textPadding = 10;
        final int windowWidth = popupFont.getWidth(text) + textPadding * 2; // Ширина
        final int windowHeight = popupFont.getLineHeight() + textPadding / 3; // высота

        // Если окно выйдет за границу, то перемещаем его на край границы
        float levelRight = levelWidth + levelXPosition;
        if (x + windowWidth > width) {
            x = width - windowWidth;
        }
        if (x + windowWidth > levelRight) {
            x = levelRight - windowWidth;
        }

        // Если окно выйдет за границу, то перемещаем его на край границы
        float levelBottom = height - levelYPosition - levelHeight;
        if (y - windowHeight < 0) {
            y = windowHeight;
        }
        if (y - windowHeight < levelBottom) {
            y = windowHeight + levelBottom;
        }

        glColor3f(0, 0, 0); // сначала чёрный квадрат
        glBegin(GL_QUADS);
        {
            glVertex2f(x, height - y);
            glVertex2f(x + windowWidth, height - y);
            glVertex2f(x + windowWidth, height - (y - windowHeight));
            glVertex2f(x, height - (y - windowHeight));
        }
        glEnd();

        // Затем белый поверх него
        glColor3f(1, 1, 1);
        glBegin(GL_QUADS);
        {
            glVertex2f(x + 1, height - y + 1);
            glVertex2f(x + windowWidth - 1, height - y + 1);
            glVertex2f(x + windowWidth - 1, height - (y - windowHeight + 1));
            glVertex2f(x + 1, height - (y - windowHeight + 1));
        }
        glEnd();

        popupFont.drawString(x + textPadding, height - y, text);
    }*/

    /**
     * Получить участок земли под координатами x и y
     *
     * @param x
     * @param y
     * @return
     */
    /*private static Ground getGroundUnderCursor(int x, int y) {
        int row = (int) (height - y - levelYPosition - 1) / GROUND_SIZE;
        int col = (int) (x - levelXPosition - 1) / GROUND_SIZE;
        return grounds[level.grounds[row][col]];
    }*/

    /**
     * Рисование всех окон
     */
    /*private static void drawWindows() {
        for (Window window : windowList) {
            int x = window.x;
            int y = window.y;
            int windowWidth = window.width;
            int windowHeight = window.height;
            glColor3f(0, 0, 0); // сначала чёрный квадрат
            glBegin(GL_QUADS);
            {
                glVertex2f(x, y);
                glVertex2f(x + windowWidth, y);
                glVertex2f(x + windowWidth, y + windowHeight);
                glVertex2f(x, (y + windowHeight));
            }
            glEnd();

            final int FRAME = 2;
            // Затем белый поверх него
            glColor3f(1, 1, 1);
            glBegin(GL_QUADS);
            {
                glVertex2f(x + FRAME, y + FRAME);
                glVertex2f(x + windowWidth - FRAME, y + FRAME);
                glVertex2f(x + windowWidth - FRAME, (y + windowHeight - FRAME));
                glVertex2f(x + FRAME, (y + windowHeight - FRAME));
            }
            glEnd();

            glColor3f(0, 0, 0); // сначала чёрный квадрат
            glBegin(GL_QUADS);
            {
                glVertex2f(x, y);
                glVertex2f(x + windowWidth, y);
                glVertex2f(x + windowWidth, y + BAR_HEIGHT);
                glVertex2f(x, (y + BAR_HEIGHT));
            }
            glEnd();

            glColor3f(0.5f, 0.5f, 0.5f);
            glBegin(GL_QUADS);
            {
                glVertex2f(x + FRAME, y + FRAME);
                glVertex2f(x + windowWidth - FRAME, y + FRAME);
                glVertex2f(x + windowWidth - FRAME, (y + BAR_HEIGHT - FRAME));
                glVertex2f(x + FRAME, (y + BAR_HEIGHT - FRAME));
            }
            glEnd();
        }
    }*/

    /**
     * Находится ли курсор в окне
     * @param window
     * @param x
     * @param y
     * @return 
     */
    /*private static boolean isCursorOnWindow(Window window, int x, int y) {
        boolean onWindow = (height - y >= window.y && height - y <= window.y + window.height
                && x >= window.x && x <= window.x + window.width);
        // System.out.println("onWindow = " + onWindow);
        return onWindow;
    }*/
    
    /**
     * Находится ли курсор на заголовке окна
     * @param window
     * @param x
     * @param y
     * @return 
     */
    /*private static boolean isCursorOnWindowBar(Window window, int x, int y) {
        boolean onWindow = (height - y >= window.y && height - y <= window.y + BAR_HEIGHT
                && x >= window.x && x <= window.x + window.width);
        // System.out.println("onWindow = " + onWindow);
        return onWindow;
    }*/
}

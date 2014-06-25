package animationproject;
import static animationproject.TextureAtlasTest.width;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.io.xml.Dom4JDriver;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 *
 * @author Michael
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.lwjgl.opengl.*;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.opengl.Texture;
import static org.lwjgl.opengl.GL11.*;

//import javax.

public class AnimationProject {
    
    private class Sun {
        public final Texture image = graphHelper.loadImage(fileNameSun);
        public int x;
        public int y;
        
        public Sun(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    
    private int currentX = 0;
    private int currentY = 0;
    private int deltaX;
    private int deltaY;
    
    private boolean leftClicked = false;

    private final static int WINDOW_WIDTH = 800;                                     // Ширина окна приложения в пикселях
    private final static int WINDOW_HEIGHT = 600;                                     // Высота окна

    private final String FILEPATH = "resources/";                               // Относительный путь к файлам ресурсов
    private final String fileNameSun = FILEPATH + "sun.png";                    // Файл с изображением солнца
    private final String fileNameShip = FILEPATH + "ship.png";                 // Корабль
    private final String fileNameUmbrellas = FILEPATH + "umbrellas.png";        // Зонтики
    private final String fileNameBackground = FILEPATH + "background.png";      // Фоновое изображение

    private final static String windowTitle = "Animation Project";

    public static final GraphicsHelper graphHelper = new GraphicsHelper(WINDOW_WIDTH, WINDOW_HEIGHT, windowTitle);;

    //private Texture sun;
    private final List<Sun> suns = new ArrayList<>();
    private Texture ship;
    private Texture umbrellas;
    private Texture background;

    private float shipX = -100f;                                        // Начальное значение координаты X корабля
    private float shipY = 342f;                                         // Начальное значение координаты Y корабля
    private float dShipX = 1.0f;                                        // Скорость передвижения корабля
    private float sunAngle = 0.0f;                                      // Начальное значение угла поворота солнца

    public void start() {
        // Логическая переменная. При установке в true прерывает основной цикл.
        boolean done = false;

        loadResources();

        // Основной цикл.
        while (!done) {
            glClear(GL_COLOR_BUFFER_BIT);
            //GL11.gl
            listen();
            render();

            Display.update();
            Display.sync(100);

            if (Display.isCloseRequested()) {
                done = true;
            }
        }

        Display.destroy();
    }

    private void listen() {
        
    }
    
    /**
     * Загрузка ресурсов в память, например изображений.
     */
    private void loadResources() {
        //Mouse.setGrabbed(true);
        // Загрузка изображений из файлов формата PNG.
        /* УДАЛИТЬ -- */
        suns.add(new Sun(0,0));
        ship = graphHelper.loadImage(fileNameShip);
        umbrellas = graphHelper.loadImage(fileNameUmbrellas);
        background = graphHelper.loadImage(fileNameBackground);
        /* -- УДАЛИТЬ */
    }

    /**
     * Вывод изображений на экран. Здесь важен порядок наложения одной картинки
     * на другую.
     */
    private void render() {
        /* УДАЛИТЬ -- */
       // graphHelper.drawImage(background, 0, 0);
        for (Sun sun : suns) {
            graphHelper.drawImage(sun.image, sun.x, sun.y, sunAngle);
        }
        moveShip(shipX, shipY);
        graphHelper.drawImage(umbrellas, 0, 209);
        //graphHelper.drawImage(suns.get(0).image, (int) (Mouse.getX()-suns.get(0).image.getImageWidth()/2), (int) (WINDOW_HEIGHT- Mouse.getY() - suns.get(0).image.getImageHeight()/2));

        sunAngle -= 0.2f;
        /* -- УДАЛИТЬ */
    }

    /* УДАЛИТЬ -- */
    /**
     * Процедура перемещения корабля
     *
     * @param x Координата X
     * @param y Координата Y
     */
    private void moveShip(float x, float y) {
        graphHelper.drawImage(ship, x, y, sunAngle);
        shipX += dShipX;

        if (shipX >= WINDOW_WIDTH) {
            shipX = -100f;
        }
    }
    /* -- УДАЛИТЬ */

    private static final XStream xstream = new XStream(new PureJavaReflectionProvider(), new Dom4JDriver()); 
    // new PureJavaReflectionProvider() - будет использоваться конструктор по умолчанию, чтобы отсутствующие поля 
    static {
        xstream.processAnnotations(Ground.class);
        xstream.processAnnotations(Level.class);
        Ground[] gr = new Ground[1];
        xstream.alias("Grounds", gr.getClass());
    }
    /**
     * Точка входа в программу. С этого места начинается выполнение кода.
     *
     * @param args Аргументы командной строки (не используется)
     */
    public static void main(String[] args) throws IOException {
        //AnimationProject animation = new AnimationProject();
        //animation.start();
        //write();
        //read();
        writeLevel();
    }
    
    private static void writeLevel() throws IOException {
        Level level = new Level();
        level.id = 1;

        int[] x = new int[0];
        xstream.alias("row", x.getClass());
        level.grounds = new int[5][5];

        for (int[] g : level.grounds) {           
            for (int i =0; i < g.length; i++) {
                g[i] = (int) (Math.random()*16);
                if (g[i] == 16) {
                    g[i] = 15;
                }
            }
        }
        Writer writer = new FileWriter("level.xml");
        xstream.toXML(level, writer);
    }
    
    private static void write() throws IOException {
        Ground[] gr = new Ground[16];
        /*Object a;
        gr[0] = new Ground();
        gr[0].imageFile = "gr/gr0.png";
        gr[0].id = 0;
        
        gr[1] = new Ground();
        gr[1].imageFile = "gr/gr1.png";
        gr[1].id = 1;
        
        gr[2] = new Ground();
        gr[2].imageFile = "gr/gr2.png";
        gr[2].id = 2;*/
        int id = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                gr[id] = new Ground();
                gr[id].id = id;
                gr[id].col = j;
                gr[id].row = i;
                id++;
            }
        }
        
        Writer writer = new FileWriter("Grounds.xml");
        xstream.toXML(gr, writer);
    }
    
    private static void read() throws FileNotFoundException {
        Reader reader = new FileReader("Grounds.xml");
        Ground[] gr = (Ground[]) xstream.fromXML(reader);
        for (Ground g : gr) {
            System.out.println(g.imageFile);
            System.out.println(g.id);
        }
    }
}

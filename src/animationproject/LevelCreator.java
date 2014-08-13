package animationproject;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.io.xml.Dom4JDriver;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Класс для генерации случайных уровней. Размер уровня - переменная SIZE.
 *
 * @author Michael
 */
public class LevelCreator {

    // Размер уровня не должен превышать 8192*8192 пикселей
    // (в случае, если уровень рисуется на отдельной текстуре).
    final static int SIZE = 256; 
    final static int H_SIZE = 100;
    final static int V_SIZE = 100;

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
        //writeLevel();
        try {
            createLevel("level.ini", H_SIZE, V_SIZE);
        } catch (Exception ex) {}
    }

    private static void writeLevel() throws IOException {
        Level level = new Level();
        level.id = 1;

        short[] x = new short[0];
        xstream.alias("row", x.getClass());
        level.grounds = new short[SIZE][SIZE]; // Создание уровня

        // Заполнение уровня
        for (short[] g : level.grounds) {
            for (int i = 0; i < g.length; i++) {
                g[i] = (short) (Math.random() * 16); // 16 - количество участков земли (с 0 по 15)
                if (g[i] == 16) {
                    g[i] = 15;
                }
            }
        }
        Writer writer = new FileWriter("level.xml");
        xstream.toXML(level, writer);
    }

    public static Level readLevel(String filename) throws Exception {
        try (FileInputStream input = new FileInputStream(filename)) {
            Properties properties = new Properties();
            properties.load(input);
            int vert = Integer.parseInt(properties.getProperty("VERTICAL_SIZE"));
            int hor = Integer.parseInt(properties.getProperty("HORIZONTAL_SIZE"));
            //System.out.println("vert: " + vert);
            //System.out.println("hor: " + hor);
            String grounds = properties.getProperty("GROUNDS");

            short[][] groundArray = new short[vert][hor];
            StringTokenizer tok = new StringTokenizer(grounds);
            int i = 0;
            int j = 0;
            while (tok.hasMoreTokens()) {
                groundArray[i][j] = Short.parseShort(tok.nextToken());
                j++;
                if (j == hor) {
                    j = 0;
                    i++;
                }
            }
            /*for (short[] k : groundArray) {
                System.out.println(Arrays.toString(k));
            }*/
            int levelId = Integer.parseInt(properties.getProperty("LEVEL_ID"));
            Level level = new Level();
            level.id = levelId;
            level.grounds = groundArray;
            return level;
        }
    }

    public static void createLevel(String filename, int horSize, int vertSize) throws Exception {
        Properties properties = new Properties();
        properties.setProperty("LEVEL_ID", "1");
        properties.setProperty("VERTICAL_SIZE", Integer.toString(vertSize));
        properties.setProperty("HORIZONTAL_SIZE", Integer.toString(horSize));
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < vertSize; i++) {
            for (int j = 0; j < horSize; j++) {
                short ground = (short) (Math.random() * 16);
                if (ground == 16) {
                    ground = 15;
                }
                builder.append(ground).append(" ");
            }
        }
        properties.setProperty("GROUNDS", builder.toString());
        try (FileOutputStream output = new FileOutputStream(filename)) {
            properties.store(output, "Level parameters:");
        }
    }
}

package animationproject;
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
 * Класс для генерации случайных уровней. Размер уровня - переменная SIZE.
 * @author Michael
 */

public class LevelCreator {
    
    final static int SIZE = 256; // Размер уровня не должен превышать 8192*8192 пикселей.

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
        writeLevel();
    }
    
    private static void writeLevel() throws IOException {
        Level level = new Level();
        level.id = 1;

        short[] x = new short[0];
        xstream.alias("row", x.getClass());
        level.grounds = new short[SIZE][SIZE]; // Создание уровня

        // Заполнение уровня
        for (short[] g : level.grounds) {           
            for (int i =0; i < g.length; i++) {
                g[i] = (short) (Math.random()*16); // 16 - количество участков земли (с 0 по 15)
                if (g[i] == 16) {
                    g[i] = 15;
                }
            }
        }
        Writer writer = new FileWriter("level.xml");
        xstream.toXML(level, writer);
    }
}

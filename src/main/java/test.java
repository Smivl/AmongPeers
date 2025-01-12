import javafx.scene.paint.Color;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.TemplateField;

enum testing{
    ONE,
    TWO
}

public class test {
    public test() {
    }

    public static void main(String[] args) {
        SequentialSpace space = new SequentialSpace();

        try {
            space.put(testing.ONE);
            Object[] o = space.get(new ActualField(testing.ONE));
            System.out.println("RAN");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}
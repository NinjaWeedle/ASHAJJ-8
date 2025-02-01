/**
 * Library of Keyboard input methods.
 * When called, it returns key pressed.
 * @version (10/20/2021)
 */
import java.util.Scanner;

public class Keyboard
{
    public static int getInt()
    {
        Scanner input = new Scanner(System.in);
        int intVal = input.nextInt();
        return intVal;
    }
    public static double getDouble()
    {
        Scanner input = new Scanner(System.in);
        double doubleVal = input.nextDouble();
        return doubleVal;
    }
    public static String getString()
    {
        Scanner input = new Scanner(System.in);
        String stringVal = input.nextLine();
        return stringVal;
    }
}

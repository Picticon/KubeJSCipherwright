package pictisoft.cipherwright.util;

public class StringConversions
{
    public static float getAsFloat(String numberString)
    {
        try
        {
            return Float.parseFloat(numberString);

        } catch (Exception ignored)
        {
        }
        return 0;
    }
}

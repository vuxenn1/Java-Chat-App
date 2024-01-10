import java.util.Arrays;

public class Crypting
{
    public static int[] Encrypt(String text)
    {
        char[] arr = text.toCharArray();
        int[] res = new int[arr.length];

        for(int i=0;i<arr.length;i++)
        {
            res[i] = (int) arr[i];

            //Encryption part multiply with 25 and add 25
            res[i] = (res[i] * 25) + 25;
        }

        return res;
    }

    public static String Decrypt(int[] arr)
    {
        String text = "";
        char[] charArr = new char[arr.length];
        for(int i=0;i<arr.length;i++)
        {
            charArr[i] = (char) ((arr[i] - 25) / 25);
            text += charArr[i];
        }
        return text;
    }
}

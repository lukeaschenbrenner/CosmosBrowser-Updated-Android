package dwai.cosmosbrowser.messaging;

import static com.lukeapps.basest.Base10Conversions.SYMBOL_TABLE;

import android.util.Log;

import com.lukeapps.basest.Base10Conversions;
import com.lukeapps.basest.Decode;
import com.lukeapps.basest.Encode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.brotli.dec.BrotliInputStream;

import dwai.cosmosbrowser.MainBrowserScreen;

/**
 * Created by Stefan on 10/19/2014.
 * A buffer to store the SMS Text Message data.
 */
public class TextMessage {
    private final String TAG = "TextMessage";
    private int howManyAdded = 0;
    public String[] textBuffer = null;

    /**
     * Constructs a new TextMessage object that allows for the use of a String buffer to represent a text message.
     * @param sizeOfParts size of the buffer (in terms of parts)
     */
    public TextMessage(int sizeOfParts){
        if(sizeOfParts < 0){
            Log.e(TAG, "******* ERROR SIZE OF PARTS IS NEGATIVE");
            return;
        }
        textBuffer = new String[sizeOfParts];
    }

    public void addPart(int index, String part) throws Exception {
        if(index < 0 || index > textBuffer.length || part == null){
            Log.e(TAG, "******* ERROR EITHER PART WAS NULL OR INDEX WAS OUT OF BOUNDS");
            return;
        }

        textBuffer[index] = part;
        howManyAdded++;
        if(howManyAdded == textBuffer.length){
            //we have them all! render the page.
            String reassembled = "";
            for(String value : textBuffer){
                if(value == null){
                    throw new Exception("One of the strings is missing.");
                }
                reassembled += value;
            }
            String[] reassembledAsArray = Base10Conversions.explode(reassembled);
            int[] nums = new int[reassembledAsArray.length];
            for(int i = 0; i < nums.length; i++){
                nums[i] = (Arrays.binarySearch(SYMBOL_TABLE, reassembledAsArray[i]));
            }//getting negative values into nums?
            Encode decoder = new Encode();//data=nums
            int[] decoded = decoder.encode_raw(124, 256, 7, 6, nums);
            byte[] decodedbytes = integersToBytes(decoded);
            //int to bytes gives complete garbage data
            //BrotliInputStream.
            InputStream targetStream = new ByteArrayInputStream(decodedbytes);
            BrotliInputStream input = new BrotliInputStream(targetStream);
            ArrayList<Integer> intsArray = new ArrayList<>();
            int i;
            while ((i = input.read()) != -1) {
                intsArray.add(i);
                //System.out.print((char) i);
            }
            byte[] almostLast = integersToBytes(convertIntegers(intsArray));
            String s = new String(almostLast, StandardCharsets.UTF_8);
            MainBrowserScreen.webView.loadDataWithBaseURL(null, s, "text/html", "utf-8", null);

        }
    }

    byte[] integersToBytes(int[] values) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        for(int i=0; i < values.length; ++i)
        {
            dos.writeInt(values[i]);
        }

        return baos.toByteArray();
    }
    public static int[] convertIntegers(List<Integer> integers)
    {
        int[] ret = new int[integers.size()];
        Iterator<Integer> iterator = integers.iterator();
        for (int i = 0; i < ret.length; i++)
        {
            ret[i] = iterator.next().intValue();
        }
        return ret;
    }

    /**
     * returns the text message to the activity in question.
     */
    private void flush(){

    }



}

package dwai.cosmosbrowser.messaging;

import static com.lukeapps.basest.Base10Conversions.SYMBOL_TABLE;

import android.util.Log;

import com.lukeapps.basest.Base10Conversions;
import com.lukeapps.basest.Decode;
import com.lukeapps.basest.Encode;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;//don't need?
//import mainbrowserscreen when finished
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import dwai.cosmosbrowser.util.Index;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.brotli.dec.BrotliInputStream;

/**
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
            Log.e(TAG, "******* ERROR: SIZE OF PARTS IS NEGATIVE");
            return;
        }
        textBuffer = new String[sizeOfParts];
    }

    public void addPart(int index, String part) throws Exception {
        if(index < 0 || index > textBuffer.length || part == null){
            Log.e(TAG, "******* ERROR: EITHER PART WAS NULL OR INDEX WAS OUT OF BOUNDS");
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
            Log.d("reassembled", reassembled);
            String[] reassembledAsArray = Base10Conversions.explode(reassembled);
            Log.d("reassembledasarray", Arrays.toString(reassembledAsArray));
            int[] nums = new int[reassembledAsArray.length];
            for(int i = 0; i < nums.length; i++){
                nums[i] = Index.findIndex(SYMBOL_TABLE, reassembledAsArray[i]);
            }
            Log.d("nums", Arrays.toString(nums));
            Encode decoder = new Encode();//not actually encoding the data! I'm decoding the data, but I use encode and reverse the parameters. Output identical when compared to python
            int[] decoded = decoder.encode_raw(124, 256, 7, 6, nums);
            Log.d("bytes", Arrays.toString(decoded));
            byte[] decodedbytes = new byte[decoded.length];
            for(int i = 0; i < decoded.length; i++){
                decodedbytes[i] = (byte) decoded[i]; //trying to encode an int array (0 to 256) as a byte array (-128 to 127). Should be okay?
            }

            /**
             * Below is an example of a functional Brotli decoding, with the encoded data being a random website I found encoded in Brotli.
             */
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try  {
                        HttpClient httpclient = new DefaultHttpClient(); // Create HTTP Client
                        HttpGet httpget = new HttpGet("https://austindw.com/faster-website/");
                        httpget.setHeader("Accept-Encoding","gzip, deflate, br");
                        HttpResponse response = httpclient.execute(httpget);
                        //HttpEntity entity = response.getEntity();
                        //InputStream is = entity.getContent(); // Create an InputStream with the response
                        BufferedReader rd;
                        System.out.println(Arrays.toString(response.getAllHeaders())); //making sure the br header is there, for austindw.com it is
                        if(response.getLastHeader("content-encoding").getValue().equals("br")) { // check if getting brotli compressed stream
                            rd = new BufferedReader(new InputStreamReader(new BrotliInputStream(response.getEntity().getContent())));
                        }
                        else {
                            rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                        }                        StringBuilder result = new StringBuilder();
                        String line = "";
                        while ((line = rd.readLine()) != null) {
                            result.append(line);
                        }
                        Log.d("result", String.valueOf(result));
                    } catch (Exception e) {
                        e.printStackTrace(); //exception doesn't occur in this example
                    }
                }
            });

            //thread.start(); //Android requires all web requests to be separate from the main thread.

            /**
             * Below is my attempt at decoding brotli-compressed data.
             */
            try{
                InputStream targetStream = new ByteArrayInputStream(decodedbytes);

                BufferedReader rd = (new BufferedReader(new InputStreamReader(new BrotliInputStream(targetStream))));
                //ArrayList<Integer> intsArray = new ArrayList<>();
                StringBuilder result = new StringBuilder();
                String line2;

                while((line2 = rd.readLine()) != null){
                    Log.d("result", String.valueOf(line2));
                    result.append(line2);
                }


                //Don't need the next 2 lines of code if we assume that brotli is able to give me a UTF-8 HTML string
                //byte[] almostLast = integersToBytes(convertIntegers(intsArray));
                //String s = new String(almostLast, StandardCharsets.UTF_8);

                //Finally, load the data with result.toString()
                //   MainBrowserScreen.webView.loadDataWithBaseURL(null, result.toString(), "text/html", "utf-8", null);

            } catch (Exception e) {
                e.printStackTrace(); //exception DOES occur in this example
            }

        }
    }

    //unused method
    byte[] integersToBytes(int[] values) throws IOException { // Don't know if this method works?
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        for(int i=0; i < values.length; ++i)
        {
            //dos.writeInt(values[i]);
            dos.writeByte(values[i]);
        }

        return baos.toByteArray();
    }

    //don't need because brotli doesn't output ints in each line read like I initially thought, it outputs strings
    public static int[] convertIntegers(List<Integer> integers) //int arraylist to int array
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
     * Returns the text message to the activity in question. Not implemented-- don't need right now
     */
    private void flush(){

    }



}

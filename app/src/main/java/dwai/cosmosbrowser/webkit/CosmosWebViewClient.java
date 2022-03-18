package dwai.cosmosbrowser.webkit;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import butterknife.BindView;
import dwai.cosmosbrowser.MainBrowserScreen;
import dwai.cosmosbrowser.R;
import dwai.cosmosbrowser.messaging.TextMessageHandler;


public class CosmosWebViewClient extends WebViewClient {

    MainBrowserScreen s;
    private static final String TAG = "CosmosWebViewClient";
    public CosmosWebViewClient(MainBrowserScreen s){
        this.s = s;
    }

    /**
     *
     * @param view The WebView in question.
     * @param url What the URL of the link clicked was.
     * @return Whether the method felt an override was necessary.
     */
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url){
        if(view == null || url == null){
            Log.e(TAG, "***************** VIEW OR URL IS NULL ERROR *****************");
            return false;
        }
        if(!(view instanceof CosmosWebView)){
            Log.e(TAG, "***************** NOT AN INSTANCE OF COSMOS WEB VIEW! ERROR *****************");
            return false;
        }
        TextMessageHandler handler = TextMessageHandler.getInstance();

        handler.sendTextMessage(url);


        s.UpdateMyText(url);

        return true;
    }

}

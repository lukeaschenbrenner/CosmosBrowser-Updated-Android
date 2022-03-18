package dwai.cosmosbrowser.webkit;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;

import dwai.cosmosbrowser.R;

public class CosmosWebView extends WebView {
    private static final String TAG = "CosmosWebView";



    private WebViewClient webViewClient;

    public CosmosWebView(Context context) {
        super(context);
        this.setWebViewClient(webViewClient);
    }

    public CosmosWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setWebViewClient(webViewClient);
    }

    public CosmosWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setWebViewClient(webViewClient);
    }

    @Override
    public void goBack() {
        ListView settingsListView = (ListView)findViewById(R.id.settingsListView);
        if(settingsListView.getVisibility() == View.VISIBLE){
            settingsListView.setVisibility(View.INVISIBLE);
        }
        else{
            super.goBack();
        }
    }
}

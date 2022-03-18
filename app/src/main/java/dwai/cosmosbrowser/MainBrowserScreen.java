package dwai.cosmosbrowser;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dwai.cosmosbrowser.messaging.TextMessage;
import dwai.cosmosbrowser.messaging.TextMessageHandler;
import dwai.cosmosbrowser.webkit.CosmosWebView;
import dwai.cosmosbrowser.webkit.CosmosWebViewClient;

public class MainBrowserScreen extends Activity {
    /** TODO: Add custom CSS files for commonly visited websites to save on space *
     TODO: fancy encoded post request, maybe include app version
     TODO: allow submitting basic web forms as post request
     TODO: Be able to load previously requested web pages by reading messages from number, no default sms perms required*/

    private static final String TAG = "MainBrowserScreen";
    public static CosmosWebView webView;
    @BindView(R.id.settingsListView)SettingsListView settingsListView;
    @BindView(R.id.rootWebView)CosmosWebView cosmosWebView;
    @BindView(R.id.searchBar)LinearLayout searchBar;
    @BindView(R.id.urlEditText)EditText urlEditText;
    @BindView(R.id.tabsListView)ListView tabsListView;
    @BindView(R.id.tabsButton)ImageView tabsButton;
    @BindView(R.id.moreSettingsButton)ImageView moreSettingsButton;
    @BindView(R.id.moreOptionsView)RelativeLayout moreOptionsView;
    @BindView(R.id.topSettingsBar)LinearLayout topSettingsBar;
    public List<View> expandableViews = new ArrayList<View>();
    private static final int[] PERMISSIONS_REQUEST_ID = {1, 2, 3, 4, 5};

    @SuppressWarnings("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            String[] permissions = {Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS, Manifest.permission.WRITE_CONTACTS};
            for(int i = 0; i < permissions.length; i++){
                if(checkSelfPermission(permissions[i]) == PackageManager.PERMISSION_DENIED){
                    ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_ID[i]);
                }
            }
        }

        TextMessageHandler handler = TextMessageHandler.getInstance();
        setContentView(R.layout.activity_main_browser_screen);
        webView = (CosmosWebView)findViewById(R.id.rootWebView);
        webView.setWebViewClient(new CosmosWebViewClient(this));
        webView.getSettings().setJavaScriptEnabled(false);

        ButterKnife.bind(this);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            searchBar.setElevation(10);
            moreOptionsView.setElevation(13);
        }
        expandableViews.add(moreOptionsView);
        expandableViews.add(tabsListView);
        webView.loadUrl("file:///android_asset/testfile.html");
//        cosmosWebView = new CosmosWebView(this);
        //don't need?

        cosmosWebView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    hideAllExpandableViews(MainBrowserScreen.this);
                    hideKeyboard(MainBrowserScreen.this);
                    view.requestFocus();

                }
                return false;
            }

        });

            urlEditText.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
// If triggered by an enter key, close the keyboard and load the site
                        if (actionId == EditorInfo.IME_ACTION_GO) {
                            hideAllExpandableViews(MainBrowserScreen.this);
                            hideKeyboard(MainBrowserScreen.this);
                            MainBrowserScreen.webView.requestFocus();

                            try {
                                TextMessage.url = urlEditText.getText().toString();
                                handler.sendTextMessage(urlEditText.getText().toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return true;
                        }
                        return false;
                    }
                });

        urlEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocused) {
                if(isFocused){
                    urlEditText.setSelection(urlEditText.getText().length());
                    searchBar.getChildAt(1).setVisibility(View.GONE);
                    searchBar.getChildAt(2).setVisibility(View.GONE);
                    hideAllExpandableViews(MainBrowserScreen.this);
                }
                else{
                    //This sets the cursor of the edit text back to the front so that the url is visible when focus changes.
                    urlEditText.setSelection(0);
                    searchBar.getChildAt(1).setVisibility(View.VISIBLE);
                    searchBar.getChildAt(2).setVisibility(View.VISIBLE);
                }
            }
        });


    }
    public void UpdateMyText(String mystr) {
        urlEditText.setText(mystr);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(grantResults.length == 0) {
            Toast.makeText(this, "No permissions granted!", Toast.LENGTH_SHORT).show();
            return;
        }
        for(int i = 0; i < grantResults.length; i++) {
            if(grantResults[i] == PackageManager.PERMISSION_DENIED){
                Toast.makeText(this, "Permission error: not granted: " + permissions[i], Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Toast.makeText(this, "Permissions are accepted! App is now functional.", Toast.LENGTH_SHORT).show();

        //enableApp here... we know the permissions are granted by this line.
    }

    public void onResume(){
        findViewById(R.id.rootWebView).requestFocus();
        super.onResume();
    }

    public void clickedTabs(View v){
     /*   moreOptionsView.setVisibility(View.INVISIBLE);
        int visibility = tabsListView.getVisibility();
        if(visibility == View.VISIBLE){
            tabsListView.setVisibility(View.INVISIBLE);
        }
        else{
            tabsListView.setVisibility(View.VISIBLE);
        }
        */
        Intent intent = new Intent(this, DefaultSMSActivity.class);
        startActivity(intent);


    }

    public void clickedSettings(View v){

        tabsListView.setVisibility(View.INVISIBLE);
        int visibility = moreOptionsView.getVisibility();
        if(visibility == View.VISIBLE){
            moreOptionsView.setVisibility(View.INVISIBLE);
        }
        else{
            moreOptionsView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed(){
        ListView settingsListView = (ListView)findViewById(R.id.settingsListView);

        if(searchBar.getChildAt(1).getVisibility() == View.GONE){
            searchBar.getChildAt(1).setVisibility(View.VISIBLE);
            searchBar.getChildAt(2).setVisibility(View.VISIBLE);
            cosmosWebView.requestFocus();
            hideKeyboard(this);
        }
        else if(settingsListView.getVisibility() == View.VISIBLE){
            hideAllExpandableViews(MainBrowserScreen.this);
        }
        else if(tabsListView.getVisibility() == View.VISIBLE){
            hideAllExpandableViews(MainBrowserScreen.this);
        }
        else{
            super.onBackPressed();
        }
    }

    private void hideAllExpandableViews(Activity activity){
        for(View v : expandableViews){
            v.setVisibility(View.GONE);
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}

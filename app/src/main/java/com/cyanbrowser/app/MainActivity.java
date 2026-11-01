package com.cyanbrowser.app;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

    private WebView browser;
    private EditText addressBar;

    private static final int CYAN = Color.rgb(0, 230, 230);
    private static final int DARK = Color.rgb(20, 20, 20);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setBackgroundColor(Color.BLACK);

        // Keep the interface clear of system bars.
        screen.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(
                0,
                insets.getSystemWindowInsetTop(),
                0,
                insets.getSystemWindowInsetBottom()
            );
            return insets;
        });

        addressBar = new EditText(this);
        addressBar.setSingleLine(true);
        addressBar.setTextColor(CYAN);
        addressBar.setHintTextColor(Color.LTGRAY);
        addressBar.setHint("Search or enter a website");
        addressBar.setBackgroundColor(DARK);
        addressBar.setImeOptions(EditorInfo.IME_ACTION_GO);

        screen.addView(addressBar, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setBackgroundColor(Color.BLACK);

        Button back = makeButton("←");
        Button forward = makeButton("→");
        Button reload = makeButton("↻");

        controls.addView(back, buttonLayout());
        controls.addView(forward, buttonLayout());
        controls.addView(reload, buttonLayout());

        screen.addView(controls);

        browser = new WebView(this);
        browser.setBackgroundColor(Color.BLACK);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.getSettings().setDomStorageEnabled(true);

        browser.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view, WebResourceRequest request) {
                String scheme = request.getUrl().getScheme();

                // Only open web addresses inside the browser.
                return scheme == null ||
                    (!scheme.equalsIgnoreCase("http") &&
                     !scheme.equalsIgnoreCase("https"));
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!addressBar.hasFocus()) {
                    addressBar.setText(url);
                }
            }
        });

        screen.addView(browser, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1
        ));

        setContentView(screen);

        back.setOnClickListener(v -> {
            if (browser.canGoBack()) browser.goBack();
        });

        forward.setOnClickListener(v -> {
            if (browser.canGoForward()) browser.goForward();
        });

        reload.setOnClickListener(v -> browser.reload());

        addressBar.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO ||
                (event != null &&
                 event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                 event.getAction() == KeyEvent.ACTION_DOWN)) {

                openAddress(addressBar.getText().toString());
                return true;
            }
            return false;
        });

        browser.loadUrl("https://duckduckgo.com/");
    }

    private Button makeButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(CYAN);
        button.setBackgroundColor(DARK);
        button.setTextSize(20);
        return button;
    }

    private LinearLayout.LayoutParams buttonLayout() {
        return new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1
        );
    }

    private void openAddress(String input) {
        String text = input.trim();
        if (text.isEmpty()) return;

        String url;

        if (text.startsWith("https://") ||
            text.startsWith("http://")) {
            url = text;
        } else if (!text.contains(" ") && text.contains(".")) {
            url = "https://" + text;
        } else {
            url = "https://duckduckgo.com/?q=" + Uri.encode(text);
        }

        addressBar.clearFocus();
        browser.requestFocus();

        InputMethodManager keyboard = (InputMethodManager)
            getSystemService(Context.INPUT_METHOD_SERVICE);

        if (keyboard != null) {
            keyboard.hideSoftInputFromWindow(
                addressBar.getWindowToken(), 0
            );
        }

        browser.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if (browser.canGoBack()) {
            browser.goBack();
        } else {
            super.onBackPressed();
        }
    }
}

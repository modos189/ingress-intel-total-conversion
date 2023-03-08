package org.exarhteam.iitc_mobile;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public class IITC_WebViewPopup extends WebView {
    private WebSettings mSettings;
    private IITC_Mobile mIitc;
    private SharedPreferences mSharedPrefs;
    private final String mDesktopUserAgent = "Mozilla/5.0 (X11; Linux x86_64; rv:17.0)" +
            " Gecko/20130810 Firefox/17.0 Iceweasel/17.0.8";
    private String mMobileUserAgent;

    private Dialog mDialog;


    // init web view
    private void iitc_init(final Context c) {
        if (isInEditMode()) return;
        mIitc = (IITC_Mobile) c;
        mSettings = getSettings();
        mSettings.setJavaScriptEnabled(true);
        //mSettings.setSavePassword(true);
        //mSettings.setSaveFormData(true);
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            setWebContentsDebuggingEnabled(true);

        setWebChromeClient(new WebChromeClient() {
            @Override
            public void onCloseWindow(WebView view) {
                Log.d("close popup window");
                mDialog.dismiss();
            }
        });
        setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                final Uri uri = Uri.parse(url);
                final String uriHost = uri.getHost();
                final String uriPath = uri.getPath();
                final String uriQuery = uri.getQueryParameter("q");
                if ((uriHost.startsWith("google.") || uriHost.contains(".google."))
                        && uriPath.equals("/url") && uriQuery != null) {
                    Log.d("popup: redirect to: " + uriQuery);
                    return shouldOverrideUrlLoading(view, uriQuery);
                }
                if (uriHost.endsWith("facebook.com")
                        && (uriPath.contains("oauth") || uriPath.equals("/login.php") || uriPath.equals("/checkpoint/"))) {
                    Log.d("popup: Facebook login");
                    return false;
                }
                if (uriHost.startsWith("accounts.google.") ||
                         uriHost.startsWith("appengine.google.") ||
                         uriHost.startsWith("accounts.youtube.")) {
                    Log.d("popup: Google login");
                    return false;
                }
                Log.d("popup: no login link, start external app to load url: " + url);

                Log.d("close popup");
                mDialog.dismiss();

                final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                // make new activity independent from iitcm
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mIitc.startActivity(intent);

                return true;
            }
        });

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mIitc);

        // Hack to work Google login page in old browser
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP &&
                !mSharedPrefs.getBoolean("pref_fake_user_agent", false))
            mSharedPrefs.edit().putBoolean("pref_fake_user_agent", true).apply();

        final String original_ua = mSettings.getUserAgentString();
        // remove ";wv " marker as Google blocks WebViews from using OAuth
        // https://developer.chrome.com/multidevice/user-agent#webview_user_agent
        mMobileUserAgent = original_ua.replace("; wv", "");
        setUserAgent();

        mDialog = new Dialog(mIitc);
        mDialog.setContentView(this);
        final WebView view = this;
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                view.destroy();
            }
        });
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                view.destroy();
            }
        });

        Log.d("open popup");
        mDialog.show();
        mDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    }

    // constructors -------------------------------------------------
    public IITC_WebViewPopup(final Context context) {
        super(context);

        iitc_init(context);
    }

    public IITC_WebViewPopup(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        iitc_init(context);
    }

    public IITC_WebViewPopup(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        iitc_init(context);
    }

    public void setUserAgent() {
        final String ua = mSharedPrefs.getBoolean("pref_fake_user_agent", false) ?
                mDesktopUserAgent : mMobileUserAgent;
        Log.d("setting user agent to: " + ua);
        mSettings.setUserAgentString(ua);
    }
}

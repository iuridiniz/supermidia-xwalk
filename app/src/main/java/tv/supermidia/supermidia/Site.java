package tv.supermidia.supermidia;

import org.xwalk.core.JavascriptInterface;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class Site extends Activity {
    public static final String TAG = "Site";
    private XWalkView mXWalkView;
    private XWalkView mXWalkViewClock;
    private TextView mOverlayText;
    private int secondsSinceStart;
    private Thread updateThread;
    private Object wifiManagerJSInterface;
    private WifiManager wifiManager;
    //Date startDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Alternate texture
        XWalkPreferences.setValue(XWalkPreferences.ANIMATABLE_XWALK_VIEW, true);
        // turn on debugging
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.webviewer);

        mXWalkView = (XWalkView) findViewById(R.id.main_site);
        mOverlayText = (TextView) findViewById(R.id.overlay_text);

        //make it fullscreen
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        /* Open my site */
        //mXWalkView.load("http://supermidia.tv/", null);
        mXWalkView.load("file:///android_asset/index.html", null);

        /* show the version of XWalk */
        Log.d(TAG, "Running XWalk: " + mXWalkView.getXWalkVersion());

        /* Update text Thread */
        this.threadStart();

        /* a test clock */
        //mXWalkViewClock = (XWalkView) findViewById(R.id.clock);
        //mXWalkViewClock.load("file:///android_asset/clock.html", null);
        //mXWalkViewClock.setBackgroundColor(Color.TRANSPARENT);
        //mXWalkViewClock.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        /* create wifi manager and JS interface */
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

        wifiManagerJSInterface = new Object() {
            @JavascriptInterface
            public void enable() {
                if (wifiManager == null) {
                    return;
                }
                wifiManager.setWifiEnabled(true);
            }

            @JavascriptInterface
            public void disable() {
                if (wifiManager == null) {
                    return;
                }
                wifiManager.setWifiEnabled(false);
            }

            @JavascriptInterface
            public boolean isEnabled() {
                if (wifiManager == null) {
                    return false;
                }
                return wifiManager.isWifiEnabled();
            }
        };
        mXWalkView.addJavascriptInterface(wifiManagerJSInterface, "wifi");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mXWalkView != null) {
            mXWalkView.pauseTimers();
            mXWalkView.onHide();
        }
        this.threadStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mXWalkView != null) {
            mXWalkView.resumeTimers();
            mXWalkView.onShow();
        }
        this.threadStart();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mXWalkView != null) {
            mXWalkView.onDestroy();
        }
        this.threadStop();
    }

    private void threadStart() {
        if (updateThread != null) {
            return;
        }
        updateThread = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateTextView();
                            }

                            private void updateTextView() {
                                //Date noteTS = Calendar.getInstance().getTime();
                                secondsSinceStart += 1;
                                WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
                                String ip = Formatter.formatIpAddress(
                                        wm.getConnectionInfo().getIpAddress());
                                mOverlayText.setText(String.format(
                                        "JAVA[ip: %s - uptime: %d:%02d:%02d]",
                                        ip,
                                        (secondsSinceStart/3600),
                                        (secondsSinceStart%3600)/60,
                                        (secondsSinceStart%60)));

                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        updateThread.start();
    }

    private void threadStop() {
        if (updateThread == null) {
            return;
        }
        updateThread.interrupt();
        try {
            updateThread.join();
            updateThread = null;
        } catch (InterruptedException e) {
        }

    }
}
package com.myevcar.mobile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.webkit.MimeTypeMap;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class MainActivity extends Activity {
    // The V36 website lives inside app/src/main/assets/site, not on a server.
    private static final String OFFLINE_HOST = "appassets.androidplatform.net";
    private static final String START_URL = "https://" + OFFLINE_HOST + "/index.html";
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        webView.setBackgroundColor(0xFF0D1723);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportMultipleWindows(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setAllowFileAccess(false);
        webView.getSettings().setAllowContentAccess(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return serveEmbeddedAsset(request.getUrl());
            }
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                return serveEmbeddedAsset(Uri.parse(url));
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return navigate(request.getUrl());
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return navigate(Uri.parse(url));
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView parent, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                if (!isUserGesture) return false;
                final WebView popup = new WebView(MainActivity.this);
                popup.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                        openExternal(request.getUrl());
                        return true;
                    }
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        openExternal(Uri.parse(url));
                        return true;
                    }
                });
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(popup);
                resultMsg.sendToTarget();
                return true;
            }
        });

        setContentView(webView);
        webView.loadUrl(START_URL);
    }

    private WebResourceResponse serveEmbeddedAsset(Uri uri) {
        if (!"https".equalsIgnoreCase(uri.getScheme()) ||
                !OFFLINE_HOST.equalsIgnoreCase(uri.getHost())) return null;

        String path = uri.getPath();
        if (path == null || path.isEmpty() || "/".equals(path)) path = "/index.html";
        if (path.startsWith("/")) path = path.substring(1);
        if (path.contains("..") || path.contains("\\") || path.startsWith("."))
            return missingAsset();

        try {
            InputStream stream = getAssets().open("site/" + path);
            String mime = mimeType(path);
            String encoding = (mime.startsWith("text/") || "application/javascript".equals(mime))
                    ? "UTF-8" : null;
            return new WebResourceResponse(mime, encoding, stream);
        } catch (IOException e) {
            return missingAsset();
        }
    }

    private static WebResourceResponse missingAsset() {
        WebResourceResponse response = new WebResourceResponse(
                "text/plain", "UTF-8", new ByteArrayInputStream(
                "Embedded MY EV CAR asset unavailable".getBytes(StandardCharsets.UTF_8)));
        response.setStatusCodeAndReasonPhrase(404, "Not Found");
        return response;
    }

    private static String mimeType(String path) {
        int dot = path.lastIndexOf('.');
        String extension = dot < 0 ? "" : path.substring(dot + 1).toLowerCase(Locale.US);
        if ("html".equals(extension)) return "text/html";
        if ("js".equals(extension)) return "application/javascript";
        if ("css".equals(extension)) return "text/css";
        if ("svg".equals(extension)) return "image/svg+xml";
        if ("webp".equals(extension)) return "image/webp";
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return type != null ? type : "application/octet-stream";
    }

    private boolean navigate(Uri uri) {
        String scheme = uri.getScheme();
        if (scheme == null || "about".equalsIgnoreCase(scheme)) return false;
        if ("https".equalsIgnoreCase(scheme) && OFFLINE_HOST.equalsIgnoreCase(uri.getHost()))
            return false;
        openExternal(uri);
        return true;
    }

    private void openExternal(Uri uri) {
        String scheme = uri.getScheme();
        if (scheme == null) return;
        if (!("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme) ||
              "mailto".equalsIgnoreCase(scheme) || "tel".equalsIgnoreCase(scheme) ||
              "geo".equalsIgnoreCase(scheme) || "whatsapp".equalsIgnoreCase(scheme)))
            return;
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (Exception ex) {
            Toast.makeText(this, "Aucune application disponible pour ouvrir ce lien.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}

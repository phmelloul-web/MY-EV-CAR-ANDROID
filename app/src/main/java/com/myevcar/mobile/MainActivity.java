package com.myevcar.mobile;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.webkit.MimeTypeMap;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String LIVE_URL = "https://broad-moon-14ca.phmelloul.workers.dev/";
    private static final String LIVE_HOST = "broad-moon-14ca.phmelloul.workers.dev";
    private static final String LOCAL_HOST = "appassets.androidplatform.net";
    private static final String LOCAL_URL = "https://" + LOCAL_HOST + "/index.html";
    private static final int REQUEST_FILES = 1001;
    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        webView.setBackgroundColor(0xFF0D1723);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setSupportMultipleWindows(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) s.setSafeBrowsingEnabled(true);
        webView.clearCache(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) {
                return serveBundledAsset(r.getUrl());
            }
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView v, String url) {
                return serveBundledAsset(Uri.parse(url));
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                return navigate(r.getUrl());
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, String url) {
                return navigate(Uri.parse(url));
            }
            @Override
            public void onReceivedError(WebView v, WebResourceRequest request,
                                        android.webkit.WebResourceError error) {
                if (request.isForMainFrame() && isLiveSite(request.getUrl())) {
                    Toast.makeText(MainActivity.this,
                        "Mode hors connexion : site MY EV CAR integre", Toast.LENGTH_SHORT).show();
                    v.loadUrl(LOCAL_URL);
                }
            }
            @Override
            public void onReceivedHttpError(WebView v, WebResourceRequest request,
                                            WebResourceResponse errorResponse) {
                if (request.isForMainFrame() && isLiveSite(request.getUrl())
                        && errorResponse.getStatusCode() >= 400) {
                    v.loadUrl(LOCAL_URL);
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView parent, boolean isDialog,
                    boolean isUserGesture, Message resultMsg) {
                if (!isUserGesture) return false;
                final WebView popup = new WebView(MainActivity.this);
                popup.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                        openExternal(req.getUrl());
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
            @Override
            public boolean onShowFileChooser(WebView view,
                    ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                try {
                    startActivityForResult(params.createIntent(), REQUEST_FILES);
                    return true;
                } catch (ActivityNotFoundException e) {
                    fileCallback = null;
                    return false;
                }
            }
        });

        webView.setDownloadListener((url, agent, disposition, mime, length) ->
                openExternal(Uri.parse(url)));
        setContentView(webView);
        if (savedInstanceState == null) {
            Toast.makeText(this, "myevcarv1 complete", Toast.LENGTH_SHORT).show();
            webView.loadUrl(LIVE_URL);
        } else {
            webView.restoreState(savedInstanceState);
        }
    }

    private boolean isLiveSite(Uri uri) {
        String scheme = uri.getScheme();
        return ("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme))
                && LIVE_HOST.equalsIgnoreCase(uri.getHost());
    }

    private WebResourceResponse serveBundledAsset(Uri uri) {
        if (!"https".equalsIgnoreCase(uri.getScheme()) ||
            !LOCAL_HOST.equalsIgnoreCase(uri.getHost())) return null;
        String path = uri.getPath();
        if (path == null || path.isEmpty() || "/".equals(path)) path = "/index.html";
        if (path.startsWith("/")) path = path.substring(1);
        if (path.contains("..") || path.contains("\\") || path.startsWith(".")) return notFound();
        try {
            InputStream input = getAssets().open("site/" + path);
            String mime = mimeType(path);
            String encoding = (mime.startsWith("text/") || "application/javascript".equals(mime)) ? "UTF-8" : null;
            return new WebResourceResponse(mime, encoding, input);
        } catch (IOException e) {
            return notFound();
        }
    }

    private WebResourceResponse notFound() {
        WebResourceResponse r = new WebResourceResponse("text/plain", "UTF-8",
            new ByteArrayInputStream("Fichier MY EV CAR introuvable".getBytes(StandardCharsets.UTF_8)));
        r.setStatusCodeAndReasonPhrase(404, "Not Found");
        return r;
    }

    private String mimeType(String name) {
        int dot = name.lastIndexOf('.');
        String ext = dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.US);
        if ("html".equals(ext)) return "text/html";
        if ("js".equals(ext)) return "application/javascript";
        if ("css".equals(ext)) return "text/css";
        if ("svg".equals(ext)) return "image/svg+xml";
        if ("webp".equals(ext)) return "image/webp";
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        return type == null ? "application/octet-stream" : type;
    }

    private boolean navigate(Uri uri) {
        String scheme = uri.getScheme();
        if (scheme == null || "about".equalsIgnoreCase(scheme)) return false;
        if (isLiveSite(uri)) return false;
        if ("https".equalsIgnoreCase(scheme) && LOCAL_HOST.equalsIgnoreCase(uri.getHost())) return false;
        openExternal(uri);
        return true;
    }

    private void openExternal(Uri uri) {
        String scheme = uri.getScheme();
        if (scheme == null) return;
        if (!("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme) ||
              "mailto".equalsIgnoreCase(scheme) || "tel".equalsIgnoreCase(scheme) ||
              "geo".equalsIgnoreCase(scheme) || "whatsapp".equalsIgnoreCase(scheme))) return;
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (Exception e) {
            Toast.makeText(this, "Aucune application disponible pour ouvrir ce lien.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FILES && fileCallback != null) {
            fileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            fileCallback = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        if (webView != null) webView.saveState(state);
        super.onSaveInstanceState(state);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (fileCallback != null) fileCallback.onReceiveValue(null);
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}

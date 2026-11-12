package com.soundscope.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.webkit.JsResult;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.webkit.WebViewAssetLoader;

public class MainActivity extends Activity {
  private static final int PICK_AUDIO = 41;
  private WebView view;
  private ValueCallback<Uri[]> pending;
  @Override public void onCreate(Bundle state) {
    super.onCreate(state);
    WebViewAssetLoader loader = new WebViewAssetLoader.Builder().addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();
    view = new WebView(this); setContentView(view);
    WebSettings settings = view.getSettings(); settings.setJavaScriptEnabled(true); settings.setDomStorageEnabled(true);
    settings.setAllowFileAccess(false); settings.setAllowContentAccess(true); settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
    view.setWebViewClient(new WebViewClient() {
      @Override public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) { return loader.shouldInterceptRequest(r.getUrl()); }
      @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
        Uri u=r.getUrl(); if ("https".equals(u.getScheme()) && "appassets.androidplatform.net".equals(u.getHost()) && u.getPath()!=null && u.getPath().startsWith("/assets/web/")) return false;
        if ("https".equals(u.getScheme()) || "http".equals(u.getScheme())) { try { startActivity(new Intent(Intent.ACTION_VIEW,u)); } catch (Exception e) { Toast.makeText(MainActivity.this,"Tidak dapat membuka link",Toast.LENGTH_SHORT).show(); } } return true;
      }
      @Override public void onReceivedError(WebView v, WebResourceRequest r, android.webkit.WebResourceError e) { if (r.isForMainFrame()) Toast.makeText(MainActivity.this,"Halaman gagal dimuat. Coba buka ulang aplikasi.",Toast.LENGTH_LONG).show(); }
    });
    view.setWebChromeClient(new WebChromeClient() {
      @Override public boolean onJsConfirm(WebView v, String url, String message, JsResult result) {
        new AlertDialog.Builder(MainActivity.this).setMessage(message).setPositiveButton("Lanjut", (d,w)->result.confirm()).setNegativeButton("Batal", (d,w)->result.cancel()).setOnCancelListener(d->result.cancel()).show(); return true;
      }
      @Override public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams params) {
        if (pending!=null) pending.onReceiveValue(null); pending=cb;
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT); i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("audio/*");
        try { startActivityForResult(i,PICK_AUDIO); return true; } catch(Exception e) { pending.onReceiveValue(null); pending=null; Toast.makeText(MainActivity.this,"Pemilih file tidak tersedia",Toast.LENGTH_SHORT).show(); return false; }
      }
    });
    if (state!=null) view.restoreState(state); else view.loadUrl("https://appassets.androidplatform.net/assets/web/index.html");
  }
  @Override protected void onActivityResult(int request, int result, Intent data) { super.onActivityResult(request,result,data); if(request==PICK_AUDIO&&pending!=null) { pending.onReceiveValue(result==RESULT_OK&&data!=null&&data.getData()!=null?new Uri[]{data.getData()}:null); pending=null; } }
  @Override protected void onSaveInstanceState(Bundle out) { super.onSaveInstanceState(out); view.saveState(out); }
  @Override public void onBackPressed() { if(view.canGoBack()) view.goBack(); else super.onBackPressed(); }
  @Override protected void onDestroy() { if(pending!=null) { pending.onReceiveValue(null); pending=null; } if(view!=null) view.destroy(); super.onDestroy(); }
}

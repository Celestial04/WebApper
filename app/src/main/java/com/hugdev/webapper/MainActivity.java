package com.hugdev.webapper;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;


            }

        });

        ProgressBar myProgressBar = findViewById(R.id.progressBar);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // Afficher la barre de progression
                myProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // Masquer la barre de progression
                myProgressBar.setVisibility(View.GONE);
            }

        });
        webView.loadUrl("https://google.com/");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // Mettre à jour la barre de progression
                myProgressBar.setProgress(newProgress);
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                // Demander la permission d'accéder à la localisation
                callback.invoke(origin, true, false);
            }
        });

        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }


        Button nex = findViewById(R.id.button);
        nex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoForward()) {
                    webView.goForward();
                }
            }
        });
        Button viewFav = findViewById(R.id.button6);
        viewFav.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String currentUrl = webView.getUrl();
                String currentName = webView.getTitle();
                SharedPreferences sharedPreferences = getSharedPreferences("Favoris", MODE_PRIVATE);
                Map<String, ?> favoritesMap = sharedPreferences.getAll();
                List<String> favoritesList = new ArrayList<>();
                for (Map.Entry<String, ?> entry : favoritesMap.entrySet()) {
                    String url = entry.getKey();
                    String name = entry.getValue().toString();
                    String currentNamee = name + " : " + url;
                    favoritesList.add(currentNamee);
                }
                final String[] favoritesArray = favoritesList.toArray(new String[favoritesList.size()]);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Favoris enregistrés")
                        .setItems(favoritesArray, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Gérer le clic sur un élément de la liste (par exemple, charger l'URL dans WebView)
                                for (Map.Entry<String, ?> entry : favoritesMap.entrySet()) {
                                    if (entry.getValue().toString().equals(favoritesArray[which].split(" : ")[0])) {
                                        String url = entry.getKey();
                                        webView.loadUrl(url);
                                        break;
                                    }
                                }
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


        Button AddFav = findViewById(R.id.button5);

                AddFav.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        WebView webView = findViewById(R.id.webview);
                        String currentUrl = webView.getUrl();
                        String currentName = webView.getTitle();
                        SharedPreferences sharedPreferences = getSharedPreferences("Favoris", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(currentUrl, currentName);
                        editor.apply();

                        if (sharedPreferences.contains(currentUrl)) {
                            Toast.makeText(getApplicationContext(), currentName + " est déjà enregistré.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), currentName + " à été enregistré.", Toast.LENGTH_SHORT).show();
                            }
                    }
                });




        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean firstStart = prefs.getBoolean("firstStart", true);

        if (firstStart) {
            showStartDialog();
        } else {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstStart", false);
            editor.apply();
        }

Button remFav = findViewById(R.id.button7);
remFav.setOnClickListener(new View.OnClickListener(){
    @Override
    public void onClick(View view) {
        String currentUrl = webView.getUrl();
        String currentName = webView.getTitle();
        SharedPreferences sharedPreferences = getSharedPreferences("Favoris", MODE_PRIVATE);
        Map<String, ?> favoritesMap = sharedPreferences.getAll();
        List<String> favoritesList = new ArrayList<>();
        for (Map.Entry<String, ?> entry : favoritesMap.entrySet()) {
            String url = entry.getKey();
            String name = entry.getValue().toString();
            String currentNamee = name + " : " + url;
            favoritesList.add(currentNamee);
        }
        final String[] favoritesArray = favoritesList.toArray(new String[favoritesList.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Favoris enregistrés")
                .setItems(favoritesArray, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Supprimer le favori correspondant
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        for (Map.Entry<String, ?> entry : favoritesMap.entrySet()) {
                            if (entry.getValue().toString().equals(favoritesArray[which].split(" : ")[0])) {
                                String url = entry.getKey();
                                editor.remove(url); // Supprimer le favori
                                editor.apply();
                                break;
                            }
                        }
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
});

    }




    private void showStartDialog() {
        Intent intent = new Intent(this, tuto_1.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void FirstStart() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        boolean firstStart = prefs.getBoolean("firstStart", true);
        editor.putBoolean("firstStart", false);
        editor.apply();
    }

    public void onDeleteDataClick(View view) {
        WebView webView = findViewById(R.id.webview);
        webView.clearCache(true);
        webView.clearSslPreferences();
        webView.clearFormData();
        webView.clearHistory();
        webView.clearMatches();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstStart", true);
        editor.apply();
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
        // Supprimer les données enregistrées ici en utilisant les étapes décrites précédemment
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor.clear();
        editor.commit();

        File filesDir = getFilesDir();
        deleteRecursive(filesDir);

        File cacheDir = getCacheDir();
        deleteRecursive(cacheDir);

        File externalFilesDir = getExternalFilesDir(null);
        if (externalFilesDir != null) {
            deleteRecursive(externalFilesDir);
        }

        // Relancer l'activité ou l'application
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }


    @Override
    public void onBackPressed() {
        WebView webView = findViewById(R.id.webview);
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

}

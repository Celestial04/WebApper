package com.hugdev.webapper;

import static android.app.DownloadManager.*;

import android.animation.ValueAnimator;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private AlertDialog alert2;



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

        webView.setDownloadListener(new DownloadListener() {
            List<AlertDialog.Builder> dialogsList = new ArrayList<>();
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                String fileName = URLUtil.guessFileName(url, contentDisposition, mimeType);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                dialogsList.add(builder);
                builder.setMessage("Voulez-vous télécharger \"" + fileName + "\" ?");
                builder.setCancelable(false);

                builder.setPositiveButton(
                        "Oui",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                Request request = new Request(Uri.parse(url));
                                request.allowScanningByMediaScanner();
                                request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                long downloadId = downloadManager.enqueue(request);
                                Toast.makeText(getApplicationContext(), "Le téléchargement de " + fileName + " est terminé.", Toast.LENGTH_SHORT).show();
                                // Créer et afficher un autre AlertDialog pour afficher le statut du téléchargement
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                                dialogsList.add(builder2);
                                builder2.setMessage("Téléchargement en cours...");
                                builder2.setCancelable(false);
                                LinearLayout layout = new LinearLayout(MainActivity.this);
                                layout.setOrientation(LinearLayout.VERTICAL);
                                layout.setPadding(50, 50, 50, 50);

                                // Ajouter un TextView pour afficher les statistiques de téléchargement
                                TextView textView = new TextView(MainActivity.this);
                                TextView speedl = new TextView(MainActivity.this);
                                textView.setText("Téléchargement en cours...");
                                speedl.setText("Vitesse du téléchargement :");
                                speedl.setTextSize(14);
                                textView.setTextSize(20);

                                layout.addView(textView);
                                layout.addView(speedl);
                                // Ajouter un bouton "Annuler"
                                Button cancelButton = new Button(MainActivity.this);
                                cancelButton.setText("Annuler");

                                ProgressBar progressdl = new ProgressBar(MainActivity.this, null, android.R.attr.progressBarStyleHorizontal);
                                layout.addView(progressdl);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    progressdl.setMin(0);
                                    progressdl.setIndeterminate(false);
                                }

                                cancelButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {


                                        downloadManager.remove(downloadId); // Annuler le téléchargement
                                        Toast.makeText(getApplicationContext(), "Le téléchargement de " + fileName + " a été annulé.", Toast.LENGTH_SHORT).show();
                                        showNextDialog();
                                    }
                                });



                                builder2.setView(layout);
                                builder2.setPositiveButton("Annuler le téléchargement", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        downloadManager.remove(downloadId);
                                        dialog.cancel();
                                        Toast.makeText(getApplicationContext(), "Le téléchargement de " + fileName + " à été annulé.", Toast.LENGTH_SHORT).show();
                                        if (alert2 != null && alert2.isShowing()) { // Vérification que alert2 n'est pas null avant de l'utiliser
                                            alert2.dismiss();
                                            dialog.dismiss();


                                        }
                                    }
                                });

                                AlertDialog alert2 = builder2.create();
                                dialog.dismiss();
                                alert2.show();

                                // Vérifier l'état du téléchargement toutes les secondes jusqu'à ce qu'il soit terminé
                                Handler handler = new Handler();
                                final int[] bytesDownloaded = {0};

// Récupérer le temps écoulé depuis le début du téléchargement
                                long startTime = System.currentTimeMillis();
                                Runnable runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        Query query = new Query();
                                        query.setFilterById(downloadId);
                                        Cursor cursor = downloadManager.query(query);
                                        if (cursor.moveToFirst()) {
                                            int columnIndex = cursor.getColumnIndex(COLUMN_STATUS);
                                            int status = cursor.getInt(columnIndex);
                                            if (status == STATUS_SUCCESSFUL) {
                                                alert2.dismiss(); // Fermer le AlertDialog affichant les statistiques
                                                Toast.makeText(getApplicationContext(), "Le téléchargement de " + fileName + " est terminé.", Toast.LENGTH_SHORT).show();
                                            } else if (status == STATUS_FAILED) {
                                                alert2.dismiss(); // Fermer le AlertDialog affichant les statistiques
                                                Toast.makeText(getApplicationContext(), "Le téléchargement de " + fileName + " a échoué.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                int downloadedBytes = cursor.getInt(cursor.getColumnIndex(COLUMN_BYTES_DOWNLOADED_SO_FAR));
                                                int bytesTotal = cursor.getInt(cursor.getColumnIndex(COLUMN_TOTAL_SIZE_BYTES));
                                                int progress = (int) ((bytesDownloaded[0] * 100L) / bytesTotal);
                                                long elapsedTime = System.currentTimeMillis() - startTime;
                                                double bytesPerSecond = (downloadedBytes - bytesDownloaded[0]) * 1000.0 / elapsedTime / 1000000.0;

                                                // Calculer l'ETA
                                                int bytesRemaining = bytesTotal - bytesDownloaded[0];
                                                long etaSeconds = (long) (bytesRemaining / bytesPerSecond);
                                                String eta = String.format("%02d:%02d:%02d", etaSeconds / 3600, (etaSeconds % 3600) / 60, etaSeconds % 60);
                                                Log.d("test", "run: " + eta);
                                                // Afficher la vitesse de transfert dans la TextView
                                                speedl.setText("Vitesse du téléchargement : " + String.format("%.1f", bytesPerSecond) + " MB/S");
                                                // Mettre à jour le nombre d'octets téléchargés jusqu'à présent
                                                bytesDownloaded[0] = downloadedBytes;
                                                textView.setText("Téléchargement en cours... " + progress + "%");
                                                progressdl.setProgress(progress);
                                                handler.postDelayed(this, 500); // Vérifier à nouveau dans 50 milliseconde
                                            }
                                        }
                                        cursor.close();

                                    }
                                };
                                handler.postDelayed(runnable, 500); // Vérifier l'état du téléchargement toutes les secondes
                            }

                        }).setNegativeButton(
                        "Non",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Request request = new Request(Uri.parse(url));
                                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                long downloadId = downloadManager.enqueue(request);
                                dialog.cancel();
                                Toast.makeText(getApplicationContext(), "Le téléchargement de " + fileName + " à été annulé.", Toast.LENGTH_SHORT).show();
                                if (alert2 != null && alert2.isShowing()) { // Vérification que alert2 n'est pas null avant de l'utiliser
                                    alert2.dismiss();
                                    downloadManager.remove(downloadId);

                                }
                            }
                        });


                AlertDialog alert = builder.create();
                alert.show();
            }
            private void showNextDialog() {
                    alert2.dismiss();
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
        viewFav.setOnClickListener(new View.OnClickListener() {
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

        AddFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebView webView = findViewById(R.id.webview);
                String currentUrl = webView.getUrl();
                String currentName = webView.getTitle();
                SharedPreferences sharedPreferences = getSharedPreferences("Favoris", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(currentUrl, currentName);
                editor.apply();

                if (sharedPreferences.contains(currentName)) {
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

        Button reloadButton = findViewById(R.id.button8); // assuming you have a Button with id "reload_button" in your layout
        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload(); // reload the web page
            }
        });

        Button remFav = findViewById(R.id.button7);
        remFav.setOnClickListener(new View.OnClickListener() {
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
                builder.setTitle("Supprimer un favori")
                        .setItems(favoritesArray, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Supprimer le favori correspondant
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                for (Map.Entry<String, ?> entry : favoritesMap.entrySet()) {
                                    if (entry.getValue().toString().equals(favoritesArray[which].split(" : ")[0])) {
                                        String url = entry.getKey();
                                        editor.remove(url); // Supprimer le favori
                                        editor.apply();
                                        Toast.makeText(MainActivity.this, favoritesArray[which].split(" : ")[0] + " à été supprimé.", Toast.LENGTH_SHORT).show(); // Afficher le message toast
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


    public void setAlert2(AlertDialog alert2) {
        this.alert2 = alert2;
    }
}


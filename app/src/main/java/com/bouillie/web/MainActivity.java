package com.bouillie.web;

import static android.app.DownloadManager.Query;
import static android.app.DownloadManager.Request;
import static android.app.usage.UsageEvents.Event.NONE;

import android.annotation.SuppressLint;
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
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.boullie.web.R;
import com.google.android.material.color.DynamicColors;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    private HorizontalScrollView scroll;
    private AlertDialog alert2;
    private int selectedTheme;
    // Méthode pour formater le temps en heures, minutes et secondes
// Méthode pour formater le temps en heures, minutes et secondes
// Méthode pour formater le temps en heures, minutes et secondes
    private String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs);
    }





    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        scroll = findViewById(R.id.scroll);




        int savedTheme = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getInt("selected_theme", NONE);
        if (savedTheme != NONE) {
            selectedTheme = savedTheme;
        }else{
            selectedTheme = R.style.Theme_WebApper_dark;
        }
        setTheme(selectedTheme);
        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());
        DynamicColors.applyToActivitiesIfAvailable(getApplication());
        setContentView(R.layout.activity_main);

        LinearLayout linearLayout2 = findViewById(R.id.scrollvv);
        WebView webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl("https://google.com");
                return true;


            }

        });

        Button soundButton = findViewById(R.id.soundButton);
        soundButton.setOnClickListener(v -> {
            // Créer une boîte de dialogue avec trois options
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Choisissez un thème");

            String[] options = {"Thème sombre", "Thème clair"};
            builder.setItems(options, (dialog, which) -> {
                // Vérifier quelle option a été sélectionnée et mettre le thème correspondant
                switch (which) {
                    case 0:
                        Toast.makeText(getApplicationContext(), "Thème sombre activé", Toast.LENGTH_SHORT).show();
                        selectedTheme = R.style.Theme_WebApper_dark;
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        updateTheme();
                        break;
                    case 1:
                        Toast.makeText(getApplicationContext(), "Thème clair activé", Toast.LENGTH_SHORT).show();
                        selectedTheme = R.style.Theme_WebApper_light;
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        updateTheme();
                        break;
                }
            });
            builder.show();
        });
        Button nex = findViewById(R.id.button);
        if (webView.canGoForward()) {
            // Afficher le bouton "forward"
            nex.setVisibility(View.VISIBLE);
        } else {
            // Masquer le bouton "forward"
            nex.setVisibility(View.GONE);
        }
        nex.setOnClickListener(v -> {
            if (webView.canGoForward()) {
                webView.goForward();
                nex.setVisibility(View.VISIBLE);
            }else {
                nex.setVisibility(View.GONE);
            }
        });


        webView.setDownloadListener(new DownloadListener() {
            final List<AlertDialog.Builder> dialogsList = new ArrayList<>();

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
                                // Créer et afficher un autre AlertDialog pour afficher le statut du téléchargement
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                                dialogsList.add(builder2);
                                builder2.setTitle("Téléchargement en cours...");
                                builder2.setMessage("Téléchargement de votre fichier en cours...");
                                builder2.setCancelable(false);
                                LinearLayout layout = new LinearLayout(MainActivity.this);
                                layout.setOrientation(LinearLayout.VERTICAL);
                                layout.setPadding(50, 50, 50, 50);

                                // Ajouter un TextView pour afficher les statistiques de téléchargement
                                TextView textView = new TextView(MainActivity.this);
                                TextView speedl = new TextView(MainActivity.this);
                                TextView textViewETA = new TextView(MainActivity.this); // Ajouter un TextView pour afficher l'ETA
                                textView.setText(R.string.t_l_chargement_en_cours);
                                speedl.setText("Vitesse : 0.0 MB/s"); // Mettez la valeur de la vitesse du téléchargement correcte ici
                                textViewETA.setText("ETA: Calculating..."); // Initialiser l'ETA avec un message par défaut
                                speedl.setTextSize(14);
                                textView.setTextSize(20);

                                layout.addView(textView);
                                layout.addView(speedl);
                                layout.addView(textViewETA); // Ajouter le TextView ETA
                                // Ajouter un bouton "Annuler"
                                Button cancelButton = new Button(MainActivity.this);
                                cancelButton.setText("Annuler");

                                ProgressBar progressdl = new ProgressBar(MainActivity.this, null, android.R.attr.progressBarStyleHorizontal);
                                layout.addView(progressdl);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    progressdl.setMin(0);
                                    progressdl.setIndeterminate(false);
                                }

                                cancelButton.setOnClickListener(v -> {


                                    downloadManager.remove(downloadId); // Annuler le téléchargement
                                    Toast.makeText(getApplicationContext(), getString(R.string.le_tl_chargement_de) + fileName + " a été annulé.", Toast.LENGTH_SHORT).show();
                                    showNextDialog();
                                });


                                builder2.setView(layout);
                                builder2.setPositiveButton("Annuler le téléchargement", (dialog1, id1) -> {
                                    downloadManager.remove(downloadId);
                                    dialog1.cancel();
                                    Toast.makeText(getApplicationContext(), getString(R.string.le_tl_chargement_de) + fileName + " à été annulé.", Toast.LENGTH_SHORT).show();
                                    if (alert2 != null && alert2.isShowing()) { // Vérification que alert2 n'est pas null avant de l'utiliser
                                        alert2.dismiss();
                                        dialog1.dismiss();


                                    }
                                });

                                alert2 = builder2.create();
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
                                            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                                            int status = cursor.getInt(columnIndex);
                                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                                alert2.dismiss(); // Fermer le AlertDialog affichant les statistiques
                                                Toast.makeText(getApplicationContext(), getString(R.string.le_tl_chargement_de) + fileName + " est terminé.", Toast.LENGTH_SHORT).show();
                                            } else if (status == DownloadManager.STATUS_FAILED) {
                                                alert2.dismiss(); // Fermer le AlertDialog affichant les statistiques
                                                Toast.makeText(getApplicationContext(), getString(R.string.le_tl_chargement_de) + fileName + " a échoué.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                @SuppressLint("Range") int downloadedBytes = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                                                @SuppressLint("Range") int bytesTotal = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                                                int progress = (int) ((downloadedBytes * 100L) / (double) bytesTotal);

                                                long elapsedTime = System.currentTimeMillis() - startTime;
                                                double bytesPerSecond = (downloadedBytes - bytesDownloaded[0]) * 1000.0 / elapsedTime / 1000000.0;

                                                // Calculer l'ETA
                                                int bytesRemaining = bytesTotal - downloadedBytes;
                                                long etaSeconds = (long) (bytesRemaining / bytesPerSecond);
                                                String etaFormatted = formatDuration(etaSeconds);
                                                textViewETA.setText("ETA : " + etaFormatted);



                                                // Afficher la vitesse de transfert dans la TextView
                                                speedl.setText("Vitesse : " + String.format("%.1f", bytesPerSecond) + " MB/s");
                                                // Mettre à jour le nombre d'octets téléchargés jusqu'à présent
                                                bytesDownloaded[0] = downloadedBytes;
                                                textView.setText(getString(R.string.t_l_chargement_en_cours) + Math.abs(progress) + "%");

                                                progressdl.setProgress(Math.abs(progress));
                                                handler.postDelayed(this, 500); // Vérifier à nouveau dans 50 millisecondes
                                            }
                                        }
                                        cursor.close();

                                    }
                                };
                                handler.postDelayed(runnable, 1000); // Vérifier l'état du téléchargement toutes les secondes
                            }

                        }).setNegativeButton(
                        "Non",
                        (dialog, id) -> {

                            dialog.cancel();
                            Toast.makeText(getApplicationContext(), getString(R.string.le_tl_chargement_de) + fileName + " à été annulé.", Toast.LENGTH_SHORT).show();

                        });


                AlertDialog alert = builder.create();
                alert.show();
            }

            private void showNextDialog() {
                if (alert2 != null && alert2.isShowing()) {
                    alert2.dismiss();
                }
            }
        });


        ProgressBar myProgressBar = findViewById(R.id.progressBar);
        EditText urlcontent = findViewById(R.id.UrlContent);
        ImageView secureimage = findViewById(R.id.SecureImage);
        urlcontent.setImeOptions(EditorInfo.IME_ACTION_GO);
        urlcontent.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_SEARCH) {
                String url = urlcontent.getText().toString();
                webView.loadUrl(url);
                return true;
            }
            return false;
        });

        webView.setWebViewClient(new WebViewClient() {

            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // Afficher la barre de progression
                myProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                urlcontent.setText(url);
                // Masquer la barre de progression
                myProgressBar.setVisibility(View.GONE);

                // Vérifier si la WebView peut naviguer vers l'avant
                try {
                    URL parsedUrl = new URL(url);
                    String protocol = parsedUrl.getProtocol();
                    if (protocol.equals("https")) {
                        // Afficher l'icône de cadenas pour indiquer que le site est sécurisé
                        secureimage.setImageResource(R.drawable.encrypted);
                    } else {
                        // Afficher une autre icône pour indiquer que le site n'est pas sécurisé
                        secureimage.setImageResource(R.drawable.uncrypted);
                    }
                } catch (MalformedURLException e) {
                    // La syntaxe de l'URL est incorrecte
                    e.printStackTrace();
                }

                if (view.canGoForward()) {
                    // Afficher le bouton "forward"
                    nex.setVisibility(View.VISIBLE);
                } else {
                    // Masquer le bouton "forward"
                    nex.setVisibility(View.GONE);
                }
            }

        });

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {

                // Mettre à jour la barre de progression
                myProgressBar.setProgress(newProgress);
                // Vérifier si la valeur atteint 100
                if (newProgress == 100) {
                    // Réinitialiser la ProgressBar
                    myProgressBar.setProgress(0);
                    // Masquer la ProgressBar
                    myProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                // Demander la permission d'accéder à la localisation
                callback.invoke(origin, true, false);
            }
        });

        webView.loadUrl("https://google.com/");


        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }



        Button viewFav = findViewById(R.id.button6);
        viewFav.setOnClickListener(view -> {
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
                    .setItems(favoritesArray, (dialog, which) -> {
                        // Gérer le clic sur un élément de la liste (par exemple, charger l'URL dans WebView)
                        for (Map.Entry<String, ?> entry : favoritesMap.entrySet()) {
                            if (entry.getValue().toString().equals(favoritesArray[which].split(" : ")[0])) {
                                String url = entry.getKey();
                                webView.loadUrl(url);
                                break;
                            }
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        });


        Button AddFav = findViewById(R.id.button5);

        AddFav.setOnClickListener(v -> {
            WebView webView1 = findViewById(R.id.webview);
            String currentUrl = webView1.getUrl();
            String currentName = webView1.getTitle();
            SharedPreferences sharedPreferences = getSharedPreferences("Favoris", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(currentUrl, currentName);
            editor.apply();

            if (sharedPreferences.contains(currentName)) {
                Toast.makeText(getApplicationContext(), currentName + " est déjà enregistré.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), currentName + " à été enregistré.", Toast.LENGTH_SHORT).show();
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
        reloadButton.setOnClickListener(v -> {
            webView.reload(); // reload the web page
        });

        Button remFav = findViewById(R.id.button7);
        remFav.setOnClickListener(view -> {
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
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putInt("selected_theme", selectedTheme).apply();
    }
    private void updateTheme() {
        AppCompatDelegate.setDefaultNightMode(selectedTheme);
        getDelegate().applyDayNight();
        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putInt("selected_theme", selectedTheme).apply();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");


        builder.setMessage("Êtes-vous certain de vouloir supprimer toutes les données de WebApper ?");
builder.setCancelable(false);
builder.setNegativeButton("NON", (dialog, which) -> Toast.makeText(getApplicationContext(), "Vos données n'ont pas été supprimées", Toast.LENGTH_SHORT).show());

        builder.setPositiveButton("OK", (dialog, which) -> {
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
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            editor.clear();
            editor.apply();
            SharedPreferences sharedPreferences = getSharedPreferences("Favoris", MODE_PRIVATE);
            SharedPreferences.Editor editorf = sharedPreferences.edit();
            editorf.clear();
            editorf.apply();
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
        });

        builder.show();


        // Le reste du code de votre activité principale


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



    public void setAlert2(AlertDialog alert2) {
        this.alert2 = alert2;
    }
}


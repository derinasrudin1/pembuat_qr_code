package com.example.pembuatqr;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    private Button btnCreate, btnScan;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        btnCreate = findViewById(R.id.btnCreate);
        btnScan = findViewById(R.id.btnScan);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateQr.class);
                startActivity(intent);
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Scanner();
            }
        });

    }
    private void Scanner(){
        ScanOptions options= new ScanOptions();
        options.setPrompt("Volume Up to Flash On");
        options.setBeepEnabled(false);
        options.setOrientationLocked(true);
        options.setCaptureActivity(StarScan.class);
        launcher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> launcher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            String scannedResult = result.getContents();

            // Deteksi apakah hasil scan merupakan URL, dengan atau tanpa "http://" atau "https://"
            if (isValidUrl(scannedResult)) {
                // Jika hasil scan tidak memiliki "http://" atau "https://", tambahkan "http://"
                String finalScannedResult = scannedResult; // Buat variabel baru untuk menyimpan hasil akhir
                if (!scannedResult.startsWith("http://") && !scannedResult.startsWith("https://")) {
                    finalScannedResult = "http://" + scannedResult;
                }

                // Tampilkan dialog dengan opsi untuk membuka di browser
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                String finalScannedResultCopy = finalScannedResult; // Jadikan variabel final atau effectively final
                builder.setTitle("Result")
                        .setMessage("Scanned URL: " + finalScannedResult)
                        .setPositiveButton("Open in Browser", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Intent untuk membuka URL di browser
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalScannedResultCopy));
                                startActivity(browserIntent);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            } else {
                // Jika hasil scan bukan URL, tampilkan dalam dialog biasa
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Result");
                builder.setMessage(scannedResult);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
            }
        }
    });

    // Fungsi untuk memeriksa apakah string merupakan URL yang valid
    private boolean isValidUrl(String text) {
        return text.startsWith("http://") || text.startsWith("https://") || text.startsWith("www.") || text.contains(".com") || text.contains(".co.id") || text.contains(".id") || text.contains(".go.id") || text.contains(".net") || text.contains(".org");
    }


}
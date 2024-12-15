package com.example.pembuatqr;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.decoder.QRCodeDecoderMetaData;
//import com.journeyapps.barcodescanner.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.IOException;
import java.util.Hashtable;

public class MainActivity extends AppCompatActivity {
    private Button btnCreate, btnScan, btnScanFromGallery;
    private TextView tvAboutMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCreate = findViewById(R.id.btnCreate);
        btnScan = findViewById(R.id.btnScan);
        btnScanFromGallery = findViewById(R.id.btnScanFromGallery);
        tvAboutMe =  findViewById(R.id.tvAboutMe);

        tvAboutMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent untuk membuka URL Tentang
                String url = "https://github.com/derinasrudin1/pembuat_qr_code.git";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });


        btnCreate.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateQr.class);
            startActivity(intent);
        });

        btnScan.setOnClickListener(v -> Scanner());

        btnScanFromGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });
    }

    private void Scanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Arahkan kamera ke QR Code");
        options.setBeepEnabled(false);
        options.setOrientationLocked(true);
        options.setCaptureActivity(StarScan.class);
        launcher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> launcher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            String scannedResult = result.getContents();

            if (isValidUrl(scannedResult)) {
                String finalScannedResult = scannedResult.startsWith("http://") || scannedResult.startsWith("https://")
                        ? scannedResult
                        : "http://" + scannedResult;

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Result")
                        .setMessage("Scanned URL: " + finalScannedResult)
                        .setPositiveButton("Open in Browser", (dialogInterface, i) -> {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalScannedResult));
                            startActivity(browserIntent);
                        })
                        .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                        .show();
            } else {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Result")
                        .setMessage(scannedResult)
                        .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                        .setNeutralButton("Copy", (dialogInterface, i) -> {
                            // Salin teks ke clipboard
                            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            android.content.ClipData clip = android.content.ClipData.newPlainText("Scanned Text", scannedResult);
                            clipboard.setPrimaryClip(clip);

                            // Tampilkan toast sebagai konfirmasi
                            Toast.makeText(MainActivity.this, "Text copied to clipboard!", Toast.LENGTH_SHORT).show();
                        })
                        .show();
            }
        }
    });

    ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImage = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                        String decodedText = decodeQrCodeFromBitmap(bitmap);

                        if (decodedText != null) {
                            if (isValidUrl(decodedText)) {
                                String finalScannedResult = decodedText.startsWith("http://") || decodedText.startsWith("https://")
                                        ? decodedText
                                        : "http://" + decodedText;

                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Result")
                                        .setMessage("Scanned URL: " + finalScannedResult)
                                        .setPositiveButton("Open in Browser", (dialogInterface, i) -> {
                                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalScannedResult));
                                            startActivity(browserIntent);
                                        })
                                        .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                                        .show();
                            } else {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Result")
                                        .setMessage(decodedText)
                                        .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                                        .setNeutralButton("Copy", (dialogInterface, i) -> {
                                            // Salin teks ke clipboard
                                            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                            android.content.ClipData clip = android.content.ClipData.newPlainText("Scanned Text", decodedText);
                                            clipboard.setPrimaryClip(clip);

                                            // Tampilkan toast sebagai konfirmasi
                                            Toast.makeText(MainActivity.this, "Text copied to clipboard!", Toast.LENGTH_SHORT).show();
                                        })
                                        .show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "No QR Code found in the image", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private String decodeQrCodeFromBitmap(Bitmap bitmap) {
        try {
            int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
            bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
            com.google.zxing.RGBLuminanceSource source = new com.google.zxing.RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new QRCodeMultiReader().decode(binaryBitmap);
            return result.getText();
        } catch (NotFoundException | FormatException | ChecksumException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isValidUrl(String text) {
        return text.startsWith("http://") || text.startsWith("https://") || text.startsWith("www.") || text.contains(".com") || text.contains(".co.id") || text.contains(".id") || text.contains(".go.id") || text.contains(".net") || text.contains(".org");
    }
}

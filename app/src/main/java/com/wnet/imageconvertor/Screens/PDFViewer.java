package com.wnet.imageconvertor.Screens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.wnet.imageconvertor.BuildConfig;
import com.wnet.imageconvertor.R;

import java.io.File;
import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PDFViewer extends AppCompatActivity {

    File file;

    @BindView(R.id.filePath)
    TextView filePath;

    @BindView(R.id.fileSize)
    TextView fileSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfviewer);
        ButterKnife.bind(this);

        file = (File) getIntent().getSerializableExtra("filePath");
        filePath.setText("File Name: "+file.getName());
        fileSize();

        PDFView pdfView = findViewById(R.id.pdfView);
        pdfView.fromFile(file)
                .enableSwipe(true) // allows to block changing pages using swipe
                .swipeHorizontal(true)
                .enableDoubletap(true)
                .defaultPage(0)
                .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                .password(null)
                .scrollHandle(null)
                .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                // spacing between pages in dp. To define spacing color, set view background
                .spacing(0)
                .pageFitPolicy(FitPolicy.WIDTH)
                .load();
    }

    @OnClick(R.id.viewPDF)
    public void OnViewPDFClicked(){
        try{
            Intent intent =new Intent(Intent.ACTION_VIEW);
            //intent.setType( "application/pdf");
            Uri path = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file);
            intent.setDataAndType(path, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share File"));
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    @OnClick(R.id.sharePDF)
    public void OnSharePDFClicked(){
        try{
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            Uri path = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file);
            intent.putExtra(Intent.EXTRA_STREAM, path);
            intent.putExtra(Intent.EXTRA_SUBJECT,
                    "Sharing File...");
            intent.putExtra(Intent.EXTRA_TEXT, "Sharing File...");

            startActivity(Intent.createChooser(intent, "Share File"));
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public void fileSize(){
        String hrSize = "File Size: ";
        double fileSizeInKB   = file.length()/1024.0;
        DecimalFormat dec = new DecimalFormat("0.00");

        if (fileSizeInKB > 1048576) {
            double fileSizeInGB = fileSizeInKB / 1048576.0;
            hrSize += dec.format(fileSizeInGB).concat(" GB");
        } else if (fileSizeInKB > 1024) {
            double fileSizeInMB = fileSizeInKB / 1024.0;
            hrSize += dec.format(fileSizeInMB).concat(" MB");
        } else {
            hrSize += dec.format(fileSizeInKB).concat(" KB");
        }
        fileSize.setText(hrSize);
    }

}

package com.wnet.imageconvertor.Screens;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.wnet.imageconvertor.R;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PDFViewer extends AppCompatActivity {

    File file;

    @BindView(R.id.filePath)
    TextView filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfviewer);
        ButterKnife.bind(this);

        file = (File) getIntent().getSerializableExtra("filePath");
        filePath.setText("File Location: "+file.getAbsolutePath());
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

    @OnClick(R.id.sharePDF)
    public void OnSharePDFClicked(){
        Intent intent =new Intent();
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));

        intent.putExtra(Intent.EXTRA_SUBJECT,
                "Sharing File...");
        intent.putExtra(Intent.EXTRA_TEXT, "Sharing File...");

        startActivity(Intent.createChooser(intent, "Share File"));
    }


}

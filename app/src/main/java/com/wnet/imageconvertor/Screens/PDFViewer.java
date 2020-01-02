package com.wnet.imageconvertor.Screens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.wnet.imageconvertor.BuildConfig;
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
        try{
            Intent intent =new Intent(Intent.ACTION_VIEW);
            //intent.setType( "application/pdf");
            Uri path = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file);
//        intent.putExtra(Intent.EXTRA_, file.getAbsolutePath());
            intent.setDataAndType(path, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            //intent.putExtra(Intent.EXTRA_SUBJECT, "Image Convertor");
            startActivity(Intent.createChooser(intent, "Share File"));
        } catch (Exception e){
            e.printStackTrace();
        }

    }

//    private fun shareFileFromStorage(path: String, mineType: String) {
//        val sharingIntent = Intent(Intent.ACTION_SEND)
//        sharingIntent.type = mineType
//        val fileToShare = File(path)
//        val uri: Uri
//        if (Build.VERSION.SDK_INT >= 24) {
//            uri = FileProvider.getUriForFile(context!!, context!!.applicationContext.packageName
//                    + ".provider", fileToShare)
//        } else {
//            uri = Uri.fromFile(fileToShare)
//        }
//        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)
//        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "RAHUL CONNECT")
//        startActivity(Intent.createChooser(sharingIntent, "Share File"))
//    }


}

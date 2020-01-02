package com.wnet.imageconvertor.Screens;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.wnet.imageconvertor.R;
import com.wnet.imageconvertor.base.BaseActivity;
import com.wnet.imageconvertor.dialog.TransparentProgressDialog;
import com.wnet.imageconvertor.util.FileUtils;
import com.wnet.imageconvertor.util.ImageConverter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.wnet.imageconvertor.util.Utils.generateUniqueImageFileName;

public class MainActivity extends BaseActivity {

    private static final String[] imageType = {"JPEG", "PNG", "PDF", "WEBP"};

    static final int REQUEST_IMAGE = 2;
    boolean initialLoad = true;
    String imagePath = "";
    Bitmap imageBitmap = null;
    Uri pickedImage = null;
    String[] permission = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    TransparentProgressDialog dialog;

    @BindView(R.id.spinner1)
    Spinner spinner;

    @BindView(R.id.image)
    ImageView imageView;

    @Override
    protected int getContentResource() {
        return R.layout.activity_main;
    }

    @Override
    protected void init(@Nullable Bundle state) {
        new Handler().post(this::initView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.gallery:
                initialLoad = false;
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE);
                return true;
            case R.id.undo:
                imageRotation(true);
                return true;
            case R.id.redo:
                imageRotation(false);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void imageRotation(boolean status) {
        try {
            Matrix matrix = new Matrix();
            if (status) {
                matrix.postRotate(-90);
            } else {
                matrix.postRotate(90);
            }
            imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true); // rotating bitmap
        } catch (Exception e) {

        }
        imageView.setImageBitmap(imageBitmap);
    }

    private void CheckPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), permission[0]) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getApplicationContext(), permission[1]) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permission, 1);
        }
    }

    @OnClick(R.id.image)
    public void OnClickImage() {
        if (initialLoad) {
            initialLoad = false;
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckPermission();
    }

    @OnClick(R.id.save_to_gallery)
    public void onConvertClicked() {
        if (imageBitmap != null) {
            String type = spinner.getSelectedItem().toString();
            dialog.show();
            if (type.equals("PDF")) {
                convertPDF(imageBitmap);
            } else {
                ImageConverter converter = new ImageConverter(this, type, imageBitmap);
                converter.execute();
            }
        }
    }

    public void convertPDF(Bitmap imageBitmap) {
        try {
            File filePath = new File(android.os.Environment.getExternalStorageDirectory(), generateUniqueImageFileName() + ".jpg");
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream); //100-best quality
            fileOutputStream.close();
            Document document = new Document();

            File file = new File(android.os.Environment.getExternalStorageDirectory(), generateUniqueImageFileName() + ".pdf");
            FileOutputStream out = new FileOutputStream(file);
            PdfWriter.getInstance(document, out); //  Change pdf's name.
//            PdfWriter.getInstance(document, new FileOutputStream(directoryPath + "/example.pdf")); //  Change pdf's name.

            document.open();

//            Image image = Image.getInstance(directoryPath + "/" + "example.jpg");  // Change image's name and extension.
            Image image = Image.getInstance(filePath.getAbsolutePath());  // Change image's name and extension.

            float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                    - document.rightMargin() - 0) / image.getWidth()) * 100; // 0 means you have no indentation. If you have any, change it.
            image.scalePercent(scaler);
            image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);

            document.add(image);
            document.close();

            filePath.delete();
            dialog.cancel();
            Intent intent = new Intent(this, PDFViewer.class);
            intent.putExtra("filePath", file);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void convertedImage(String fileName) {
        dialog.cancel();
        if (fileName != null) {
            File filePath = new File(android.os.Environment.getExternalStorageDirectory(), fileName);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(filePath.getAbsolutePath()), "image/*");
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "Error Getting Image!!", Toast.LENGTH_LONG).show();
        }
    }

    public void initView() {
        dialog = new TransparentProgressDialog(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, imageType);
        spinner.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK) {
            pickedImage = data.getData();
            if (pickedImage != null) {
                imagePath = FileUtils.getPath(getApplicationContext(), pickedImage);
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                imageBitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
                imageView.setImageBitmap(imageBitmap);
            }
        }
    }
}

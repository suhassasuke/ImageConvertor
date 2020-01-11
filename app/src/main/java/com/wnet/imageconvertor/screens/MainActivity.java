package com.wnet.imageconvertor.screens;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wnet.imageconvertor.BuildConfig;
import com.wnet.imageconvertor.R;
import com.wnet.imageconvertor.base.BaseActivity;
import com.wnet.imageconvertor.dialog.TransparentProgressDialog;
import com.wnet.imageconvertor.util.FileUtils;
import com.wnet.imageconvertor.util.ImageConverter;

import java.io.File;
import java.io.FileOutputStream;

import butterknife.BindView;
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
    int progressValue = 100;
    int rotateAngle = 0;
    TransparentProgressDialog dialog;

    @BindView(R.id.edit_title_layout_first)
    LinearLayout editTitleLayoutFirst;

    @BindView(R.id.edit_title_layout_second)
    LinearLayout editTitleLayoutSecond;

    @BindView(R.id.main_layout)
    LinearLayout mainLayout;

    @BindView(R.id.crop_layout)
    LinearLayout cropLayout;

    @BindView(R.id.edit_layout_second)
    LinearLayout editLayoutSecond;

    @BindView(R.id.spinner1)
    Spinner spinner;

    @BindView(R.id.image)
    ImageView imageView;

    @BindView(R.id.image_quality)
    SeekBar imageQuality;

    @BindView(R.id.quality_percentage)
    TextView qualityPercentage;

    @BindView(R.id.reset_image)
    Button resetImage;

    @BindView(R.id.cropImageView)
    CropImageView cropImageView;

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

    @OnClick(R.id.undo)
    public void onUndoClicked() {
        imageRotation(true);
    }

    @OnClick(R.id.redo)
    public void onRedoClicked() {
        imageRotation(false);
    }

    @OnClick(R.id.gallery)
    public void onGalleryClicked() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @OnClick(R.id.crop)
    public void onCropClicked() {
        mainLayout.setVisibility(View.GONE);
        cropLayout.setVisibility(View.VISIBLE);
        editTitleLayoutFirst.setVisibility(View.GONE);
        editTitleLayoutSecond.setVisibility(View.VISIBLE);
        cropImageView.setImageBitmap(imageBitmap);
    }

    @OnClick(R.id.crop_done)
    public void onCropDoneClicked() {
        resetImage.setVisibility(View.VISIBLE);
        imageBitmap = cropImageView.getCroppedImage();
        imageView.setImageBitmap(imageBitmap);
        mainLayout.setVisibility(View.VISIBLE);
        cropLayout.setVisibility(View.GONE);
        editTitleLayoutFirst.setVisibility(View.VISIBLE);
        editTitleLayoutSecond.setVisibility(View.GONE);
    }

    @OnClick(R.id.crop_rotate)
    public void onCropRotateClicked() {
        if (rotateAngle > 360) {
            rotateAngle = 90;
        }
        rotateAngle += 90;
        cropImageView.setRotatedDegrees(-rotateAngle);
    }

    @OnClick(R.id.flip_horizental)
    public void onFlipHorizentalClicked() {
        cropImageView.flipImageHorizontally();
    }

    @OnClick(R.id.flip_vertical)
    public void onFlipVerticalClicked() {
        cropImageView.flipImageVertically();
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
            e.printStackTrace();
        }
        imageView.setImageBitmap(imageBitmap);
    }

    private void CheckPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permission[0]) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(getApplicationContext(), permission[1]) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permission, 1);
            }
        }
    }

    @OnClick(R.id.image)
    public void OnClickImage() {
        if (initialLoad) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckPermission();
    }

    @OnClick(R.id.reset_image)
    public void onResetImageClicked() {
        resetImage.setVisibility(View.GONE);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        imageBitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
        imageView.setImageBitmap(imageBitmap);
    }

    @OnClick(R.id.save_to_gallery)
    public void onConvertClicked() {
        if (imageBitmap != null) {
            String type = spinner.getSelectedItem().toString();
            dialog.show();
            if (type.equals("PDF")) {
                convertPDF(imageBitmap);
            } else {
                ImageConverter converter = new ImageConverter(this, type, progressValue, imageBitmap);
                converter.execute();
            }
        }
    }

    public void convertPDF(Bitmap imageBitmap) {
        try {
            File filePath = new File(android.os.Environment.getExternalStorageDirectory(), generateUniqueImageFileName() + ".jpg");
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, progressValue, fileOutputStream); //100-best quality
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
            dialog.dismiss();
            Intent intent = new Intent(this, PDFViewer.class);
            intent.putExtra("filePath", file);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void convertedImage(String fileName) {
        try {
            dialog.dismiss();
            if (fileName != null) {
                File filePath = new File(android.os.Environment.getExternalStorageDirectory(), fileName);
                Uri path = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", filePath);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.setDataAndType(path, "image/*");
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "Error Getting Image!!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initView() {
        dialog = new TransparentProgressDialog(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, imageType);
        spinner.setAdapter(adapter);
        imageQuality.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressValue = progress;
                qualityPercentage.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("onStartTrackingTouch", "start");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("onStopTrackingTouch", "stop");
                if (progressValue < 30) {
                    progressValue = 30;
                    imageQuality.setProgress(progressValue);
                    Toast.makeText(getApplicationContext(), "Minimum quality: 30%", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE:
                    pickedImage = data.getData();
                    if (pickedImage != null) {
                        initialLoad = false;
                        editTitleLayoutFirst.setVisibility(View.VISIBLE);
                        editLayoutSecond.setVisibility(View.VISIBLE);
                        imagePath = FileUtils.getPath(getApplicationContext(), pickedImage);
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        imageBitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
                        imageView.setImageBitmap(imageBitmap);
                    }
                    break;
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    if (resultCode == RESULT_OK) {
                        Uri resultUri = result.getUri();
                        imageView.setImageURI(resultUri);
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        Exception error = result.getError();
                        error.printStackTrace();
                    }
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(editTitleLayoutFirst.getVisibility()==View.VISIBLE) {
            super.onBackPressed();
        }else{
            mainLayout.setVisibility(View.VISIBLE);
            cropLayout.setVisibility(View.GONE);
            editTitleLayoutFirst.setVisibility(View.VISIBLE);
            editTitleLayoutSecond.setVisibility(View.GONE);
        }
    }
}

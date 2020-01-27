package com.wnet.imageconvertor.ui.homescreen

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View.VISIBLE
import android.view.View.GONE
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.pdf.PdfWriter
import com.luminaire.apolloar.base_class.BaseFragment
import com.wnet.imageconvertor.BuildConfig
import androidx.core.os.bundleOf

import com.wnet.imageconvertor.R
import com.wnet.imageconvertor.dialog.TransparentProgressDialog
import com.wnet.imageconvertor.ui.OnBoardActivity
import com.wnet.imageconvertor.util.AppConstants.Companion.PDF_FILE_PATH
import com.wnet.imageconvertor.util.FileUtils
import com.wnet.imageconvertor.util.ImageConverter
import com.wnet.imageconvertor.util.Utils.generateUniqueImageFileName
import kotlinx.android.synthetic.main.header.view.*
import kotlinx.android.synthetic.main.home_fragment.view.*
import java.io.File
import java.io.FileOutputStream

class HomeFragment : BaseFragment() {

    private val imageType = arrayOf("JPEG", "PNG", "PDF", "WEBP")
    private lateinit var root: View
    internal val REQUEST_IMAGE = 2
    internal var pickedImage: Uri? = null
    internal var imagePath: String? = ""
//    var imageBitmap: Bitmap? = null
    lateinit var dialog: TransparentProgressDialog
    internal var progressValue = 100
    internal var permission = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    lateinit var myactivity: OnBoardActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val activity: Activity? = activity
        if(activity is OnBoardActivity){
            myactivity = activity as OnBoardActivity
        }
        root = inflater.inflate(R.layout.home_fragment, container, false)
        val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, imageType)
        root.spinner1.setAdapter(adapter)

        dialog = TransparentProgressDialog(requireContext())
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        root.image.setOnClickListener {
            if (myactivity.initialLoad) {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                startActivityForResult(intent, REQUEST_IMAGE)
            }
        }

        root.gallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_IMAGE)
        }

        root.crop.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_cropFragment)
        }

        root.undo.setOnClickListener {
            imageRotation(true)
        }

        root.redo.setOnClickListener {
            imageRotation(false)
        }

        root.reset_image.setOnClickListener {
            myactivity.modifiedImage = false
            root.reset_image.visibility = GONE
            val bmOptions = BitmapFactory.Options()
            myactivity.imageBitmap = BitmapFactory.decodeFile(imagePath, bmOptions)
            root.image.setImageBitmap(myactivity.imageBitmap)
            root.image_quality.progress = 100
        }

        root.save_to_gallery.setOnClickListener {
            if (myactivity.imageBitmap != null) {
                val type = root.spinner1.getSelectedItem().toString()
                dialog.show()
                if (type == "PDF") {
                    convertPDF(myactivity.imageBitmap!!)
                } else {
                    val converter = ImageConverter(this, type, progressValue, myactivity.imageBitmap!!)
                    converter.execute()
                }
            }
        }

        root.image_quality.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                progressValue = progress
                root.quality_percentage.setText("$progress%")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                Log.d("onStartTrackingTouch", "start")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Log.d("onStopTrackingTouch", "stop")
                if (progressValue < 30) {
                    progressValue = 30
                    root.image_quality.setProgress(progressValue)
                    Toast.makeText(requireContext(), "Minimum quality: 30%", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE -> {
                    pickedImage = data!!.getData()
                    if (pickedImage != null) {
                        myactivity.initialLoad = false
                        root.edit_title_layout_first.visibility = VISIBLE
                        root.edit_layout_second.visibility = VISIBLE
                        root.reset_image.visibility = GONE
                        imagePath = FileUtils.getPath(requireContext(), pickedImage)
                        val bmOptions = BitmapFactory.Options()
                        myactivity.imageBitmap = BitmapFactory.decodeFile(imagePath, bmOptions)
                        root.image.setImageBitmap(myactivity.imageBitmap)
                    }
                }
            }
        }
    }

    private fun imageRotation(status: Boolean) {
        try {
            val matrix = Matrix()
            if (status) {
                matrix.postRotate(-90f)
            } else {
                matrix.postRotate(90f)
            }
            myactivity.imageBitmap = Bitmap.createBitmap(myactivity.imageBitmap!!, 0, 0, myactivity.imageBitmap!!.getWidth(), myactivity.imageBitmap!!.getHeight(), matrix, true) // rotating bitmap
        } catch (e: Exception) {
            e.printStackTrace()
        }

        root.image.setImageBitmap(myactivity.imageBitmap)
    }

    fun convertPDF(imageBitmap: Bitmap) {
        try {
            val filePath = File(Environment.getExternalStorageDirectory(), generateUniqueImageFileName() + ".jpg")
            val fileOutputStream = FileOutputStream(filePath)
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, progressValue, fileOutputStream) //100-best quality
            fileOutputStream.close()
            val document = Document()

            val file = File(Environment.getExternalStorageDirectory(), generateUniqueImageFileName() + ".pdf")
            val out = FileOutputStream(file)
            PdfWriter.getInstance(document, out) //  Change pdf's name.
            //            PdfWriter.getInstance(document, new FileOutputStream(directoryPath + "/example.pdf")); //  Change pdf's name.

            document.open()

            //            Image image = Image.getInstance(directoryPath + "/" + "example.jpg");  // Change image's name and extension.
            val image = Image.getInstance(filePath.absolutePath)  // Change image's name and extension.

            val scaler = (document.pageSize.width - document.leftMargin()
                    - document.rightMargin() - 0f) / image.width * 100 // 0 means you have no indentation. If you have any, change it.
            image.scalePercent(scaler)
            image.alignment = Image.ALIGN_CENTER or Image.ALIGN_TOP

            document.add(image)
            document.close()

            filePath.delete()
            dialog.dismiss()
            val bundle = bundleOf(PDF_FILE_PATH to file)
            findNavController().navigate(R.id.action_homeFragment_to_PDFViewFragment, bundle)
//            val intent = Intent(requireContext(), PDFViewer::class.java)
//            intent.putExtra("filePath", file)
//            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun CheckPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission[0]) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(requireContext(), permission[1]) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permission, 1)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        CheckPermission()
        if(myactivity != null && myactivity.imageBitmap !=null){
            if (myactivity.modifiedImage) {
                root.reset_image.visibility = VISIBLE
            }else{
                root.reset_image.visibility = GONE
            }
            root.edit_title_layout_first.visibility = VISIBLE
            root.edit_layout_second.visibility = VISIBLE
            root.image.setImageBitmap(myactivity.imageBitmap)
        }
    }

    fun convertedImage(fileName: String?) {
        try {
            dialog.dismiss()
            if (fileName != null) {
                val filePath = File(android.os.Environment.getExternalStorageDirectory(), fileName)
                val path = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", filePath)
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                intent.setDataAndType(path, "image/*")
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Error Getting Image!!", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

//    override fun onBackPressed() {
//        if (root.edit_title_layout_first.visibility == VISIBLE) {
//            super.onBackPressed()
//        } else {
//            mainLayout.setVisibility(VISIBLE)
//            cropLayout.setVisibility(GONE)
//            editTitleLayoutFirst.setVisibility(VISIBLE)
//            editTitleLayoutSecond.setVisibility(GONE)
//        }
//    }
}

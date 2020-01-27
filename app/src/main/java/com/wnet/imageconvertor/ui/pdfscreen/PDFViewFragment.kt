package com.wnet.imageconvertor.ui.pdfscreen

import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.wnet.imageconvertor.BuildConfig

import com.wnet.imageconvertor.R
import com.wnet.imageconvertor.util.AppConstants
import kotlinx.android.synthetic.main.pdfview_fragment.view.*
import java.io.File
import java.text.DecimalFormat

class PDFViewFragment : Fragment() {

    private lateinit var root: View
    lateinit var file: File

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        root = inflater.inflate(R.layout.pdfview_fragment, container, false)
        file = arguments?.getSerializable(AppConstants.PDF_FILE_PATH) as File
        root.filePath.setText(file.absolutePath)
        fileSize()
        root.pdfView.fromFile(file)
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
                .load()

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        root.viewPDF.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                //intent.setType( "application/pdf");
                val path = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", file)
                intent.setDataAndType(path, "application/pdf")
                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                startActivity(Intent.createChooser(intent, "Share File"))
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        root.sharePDF.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "application/pdf"
                val path = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", file)
                intent.putExtra(Intent.EXTRA_STREAM, path)
                intent.putExtra(Intent.EXTRA_SUBJECT,
                        "Sharing File...")
                intent.putExtra(Intent.EXTRA_TEXT, "Sharing File...")

                startActivity(Intent.createChooser(intent, "Share File"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fileSize() {
        var hrSize = ""
        val fileSizeInKB = file.length() / 1024.0
        val dec = DecimalFormat("0.00")

        if (fileSizeInKB > 1048576) {
            val fileSizeInGB = fileSizeInKB / 1048576.0
            hrSize += dec.format(fileSizeInGB) + " GB"
        } else if (fileSizeInKB > 1024) {
            val fileSizeInMB = fileSizeInKB / 1024.0
            hrSize += dec.format(fileSizeInMB) + " MB"
        } else {
            hrSize += dec.format(fileSizeInKB) + " KB"
        }
        root.fileSize.setText(hrSize)
    }

}

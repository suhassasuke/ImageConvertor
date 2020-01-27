package com.wnet.imageconvertor.ui.cropscreen

import android.app.Activity
import android.graphics.Bitmap
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.luminaire.apolloar.base_class.BaseFragment

import com.wnet.imageconvertor.R
import com.wnet.imageconvertor.dialog.TransparentProgressDialog
import com.wnet.imageconvertor.ui.OnBoardActivity
import kotlinx.android.synthetic.main.crop_fragment.view.*
import kotlinx.android.synthetic.main.header.view.*

class CropFragment : BaseFragment() {

    private lateinit var root: View
    lateinit var dialog: TransparentProgressDialog
    lateinit var myactivity: OnBoardActivity
    internal var rotateAngle = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val activity: Activity? = activity
        if(activity is OnBoardActivity){
            myactivity = activity as OnBoardActivity
        }
        root = inflater.inflate(R.layout.crop_fragment, container, false)

        root.edit_title_layout_first.visibility = View.GONE
        root.edit_title_layout_second.visibility = View.VISIBLE

        dialog = TransparentProgressDialog(requireContext())
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        root.cropImageView.setImageBitmap(myactivity.imageBitmap)

        root.crop_done.setOnClickListener {
            myactivity.modifiedImage = true
            myactivity.imageBitmap = root.cropImageView.getCroppedImage()
//            requireActivity().onBackPressed()
            fragmentManager!!.popBackStack()
        }

        root.flip_horizental.setOnClickListener {
            root.cropImageView.flipImageHorizontally()
        }

        root.flip_vertical.setOnClickListener {
            root.cropImageView.flipImageVertically()
        }

        root.crop_rotate.setOnClickListener {
            if (rotateAngle > 360) {
                rotateAngle = 90
            }
            rotateAngle += 90
            root.cropImageView.setRotatedDegrees(-rotateAngle)
        }
    }

}

package com.luminaire.apolloar.base_class


import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import android.content.Intent
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import android.content.DialogInterface
import android.util.Log
import com.wnet.imageconvertor.R
import com.wnet.imageconvertor.util.AppConstants.Companion.ALERT_NO
import com.wnet.imageconvertor.util.AppConstants.Companion.ALERT_YES
import java.util.ArrayList
import java.util.HashMap


/**
 * A simple [Fragment] subclass.
 *
 */
open class BaseFragment : Fragment() {


    val REQUEST_ID_MULTIPLE_PERMISSIONS = 1
    private val SPLASH_TIME_OUT = 2000
    private val TAG = "tag"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return TextView(requireContext()).apply {
            setText(R.string.hello_blank_fragment)
        }
    }

    protected fun hideSoftKeyboard(view: TextInputEditText) {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(
            view.getWindowToken(),
            InputMethodManager.RESULT_UNCHANGED_SHOWN
        );
    }

    protected fun showAlert(
        title: String,
        message: String,
        hasNegativeAction: Boolean,
        identifier: Int,
        listener: AlertActionListener
    ) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
        builder.setMessage(message)

        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            dialog.cancel()
            listener.onNavigation(identifier, ALERT_YES)
        }

        if (hasNegativeAction) {
            builder.setNegativeButton(android.R.string.no) { dialog, which ->
                dialog.cancel()
                listener.onNavigation(identifier, ALERT_NO)
            }
        }

        builder.show()
    }

    interface AlertActionListener {
        fun onNavigation(identifier: Int, to: Int)
    }

    public fun checkAndRequestPermissions(): Boolean {
        val cameraPermission =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        val writePermission =
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        val readPermission =
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )


        val listPermissionsNeeded = ArrayList<String>()

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (writePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (readPermission != PackageManager.PERMISSION_GRANTED){
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (!listPermissionsNeeded.isEmpty()) {
            requestPermissions(
                listPermissionsNeeded.toTypedArray(),
                REQUEST_ID_MULTIPLE_PERMISSIONS
            )
            return false
        }
        return true
    }

    public override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        Log.d(TAG, "Permission callback called-------")
        when (requestCode) {
            REQUEST_ID_MULTIPLE_PERMISSIONS -> {

                val perms = HashMap<String, Int>()
                // Initialize the map with both permissions
                perms[Manifest.permission.CAMERA] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] =
                    PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.READ_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                // Fill with actual results from user
                if (grantResults.size > 0) {
                    for (i in permissions.indices)
                        perms[permissions[i]] = grantResults[i]
                    // Check for both permissions
                    if ((perms[Manifest.permission.CAMERA] == PackageManager.PERMISSION_GRANTED
                                && perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                                && perms[Manifest.permission.READ_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                                )
                    ) {
                        Log.d(TAG, "sms & location services permission granted")
                        // process the normal flow
//                        val i = Intent(this@Decompress, OnBoardActivity::class.java)
//                        startActivity(i)
                        requireActivity().finish()
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ")
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                        //                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if ((ActivityCompat.shouldShowRequestPermissionRationale(
                                requireActivity(),
                                Manifest.permission.CAMERA
                            )
                                    || ActivityCompat.shouldShowRequestPermissionRationale(
                                requireActivity(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                                    || ActivityCompat.shouldShowRequestPermissionRationale(
                                requireActivity(),
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            ))
                        ) {
                            showDialogOK("Service Permissions are required for this app",
                                DialogInterface.OnClickListener { dialog, which ->
                                    when (which) {
                                        DialogInterface.BUTTON_POSITIVE -> checkAndRequestPermissions()
                                        DialogInterface.BUTTON_NEGATIVE ->
                                            // proceed with logic by disabling the related features or quit the app.
                                            requireActivity().finish()
                                    }
                                })
                        } else {
                            explain("You need to give some mandatory permissions to continue. Do you want to go to app settings?")
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }//permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                    }
                }
            }
        }

    }

    private fun showDialogOK(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", okListener)
            .create()
            .show()
    }

    private fun explain(msg: String) {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setMessage(msg)
            .setPositiveButton(
                "Yes"
            ) { paramDialogInterface, paramInt ->
                //  permissionsclass.requestPermission(type,code);
                startActivity(
                    Intent(
                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:com.luminaire.apolloar")
                    )
                )
            }
            .setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                override fun onClick(paramDialogInterface: DialogInterface, paramInt: Int) {
                   requireActivity().finish()
                }
            })
        dialog.show()
    }


}

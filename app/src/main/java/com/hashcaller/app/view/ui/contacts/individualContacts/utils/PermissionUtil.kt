package com.hashcaller.app.view.ui.contacts.individualContacts.utils

object PermissionUtil{

//    fun requestCallLogPermission(context: Context):Boolean{
//        var permissionGiven = false
//        //persmission
////         Manifest.permission.ANSWER_PHONE_CALLS, removed
//        Dexter.withContext(context)
//            .withPermissions(
//                Manifest.permission.WRITE_CALL_LOG,
//                Manifest.permission.READ_CALL_LOG
//            ).withListener(object : MultiplePermissionsListener {
//                override fun onPermissionsChecked(report: MultiplePermissionsReport?) { /* ... */
////
//                    report.let {
//                        if(report?.areAllPermissionsGranted()!!){
//                            permissionGiven = true
//
////                            Toast.makeText(applicationContext, "thank you", Toast.LENGTH_SHORT).show()
//
//                        }
//                    }
//                }
//
//                override fun onPermissionRationaleShouldBeShown(
//                    permissions: List<PermissionRequest?>?,
//                    token: PermissionToken?
//                ) { /* ... */
//                    token?.continuePermissionRequest()
////                    Toast.makeText(applicationContext, "onPermissionRationaleShouldBeShown", Toast.LENGTH_SHORT).show()
//                }
//            }).check()
//        return permissionGiven
//    }
//     fun requesetPermission(context: Context): Boolean {
//        var permissionGiven = false
//        //persmission
////         Manifest.permission.ANSWER_PHONE_CALLS, removed
//        Dexter.withContext(context)
//            .withPermissions(
//                Manifest.permission.WRITE_CONTACTS,
//                Manifest.permission.READ_CONTACTS,
//                Manifest.permission.CALL_PHONE,
//                Manifest.permission.READ_PHONE_STATE,
//                Manifest.permission.WRITE_CALL_LOG,
//                Manifest.permission.READ_CALL_LOG,
//                Manifest.permission.RECEIVE_MMS,
//                Manifest.permission.SEND_SMS,
//                Manifest.permission.READ_SMS
//
//            ).withListener(object : MultiplePermissionsListener {
//                override fun onPermissionsChecked(report: MultiplePermissionsReport?) { /* ... */
////
//                    report.let {
//                        if(report?.areAllPermissionsGranted()!!){
//                            permissionGiven = true
//
////                            Toast.makeText(applicationContext, "thank you", Toast.LENGTH_SHORT).show()
//
//                        }
//                    }
//                }
//
//                override fun onPermissionRationaleShouldBeShown(
//                    permissions: List<PermissionRequest?>?,
//                    token: PermissionToken?
//                ) { /* ... */
//                    token?.continuePermissionRequest()
////                    Toast.makeText(applicationContext, "onPermissionRationaleShouldBeShown", Toast.LENGTH_SHORT).show()
//                }
//            }).check()
//        return permissionGiven
//    }
}
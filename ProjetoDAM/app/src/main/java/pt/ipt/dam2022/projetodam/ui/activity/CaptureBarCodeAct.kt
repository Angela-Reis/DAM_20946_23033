package pt.ipt.dam2022.projetodam.ui.activity

import com.journeyapps.barcodescanner.CaptureActivity

/**
 * Activity with no xml, serves to reference a CaptureActivity in AndroidManifest
 * in order to make the screenRotation of the Barcode Scanner work with the phone sensor
 */
class CaptureBarCodeAct: CaptureActivity() {
}
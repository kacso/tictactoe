package hr.inceptum.tictactoe.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import hr.inceptum.tictactoe.R

/***
 *
 *  Dialog which indicates that long running task is executing.
 *
 */
open class ProgressDialog : DialogFragment() {
    companion object {
        fun newInstance(): ProgressDialog {
            return ProgressDialog()
        }
    }

    private lateinit var rootView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.dialog_progress, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        isCancelable = false

        return rootView.rootView
    }
}
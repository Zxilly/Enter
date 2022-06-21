package top.learningman.enter.fragment.setup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import top.learningman.enter.activity.SetupActivity
import top.learningman.enter.databinding.FragmentAccessibilityBinding
import top.learningman.enter.utils.AccessibilityCheck
import top.learningman.enter.viewModel.AccessibilityFragmentViewModel

class AccessibilityFragment : Fragment() {

    companion object {
        fun newInstance() = AccessibilityFragment()

        fun on(context: Context): Boolean {
            return !AccessibilityCheck.isAccessibilitySettingsOn(context)
        }
    }

    private lateinit var viewModel: AccessibilityFragmentViewModel
    private lateinit var binding: FragmentAccessibilityBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentAccessibilityBinding.inflate(inflater, container, false).apply {
            binding = this
            viewModel =
                ViewModelProvider(this@AccessibilityFragment)[AccessibilityFragmentViewModel::class.java]

            accessibility.setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                Toast.makeText(
                    requireContext(),
                    "Please enable accessibility for Enter",
                    Toast.LENGTH_LONG
                ).show()
                viewModel.tryGrant()
            }
        }.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as SetupActivity).setHeader("Require Accessibility Service")
    }

    override fun onResume() {
        super.onResume()
        if (on(requireContext())) {
            if (viewModel.haveTryGrant()) {
                Toast.makeText(
                    requireContext(),
                    "Failed to grant permission.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                requireContext(),
                "Permission granted.",
                Toast.LENGTH_SHORT
            ).show()
            binding.accessibility.isEnabled = false
            (requireActivity() as SetupActivity).pass()
        }
    }

}
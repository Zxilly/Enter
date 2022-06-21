package top.learningman.enter.fragment.setup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import top.learningman.enter.activity.SetupActivity
import top.learningman.enter.databinding.FragmentPermissionBinding
import top.learningman.enter.viewModel.PermissionFragmentViewModel

class PermissionFragment : Fragment() {

    companion object {
        fun newInstance() = PermissionFragment()

        fun on(context: Context): Boolean {
            return !Settings.canDrawOverlays(context)
        }
    }

    private lateinit var viewModel: PermissionFragmentViewModel
    private lateinit var binding: FragmentPermissionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentPermissionBinding.inflate(inflater, container, false).apply {
            binding = this
            viewModel =
                ViewModelProvider(this@PermissionFragment)[PermissionFragmentViewModel::class.java]

            grant.setOnClickListener {
                Toast.makeText(
                    requireContext(),
                    "Please grant overlay permission to Enter.",
                    Toast.LENGTH_LONG
                ).show()
                // grant overlay permission
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.data = Uri.parse("package:${requireContext().packageName}")
                startActivity(intent)
                viewModel.tryGrant()
            }
        }.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as SetupActivity).setHeader("Require Overlay Permission")
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
            binding.grant.isEnabled = false
            (requireActivity() as SetupActivity).pass()
        }
    }

}
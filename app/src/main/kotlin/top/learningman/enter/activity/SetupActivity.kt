package top.learningman.enter.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.android.setupwizardlib.view.NavigationBar
import top.learningman.enter.databinding.ActivitySetupBinding
import top.learningman.enter.fragment.setup.AccessibilityFragment
import top.learningman.enter.fragment.setup.PermissionFragment

class SetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding.viewPager) {
            adapter = PagerAdapter(this@SetupActivity)
            offscreenPageLimit = 1
            isUserInputEnabled = false
            registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    val target = supportFragmentManager.findFragmentByTag("f$position")
                    when (target) {
                        is AccessibilityFragment -> {
                            binding.setup.headerText = "Require Accessibility Permission"
                        }
                        is PermissionFragment -> {
                            binding.setup.headerText = "Require Overlay Permission"
                        }
                        else -> throw IllegalStateException("Unknown fragment")
                    }
                }
            })
        }

        with(binding.setup) {
            navigationBar.backButton.visibility = View.GONE
            navigationBar.nextButton.isEnabled = false
            navigationBar.setNavigationBarListener(object : NavigationBar.NavigationBarListener {
                override fun onNavigateBack() {
                }

                override fun onNavigateNext() {
                    if (binding.viewPager.currentItem == 1 || binding.viewPager.adapter!!.itemCount == 1) {
                        startActivity(Intent(this@SetupActivity, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                        finish()
                    } else {
                        binding.viewPager.currentItem = 1
                        navigationBar.nextButton.isEnabled = false
                    }
                }
            })
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onResume() {
        super.onResume()
    }

    fun pass() {
        binding.setup.navigationBar.nextButton.isEnabled = true
    }

    class PagerAdapter(private val fa: FragmentActivity) : FragmentStateAdapter(fa) {
        private var itemCount: Int = 0

        init {
            if (AccessibilityFragment.on(fa)) {
                itemCount++
            }
            if (PermissionFragment.on(fa)) {
                itemCount++
            }
        }

        override fun getItemCount(): Int {
            return itemCount
        }

        override fun createFragment(position: Int): Fragment {
            return when (itemCount) {
                2 -> {
                    when (position) {
                        0 -> PermissionFragment.newInstance()
                        1 -> AccessibilityFragment.newInstance()
                        else -> throw IllegalArgumentException("Invalid position: $position")
                    }
                }
                1 -> {
                    when {
                        AccessibilityFragment.on(fa) -> return AccessibilityFragment()
                        PermissionFragment.on(fa) -> return PermissionFragment()
                        else -> throw IllegalArgumentException("Invalid position: $position")
                    }
                }
                else -> throw IllegalStateException("itemCount is $itemCount")
            }
        }
    }
}
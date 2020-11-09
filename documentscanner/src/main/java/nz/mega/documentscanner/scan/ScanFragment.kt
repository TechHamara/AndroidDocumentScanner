package nz.mega.documentscanner.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import nz.mega.documentscanner.DocumentScannerViewModel
import nz.mega.documentscanner.R
import nz.mega.documentscanner.data.PageItem
import nz.mega.documentscanner.databinding.FragmentScanBinding
import nz.mega.documentscanner.utils.DialogFactory
import nz.mega.documentscanner.utils.ViewUtils.scrollToLastPosition
import nz.mega.documentscanner.view.OffsetPageTransformer

class ScanFragment : Fragment() {

    companion object {
        private const val TAG = "ScanFragment"
    }

    private val navigationArguments: ScanFragmentArgs by navArgs()
    private val viewModel: DocumentScannerViewModel by activityViewModels()
    private val adapter: ScanPagerAdapter by lazy { ScanPagerAdapter() }
    private val viewPagerCallback: ViewPager2.OnPageChangeCallback by lazy {
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.setCurrentPagePosition(position)
            }
        }
    }

    private var scrolled = false
    private lateinit var binding: FragmentScanBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupObservers()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) { showDiscardDialog() }
    }

    override fun onDestroyView() {
        binding.viewPager.unregisterOnPageChangeCallback(viewPagerCallback)
        super.onDestroyView()
    }

    private fun setupView() {
        val pageMargin = resources.getDimensionPixelOffset(R.dimen.scan_page_margin)
        val pageOffset = resources.getDimensionPixelOffset(R.dimen.scan_page_offset)

        binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.setPageTransformer(OffsetPageTransformer(pageOffset, pageMargin))
        binding.viewPager.registerOnPageChangeCallback(viewPagerCallback)
        binding.viewPager.adapter = adapter
        binding.btnBack.setOnClickListener { showDiscardDialog() }
        binding.btnAdd.setOnClickListener { navigateBack() }
        binding.btnRotate.setOnClickListener { viewModel.rotatePage() }
        binding.btnDelete.setOnClickListener {
            DialogFactory.createDeleteCurrentScanDialog(requireContext()) {
                viewModel.deletePage()
            }.show()
        }
        binding.btnCrop.setOnClickListener { findNavController().navigate(ScanFragmentDirections.actionScanFragmentToCropFragment()) }
        binding.btnDone.setOnClickListener { findNavController().navigate(ScanFragmentDirections.actionScanFragmentToSaveFragment()) }
        binding.btnRetake.setOnClickListener {
            viewModel.deletePage()
            navigateBack()
        }
    }

    private fun setupObservers() {
        viewModel.getDocumentTitle().observe(viewLifecycleOwner, ::showDocumentTitle)
        viewModel.getDocumentPages(requireContext()).observe(viewLifecycleOwner, ::showPages)
        viewModel.getCurrentPagePosition().observe(viewLifecycleOwner, ::showPageCount)
    }

    private fun showDocumentTitle(title: String) {
        binding.txtScanTitle.text = title
    }

    private fun showPages(items: List<PageItem>) {
        val currentPosition = viewModel.getCurrentPagePosition().value ?: 0
        binding.btnDelete.isVisible = items.size > 1
        adapter.submitList(items)

        if (items.isNotEmpty()) {
            binding.viewPager.post {
                if (!scrolled && navigationArguments.scrollToLast) {
                    binding.viewPager.scrollToLastPosition()
                    scrolled = true
                } else if (binding.viewPager.currentItem != currentPosition) {
                    binding.viewPager.currentItem = currentPosition
                }
            }
        } else {
            navigateBack()
        }
    }

    private fun showPageCount(currentPosition: Int) {
        binding.txtPageCount.text = String.format(
            getString(R.string.scan_format_page_count),
            currentPosition + 1,
            viewModel.getPagesCount()
        )
    }

    private fun showDiscardDialog() {
        val pagesCount = viewModel.getPagesCount()

        when {
            pagesCount == 1 -> {
                DialogFactory.createDiscardScanDialog(requireContext()) {
                    viewModel.deletePage()
                    navigateBack()
                }.show()
            }
            pagesCount > 1 -> {
                DialogFactory.createDiscardScansDialog(requireContext()) {
                    viewModel.deleteAllPages()
                    navigateBack()
                }.show()
            }
            else -> navigateBack()
        }
    }

    private fun navigateBack() {
        findNavController().popBackStack(R.id.cameraFragment, false)
    }
}

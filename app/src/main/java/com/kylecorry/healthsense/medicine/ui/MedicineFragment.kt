package com.kylecorry.healthsense.medicine.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.kylecorry.healthsense.R
import com.kylecorry.healthsense.databinding.FragmentMedicineBinding
import com.kylecorry.healthsense.databinding.ListItemMedicineBinding
import com.kylecorry.healthsense.heart.domain.BloodPressureReading
import com.kylecorry.healthsense.medicine.domain.Frequency
import com.kylecorry.healthsense.medicine.domain.Medicine
import com.kylecorry.healthsense.medicine.domain.MedicineFoodRequirement
import com.kylecorry.healthsense.medicine.domain.TimeOfDay
import com.kylecorry.healthsense.medicine.infrastructure.MedicineRepo
import com.kylecorry.trailsensecore.infrastructure.system.UiUtils
import com.kylecorry.trailsensecore.infrastructure.view.ListView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek

class MedicineFragment : Fragment() {

    private var _binding: FragmentMedicineBinding? = null
    private val binding get() = _binding!!

    private val medicineRepo by lazy { MedicineRepo.getInstance(requireContext().applicationContext) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listView = ListView<Medicine>(
            binding.medicineList,
            R.layout.list_item_medicine
        ) { medicineView, medicine ->
            val itemBinding = ListItemMedicineBinding.bind(medicineView)
            itemBinding.medicineName.text = medicine.name
            itemBinding.medicineDosage.text = medicine.dosage ?: ""
            medicineView.setOnLongClickListener {
                UiUtils.alertWithCancel(
                    requireContext(),
                    getString(R.string.delete_medicine),
                    medicine.name,
                    getString(R.string.dialog_ok),
                    getString(R.string.dialog_cancel)
                ) { cancelled ->
                    if (!cancelled) {
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                medicineRepo.delete(medicine)
                            }
                        }
                    }
                }
                true
            }
        }

        medicineRepo.getAll().observe(viewLifecycleOwner, Observer { listView.setData(it) })
    }


    // TODO: Allow the user to store all medicines they take
    // TODO: Allow user to schedule a reminder for each medicine with frequency (for MVP, every X days at a list of times)
    // TODO: Allow the user to pick predefined times (ex. Morning, Midday, Afternoon, Night)
    // TODO: Allow the user to specify dosages
    // TODO: Allow the user to specify whether they can take without food
}
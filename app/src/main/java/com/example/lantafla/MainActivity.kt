package com.example.lantafla

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lantafla.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var adapter: PaymentTableAdapter

    private var currentTab = 0
    private var inputExpanded = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs   = getPreferences(MODE_PRIVATE)
        adapter = PaymentTableAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter       = adapter

        restorePrefs()
        setupTabs()
        binding.btnCalculate.setOnClickListener { calculate() }
        binding.btnToggleInput.setOnClickListener { toggleInput() }
    }

    // ── tabs ──────────────────────────────────────────────────────────────────

    private fun setupTabs() {
        val chips = listOf(binding.chipJgOv, binding.chipJgVt, binding.chipJaOv, binding.chipJaVt)
        chips.forEachIndexed { i, chip ->
            chip.setOnClickListener {
                currentTab = i
                chips.forEach { it.isChecked = false }
                chip.isChecked = true
                recalculate()
            }
        }
        chips[currentTab].isChecked = true
    }

    // ── collapse / expand input card ──────────────────────────────────────────

    private fun toggleInput() {
        inputExpanded = !inputExpanded
        binding.inputCard.visibility = if (inputExpanded) View.VISIBLE else View.GONE
        binding.btnToggleInput.text = getString(
            if (inputExpanded) R.string.btn_toggle_hide else R.string.btn_toggle_show
        )
    }

    // ── calculation ───────────────────────────────────────────────────────────

    private fun calculate() {
        savePrefs()
        recalculate()
    }

    private fun recalculate() {
        val amount  = binding.etAmount.text.toString().replace(".", "").replace(",", "").toDoubleOrNull()
        val months  = binding.etMonths.text.toString().toIntOrNull()
        val rate    = binding.etRate.text.toString().replace(",", ".").toDoubleOrNull()
        val inflPct = binding.etInflation.text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0

        if (amount == null || amount <= 0 || months == null || months < 1 || rate == null) {
            Toast.makeText(this, getString(R.string.error_input), Toast.LENGTH_SHORT).show()
            return
        }

        val lantokupct   = binding.etLantokukostnadur.text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0
        val skjalarerd   = binding.etSkjalarerd.text.toString().replace(".", "").replace(",", "").toLongOrNull() ?: 0L
        val greidslugald = binding.etGreidslugald.text.toString().replace(".", "").replace(",", "").toLongOrNull() ?: 0L

        val effectivePrincipal = amount * (1.0 + lantokupct / 100.0) + skjalarerd.toDouble()

        var rows = when (currentTab) {
            0 -> LoanCalculator.jafngreidslur(effectivePrincipal, months, rate)
            1 -> LoanCalculator.jafngreidslurVerdtryggt(effectivePrincipal, months, rate, inflPct)
            2 -> LoanCalculator.jafnarAfborganir(effectivePrincipal, months, rate)
            else -> LoanCalculator.jafnarAfborganirVerdtryggt(effectivePrincipal, months, rate, inflPct)
        }

        if (greidslugald > 0L) {
            rows = rows.map { it.copy(payment = it.payment + greidslugald) }
        }

        val indexed = currentTab == 1 || currentTab == 3
        adapter.showInflation = indexed
        binding.tableHeader.colInflation.visibility = if (indexed) View.VISIBLE else View.GONE
        adapter.submitList(rows)

        val totalPmt  = rows.sumOf { it.payment }
        val totalInt  = rows.sumOf { it.interest }
        val totalInfl = rows.sumOf { it.inflation }
        binding.tvStatus.text = buildString {
            append(getString(R.string.status_total_payment, PaymentTableAdapter.fmt(totalPmt)))
            append("  •  ")
            append(getString(R.string.status_total_interest, PaymentTableAdapter.fmt(totalInt)))
            if (indexed && totalInfl > 0) {
                append("  •  ")
                append(getString(R.string.status_total_inflation, PaymentTableAdapter.fmt(totalInfl)))
            }
        }

        // Show toggle after first successful calculation
        binding.btnToggleInput.visibility = View.VISIBLE
    }

    // ── prefs ─────────────────────────────────────────────────────────────────

    private fun restorePrefs() {
        binding.etAmount.setText(prefs.getString("amount", ""))
        binding.etMonths.setText(prefs.getString("months", ""))
        binding.etRate.setText(prefs.getString("rate", ""))
        binding.etInflation.setText(prefs.getString("inflation", ""))
        binding.etLantokukostnadur.setText(prefs.getString("lantokupct", ""))
        binding.etSkjalarerd.setText(prefs.getString("skjalarerd", ""))
        binding.etGreidslugald.setText(prefs.getString("greidslugald", ""))
    }

    private fun savePrefs() {
        prefs.edit()
            .putString("amount",      binding.etAmount.text.toString())
            .putString("months",      binding.etMonths.text.toString())
            .putString("rate",        binding.etRate.text.toString())
            .putString("inflation",   binding.etInflation.text.toString())
            .putString("lantokupct",  binding.etLantokukostnadur.text.toString())
            .putString("skjalarerd",  binding.etSkjalarerd.text.toString())
            .putString("greidslugald", binding.etGreidslugald.text.toString())
            .apply()
    }
}

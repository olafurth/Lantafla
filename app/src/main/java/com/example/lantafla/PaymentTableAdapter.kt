package com.example.lantafla

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lantafla.databinding.ItemPaymentRowBinding
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class PaymentTableAdapter : ListAdapter<PaymentRow, PaymentTableAdapter.RowHolder>(Diff()) {

    var showInflation: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            notifyItemRangeChanged(0, itemCount)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowHolder {
        val inf = LayoutInflater.from(parent.context)
        return RowHolder(ItemPaymentRowBinding.inflate(inf, parent, false))
    }

    override fun onBindViewHolder(holder: RowHolder, position: Int) {
        holder.bind(getItem(position), showInflation)
    }

    inner class RowHolder(private val b: ItemPaymentRowBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(row: PaymentRow, inflation: Boolean) {
            b.colMonth.text     = row.month.toString()
            b.colPayment.text   = fmt(row.payment)
            b.colInterest.text  = fmt(row.interest)
            b.colPrincipal.text = fmt(row.principal)
            b.colInflation.text = fmt(row.inflation)
            b.colBalance.text   = fmt(row.balance)
            b.colInflation.visibility = visOf(inflation)
        }
    }

    companion object {
        private val symbols = DecimalFormatSymbols(Locale.ROOT).apply { groupingSeparator = '.' }
        private val numFmt  = DecimalFormat("#,###", symbols)

        fun fmt(amount: Long): String = numFmt.format(amount)

        fun visOf(show: Boolean) =
            if (show) android.view.View.VISIBLE else android.view.View.GONE
    }

    private class Diff : DiffUtil.ItemCallback<PaymentRow>() {
        override fun areItemsTheSame(a: PaymentRow, b: PaymentRow) = a.month == b.month
        override fun areContentsTheSame(a: PaymentRow, b: PaymentRow) = a == b
    }
}

package com.example.lantafla

import kotlin.math.pow

object LoanCalculator {

    private fun annuityPayment(balance: Double, r: Double, remaining: Int): Double =
        if (r == 0.0) balance / remaining
        else balance * r / (1.0 - (1.0 + r).pow(-remaining))

    private fun monthlyInflation(annualPct: Double): Double =
        (1.0 + annualPct / 100.0).pow(1.0 / 12.0) - 1.0

    private fun row(k: Int, pmt: Double, interest: Double, principal: Double,
                    verdbaetur: Double, balance: Double) = PaymentRow(
        month     = k,
        payment   = pmt.toLong(),
        interest  = interest.toLong(),
        principal = principal.toLong(),
        inflation = verdbaetur.toLong(),
        balance   = balance.coerceAtLeast(0.0).toLong()
    )

    // ── 1. Óverðtryggt jafngreiðslulán ───────────────────────────────────────

    fun jafngreidslur(principal: Double, months: Int, annualRatePct: Double): List<PaymentRow> {
        val r   = annualRatePct / 100.0 / 12.0
        val pmt = annuityPayment(principal, r, months)
        var balance = principal
        return (1..months).map { k ->
            val vextir  = balance * r
            val afborgun = pmt - vextir
            balance -= afborgun
            row(k, pmt, vextir, afborgun, 0.0, balance)
        }
    }

    // ── 2. Óverðtryggt jafnar afborganir ─────────────────────────────────────

    fun jafnarAfborganir(principal: Double, months: Int, annualRatePct: Double): List<PaymentRow> {
        val r       = annualRatePct / 100.0 / 12.0
        val afborgun = principal / months
        var balance  = principal
        return (1..months).map { k ->
            val vextir = balance * r
            val pmt    = afborgun + vextir
            balance -= afborgun
            row(k, pmt, vextir, afborgun, 0.0, balance)
        }
    }

    // ── 3. Verðtryggt jafngreiðslulán ────────────────────────────────────────
    //
    // Nominal monthly rate = real rate + monthly inflation.
    // Payment is FIXED in nominal terms (jafnar greiðslur = equal payments).
    // Total cash payment = vextir + verðbætur + afborgun.

    fun jafngreidslurVerdtryggt(
        principal: Double, months: Int,
        annualRatePct: Double, annualInflationPct: Double
    ): List<PaymentRow> {
        val r   = annualRatePct / 100.0 / 12.0
        val m   = monthlyInflation(annualInflationPct)
        val rn  = r + m                                  // nominal monthly rate
        val pmt = annuityPayment(principal, rn, months)  // fixed each month
        var balance = principal
        return (1..months).map { k ->
            val verdbaetur = balance * m
            val vextir     = balance * r
            val afborgun   = pmt - verdbaetur - vextir   // = pmt - balance * rn
            balance       -= afborgun
            row(k, pmt, vextir, afborgun, verdbaetur, balance)
        }
    }

    // ── 4. Verðtryggt jafnar afborganir ──────────────────────────────────────
    //
    // Afborgun is FIXED at principal/months in nominal terms.
    // Balance decreases linearly; total payment decreases each month.
    // Total cash payment = vextir + verðbætur + afborgun.

    fun jafnarAfborganirVerdtryggt(
        principal: Double, months: Int,
        annualRatePct: Double, annualInflationPct: Double
    ): List<PaymentRow> {
        val r        = annualRatePct / 100.0 / 12.0
        val m        = monthlyInflation(annualInflationPct)
        val afborgun = principal / months                // fixed nominal afborgun
        var balance  = principal
        return (1..months).map { k ->
            val verdbaetur = balance * m
            val vextir     = balance * r
            val pmt        = afborgun + verdbaetur + vextir
            balance       -= afborgun
            row(k, pmt, vextir, afborgun, verdbaetur, balance)
        }
    }
}

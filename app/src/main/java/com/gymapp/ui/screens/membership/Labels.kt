package com.gymapp.ui.screens.membership

import androidx.annotation.StringRes
import com.gymapp.R
import com.gymapp.data.model.PaymentMethod
import com.gymapp.data.model.PlanType
import java.util.Locale

@StringRes
fun PlanType.labelRes(): Int = when (this) {
    PlanType.DAILY -> R.string.plan_daily
    PlanType.WEEKLY -> R.string.plan_weekly
    PlanType.MONTHLY -> R.string.plan_monthly
    PlanType.QUARTERLY -> R.string.plan_quarterly
    PlanType.HALF_YEAR -> R.string.plan_half_year
    PlanType.YEARLY -> R.string.plan_yearly
}

@StringRes
fun PaymentMethod.labelRes(): Int = when (this) {
    PaymentMethod.ABA_KHQR -> R.string.pay_aba
    PaymentMethod.ACLEDA -> R.string.pay_acleda
    PaymentMethod.WING -> R.string.pay_wing
    PaymentMethod.CASH -> R.string.pay_cash
}

/** Money is always rendered with Latin digits and two decimals (USD is the gym currency). */
fun money(usd: Double): String = String.format(Locale.US, "$%.2f", usd)

package com.sosobro.sosomonenote.ui.account

import com.sosobro.sosomonenote.database.AccountEntity

data class AccountListItem(
    val type: Int,
    val title: String? = null,
    val account: AccountEntity? = null
) {
    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ACCOUNT = 1
    }
}

package ch.verein.whosintoday.domain

internal data class Member(val id: Long, val name: String, val active: Boolean = true)

package de.kyrohpaneup.abirechner.data.utils

data class StringIDClass(
    val id: String,
    val text: String
) {
    override fun toString(): String {
        return text
    }
}
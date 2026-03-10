package de.kyrohpaneup.abirechner.data.utils

data class DoubleIDClass(
    val id: Double,
    val text: String
) {
    override fun toString(): String {
        return text
    }
}
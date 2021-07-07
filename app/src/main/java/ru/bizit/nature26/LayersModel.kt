package ru.bizit.nature26

data class Layer(
    var id: Int,
    var group: Int? = null,
    var name: String,
    var type: String?,
    var url: String?,
    var color: String?,
    var commonName: String,
    var commonDescription: String?,
    var symbol: String?,
    var icon: String?,
    var layers: MutableList<Layer>? = null,
    var isGroup: Boolean,
    var cluster: Boolean,
    var visible: Boolean = false
)

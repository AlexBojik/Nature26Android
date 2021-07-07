package ru.bizit.nature26

class UserMessage(
        var images: List<Image>,
        var text: String,
        var lat: Double,
        var lon: Double,
        var token: String
)

class Image(
        var jpeg: String
)


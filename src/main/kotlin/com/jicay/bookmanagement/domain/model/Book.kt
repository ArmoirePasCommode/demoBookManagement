package com.jicay.bookmanagement.domain.model

data class Book(val name: String, val author: String, val reserved: Boolean = false) {
    init {
        require(name.isNotBlank()) { "Name must not be blank" }
        require(author.isNotBlank()) { "Author must not be blank" }
    }
}

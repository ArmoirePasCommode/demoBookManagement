package com.jicay.bookmanagement.infrastructure.driven.adapter

import com.jicay.bookmanagement.domain.model.Book
import com.jicay.bookmanagement.domain.port.BookPort
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service

@Service
class BookDAO(private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : BookPort {
    override fun getAllBooks(): List<Book> {
        return namedParameterJdbcTemplate.query("SELECT * FROM BOOK", MapSqlParameterSource()) { rs, _ ->
            Book(
                name = rs.getString("title"),
                author = rs.getString("author"),
                reserved = rs.getBoolean("reserved")
            )
        }
    }

    override fun createBook(book: Book) {
        namedParameterJdbcTemplate.update(
            "INSERT INTO BOOK (title, author, reserved) values (:title, :author, :reserved)",
            mapOf(
                "title" to book.name,
                "author" to book.author,
                "reserved" to book.reserved
            )
        )
    }

    override fun reserveBook(name: String) {
        val affectedRows = namedParameterJdbcTemplate.update(
            "UPDATE BOOK SET reserved = true WHERE title = :title AND reserved = false",
            mapOf("title" to name)
        )
        if (affectedRows == 0) {
            throw IllegalStateException("Book is already reserved")
        }
    }
}
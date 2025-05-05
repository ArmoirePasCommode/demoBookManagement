package com.jicay.bookmanagement.domain.usecase

import com.jicay.bookmanagement.domain.model.Book
import com.jicay.bookmanagement.domain.port.BookPort
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import io.kotest.assertions.throwables.shouldThrow

class BookUseCaseTest : FunSpec({

    val bookPort = mockk<BookPort>()
    val bookUseCase = BookUseCase(bookPort)

    test("get all books should returns all books sorted by name") {
        every { bookPort.getAllBooks() } returns listOf(
            Book("Les Misérables", "Victor Hugo"),
            Book("Hamlet", "William Shakespeare")
        )

        val res = bookUseCase.getAllBooks()

        res.shouldContainExactly(
            Book("Hamlet", "William Shakespeare"),
            Book("Les Misérables", "Victor Hugo")
        )
    }

    test("add book") {
        justRun { bookPort.createBook(any()) }

        val book = Book("Les Misérables", "Victor Hugo")

        bookUseCase.addBook(book)

        verify(exactly = 1) { bookPort.createBook(book) }
    }

    test("get all books should returns all books sorted by name") {
        every { bookPort.getAllBooks() } returns listOf(
            Book("Harry Potter", "J.K. Rowling"),
            Book("Hamlet", "William Shakespeare")
        )

        val res = bookUseCase.getAllBooks()

        res.shouldContainExactly(
            Book("Hamlet", "William Shakespeare"),
            Book("Harry Potter", "J.K. Rowling")
        )
    }

    test("add book") {
        justRun { bookPort.createBook(any()) }

        val book = Book("Harry Potter", "J.K. Rowling")

        bookUseCase.addBook(book)

        verify(exactly = 1) { bookPort.createBook(book) }
    }
    test("reserveBook should reserve a book if it is available") {
        val book = Book("Les Misérables", "Victor Hugo", reserved = false)
        every { bookPort.getAllBooks() } returns listOf(book)
        justRun { bookPort.reserveBook("Les Misérables") }

        bookUseCase.reserveBook("Les Misérables")

        verify(exactly = 1) { bookPort.reserveBook("Les Misérables") }
    }

    test("reserveBook should throw an exception if the book is already reserved") {
        val book = Book("Les Misérables", "Victor Hugo", reserved = true)
        every { bookPort.getAllBooks() } returns listOf(book)

        shouldThrow<IllegalStateException> {
            bookUseCase.reserveBook("Les Misérables")
        }
    }

    test("reserveBook should throw an exception if the book does not exist") {
        every { bookPort.getAllBooks() } returns emptyList()

        shouldThrow<IllegalArgumentException> {
            bookUseCase.reserveBook("Unknown Book")
        }
    }
})
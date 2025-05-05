package com.jicay.bookmanagement.infrastructure.driven.adapter

import com.jicay.bookmanagement.domain.model.Book
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import io.kotest.assertions.throwables.shouldThrow
import java.sql.ResultSet

@SpringBootTest
@ActiveProfiles("testIntegration")
class BookDAOIT(
    private val bookDAO: BookDAO
) : FunSpec() {
    init {
        extension(SpringExtension)

        beforeTest {
            performQuery(
                // language=sql
                "DELETE FROM book"
            )
        }

        test("get all books from db") {
            performQuery(
                // language=sql
                """
               insert into book (title, author)
               values 
                   ('Hamlet', 'Shakespeare'),
                   ('Les fleurs du mal', 'Beaudelaire'),
                   ('Harry Potter', 'Rowling');
            """.trimIndent()
            )

            val res = bookDAO.getAllBooks()

            res.shouldContainExactlyInAnyOrder(
                Book("Hamlet", "Shakespeare"),
                Book("Les fleurs du mal", "Beaudelaire"),
                Book("Harry Potter", "Rowling")
            )
        }

        test("create book in db") {
            bookDAO.createBook(Book("Les misérables", "Victor Hugo"))

            val res = performQuery(
                // language=sql
                "SELECT * from book"
            )

            res shouldHaveSize 1
            assertSoftly(res.first()) {
                this["id"].shouldNotBeNull().shouldBeInstanceOf<Int>()
                this["title"].shouldBe("Les misérables")
                this["author"].shouldBe("Victor Hugo")
            }
        }

        test("reserve a book in db") {
            performQuery(
                // language=sql
                """
                INSERT INTO book (title, author, reserved)
                VALUES ('Les Misérables', 'Victor Hugo', false);
                """.trimIndent()
            )

            bookDAO.reserveBook("Les Misérables")

            val res = performQuery(
                // language=sql
                "SELECT reserved FROM book WHERE title = 'Les Misérables';"
            )

            res.first()["reserved"] shouldBe true
        }

        test("reserveBook should throw an exception if the book is already reserved") {
            // Insert the book as available
            performQuery(
                // language=sql
                """
                INSERT INTO book (title, author, reserved)
                VALUES ('Les Misérables', 'Victor Hugo', false);
                """.trimIndent()
            )
            // First reservation should succeed
            bookDAO.reserveBook("Les Misérables")
            // Second reservation should fail
            shouldThrow<IllegalStateException> {
                bookDAO.reserveBook("Les Misérables")
            }
        }

        afterSpec {
            container.stop()
        }
    }

    companion object {
        private val container = PostgreSQLContainer<Nothing>("postgres:13-alpine")

        init {
            container.start()
            System.setProperty("spring.datasource.url", container.jdbcUrl)
            System.setProperty("spring.datasource.username", container.username)
            System.setProperty("spring.datasource.password", container.password)
        }

        private fun ResultSet.toList(): List<Map<String, Any>> {
            val md = this.metaData
            val columns = md.columnCount
            val rows: MutableList<Map<String, Any>> = ArrayList()
            while (this.next()) {
                val row: MutableMap<String, Any> = HashMap(columns)
                for (i in 1..columns) {
                    row[md.getColumnName(i)] = this.getObject(i)
                }
                rows.add(row)
            }
            return rows
        }

        fun performQuery(sql: String): List<Map<String, Any>> {
            val hikariConfig = com.zaxxer.hikari.HikariConfig()
            hikariConfig.jdbcUrl = container.jdbcUrl
            hikariConfig.username = container.username
            hikariConfig.password = container.password
            hikariConfig.driverClassName = container.driverClassName

            val ds = com.zaxxer.hikari.HikariDataSource(hikariConfig)

            val statement = ds.connection.createStatement()
            statement.execute(sql)
            val resultSet = statement.resultSet
            return resultSet?.toList() ?: listOf()
        }
    }
}
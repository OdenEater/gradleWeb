// This file is part of the example project.
package org.example.controller;

import org.example.entity.Book;
import org.example.repository.BookRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// This file is Controller class for Book
@RestController
public class BookController {

    private final BookRepository bookRepository;

    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping("books")
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @GetMapping("books/add")
    public String addBook() {
        Book book = new Book();
        book.setTitle("Sample Book");
        book.setAuthor("Sample Author");
        bookRepository.save(book);
        return "Book added successfully!";
    }
}

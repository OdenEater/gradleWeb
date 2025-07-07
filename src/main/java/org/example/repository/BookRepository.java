package org.example.repository;
// This file is Repository interface for Book
import org.example.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
    // Additional query methods can be defined here if needed
}

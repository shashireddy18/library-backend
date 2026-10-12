package com.library.service;

import com.library.entity.Book;
import com.library.exception.BadRequestException;
import com.library.exception.ConflictException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.IssueRecordRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {
    private final BookRepository books;
    private final IssueRecordRepository issues;

    public BookService(BookRepository books, IssueRecordRepository issues) {
        this.books = books;
        this.issues = issues;
    }

    public List<Book> list(String q) {
        if (q == null || q.isBlank()) {
            return books.findAll();
        }
        String s = q.trim();
        return books.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(s, s, s);
    }

    public Book get(Long id) {
        return books.findById(id).orElseThrow(() -> new ResourceNotFoundException("Book not found"));
    }

    @Transactional
    public Book create(Book b) {
        if (books.existsByIsbn(b.getIsbn().trim())) {
            throw new ConflictException("A book with this ISBN already exists");
        }
        b.setId(null);
        b.setIsbn(b.getIsbn().trim());
        b.setAvailableCopies(b.getTotalCopies());
        return books.save(b);
    }

    @Transactional
    public Book update(Long id, Book in) {
        Book b = get(id);
        if (books.existsByIsbnAndIdNot(in.getIsbn().trim(), id)) {
            throw new ConflictException("A book with this ISBN already exists");
        }
        int issuedNow = b.getTotalCopies() - b.getAvailableCopies();
        if (in.getTotalCopies() < issuedNow) {
            throw new BadRequestException("Total copies cannot be less than copies currently issued (" + issuedNow + ")");
        }
        b.setTitle(in.getTitle());
        b.setAuthor(in.getAuthor());
        b.setIsbn(in.getIsbn().trim());
        b.setCategory(in.getCategory());
        b.setTotalCopies(in.getTotalCopies());
        b.setAvailableCopies(in.getTotalCopies() - issuedNow);
        return books.save(b);
    }

    @Transactional
    public void delete(Long id) {
        Book b = get(id);
        if (issues.existsByBookId(id)) {
            throw new ConflictException("Cannot delete: this book has issue history");
        }
        books.delete(b);
    }
}

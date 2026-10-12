package com.library.config;

import com.library.entity.AppUser;
import com.library.entity.Book;
import com.library.entity.Student;
import com.library.repository.AppUserRepository;
import com.library.repository.BookRepository;
import com.library.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    private final AppUserRepository users;
    private final BookRepository books;
    private final StudentRepository students;
    private final PasswordEncoder encoder;

    @Value("${app.admin.username}")
    private String adminUsername;
    @Value("${app.admin.password}")
    private String adminPassword;

    public DataSeeder(AppUserRepository users, BookRepository books, StudentRepository students,
                      PasswordEncoder encoder) {
        this.users = users;
        this.books = books;
        this.students = students;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (users.findByUsername(adminUsername).isEmpty()) {
            AppUser u = new AppUser();
            u.setUsername(adminUsername);
            u.setPassword(encoder.encode(adminPassword));
            u.setFullName("Head Librarian");
            u.setRole("LIBRARIAN");
            users.save(u);
        }
        if (books.count() == 0) {
            books.save(book("Clean Code", "Robert C. Martin", "9780132350884", "Programming", 3));
            books.save(book("Effective Java", "Joshua Bloch", "9780134685991", "Programming", 2));
            books.save(book("Database System Concepts", "Silberschatz", "9780078022159", "Databases", 4));
            books.save(book("Introduction to Algorithms", "Cormen", "9780262033848", "Algorithms", 2));
        }
        if (students.count() == 0) {
            students.save(student("Arun Kumar", "CS001", "arun@example.com", "Computer Science", "9876543210"));
            students.save(student("Priya Sharma", "CS002", "priya@example.com", "Computer Science", "9123456780"));
        }
    }

    private Book book(String title, String author, String isbn, String category, int copies) {
        Book b = new Book();
        b.setTitle(title);
        b.setAuthor(author);
        b.setIsbn(isbn);
        b.setCategory(category);
        b.setTotalCopies(copies);
        b.setAvailableCopies(copies);
        return b;
    }

    private Student student(String name, String rollNo, String email, String dept, String phone) {
        Student s = new Student();
        s.setName(name);
        s.setRollNo(rollNo);
        s.setEmail(email);
        s.setDepartment(dept);
        s.setPhone(phone);
        return s;
    }
}

package com.saumyadev.books.controller;

import com.saumyadev.books.entity.Book;
import com.saumyadev.books.exception.BookNotFoundException;
import com.saumyadev.books.request.BookRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "Books API Endpoints", description = "Operations related to books")
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final List<Book> books = new ArrayList<>();

    public BookController() {
        initializeBooks();
    }

    private void initializeBooks() {
        books.addAll(List.of(
                new Book(1, "A Brief History of Time", "Stephen Hawking", "Science", 5),
                new Book(2, "The Great Gatsby", "F. Scott Fitzgerald", "Fiction", 4),
                new Book(3, "Clean Code", "Robert C. Martin", "Programming", 5),
                new Book(4, "Thinking, Fast and Slow", "Daniel Kahneman", "Psychology", 4),
                new Book(5, "Sapiens: A Brief History of Humankind", "Yuval Noah Harari", "History", 5),
                new Book(6, "The Pragmatic Programmer", "Andrew Hunt", "Programming", 5),
                new Book(7, "To Kill a Mockingbird", "Harper Lee", "Fiction", 5),
                new Book(8, "The Selfish Gene", "Richard Dawkins", "Science", 4),
                new Book(9, "Atomic Habits", "James Clear", "Self-help", 5),
                new Book(10, "The Art of War", "Sun Tzu", "Philosophy", 4)
        ));
    }

    @Operation(summary = "Get all books", description = "retrieve a list of all available books")
    @GetMapping("/title/{title}")
    public Book getBookByTitle(@PathVariable String title) {

        return books.stream()
                .filter(book -> book.getTitle().equalsIgnoreCase(title))
                .findFirst()
                .orElse(null);

    }

    @GetMapping
    public List<Book> getBooksByCategory(@Parameter(description = "Optional query parameter") @RequestParam(required = false) String category){
        if(category == null) {
            return books;
        }

        return books.stream()
                .filter(book -> book.getCategory().equalsIgnoreCase(category))
                .toList();
    }

    @Operation(summary = "Get a book by ID", description = "Retrieve a specific book by ID")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/id/{id}")
    public Book getBooksById(@Parameter(description = "ID of the book to be retrieved.") @PathVariable @Min(value = 1) long id) throws Exception{

        return books.stream()
                .filter(book -> book.getId() == id)
                .findFirst()
                .orElseThrow(() -> new BookNotFoundException("Book not found - " + id));
    }

    @Operation(summary = "Create a new book", description = "Add a new book to the list")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void createBook(@Valid @RequestBody BookRequest bookRequest) {
        long id = books.isEmpty() ? 1 : books.getLast().getId() + 1;

        Book book = convertToBook(id, bookRequest);

        books.add(book);
    }

    @Operation(summary = "Update a book", description = "Update the details of an existing book")
    @PutMapping("/{id}")
    public Book updateBook(@Parameter(description = "ID of the book to be updated") @PathVariable long id, @Valid @RequestBody BookRequest bookRequest) {
        for(int i = 0; i < books.size(); i++) {
            if(books.get(i).getId() == id){
                Book updatedBook = convertToBook(id, bookRequest);
                books.set(i, updatedBook);
                return updatedBook;
            }
        }
        throw new BookNotFoundException("Book not found - "+id);
    }

    @Operation(summary = "Delete a book", description = "Remove a book from the list")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteBook(@Parameter(description = "ID of the book to be deleted") @PathVariable long id){
        books.stream()
             .filter(book -> book.getId() == id)
             .findFirst()
             .orElseThrow(() -> new BookNotFoundException("Book not found - "+id));

        books.removeIf(book -> book.getId() == id);
    }

    private Book convertToBook(long id, BookRequest bookRequest) {
        return new Book(
                id,
                bookRequest.getTitle(),
                bookRequest.getAuthor(),
                bookRequest.getCategory(),
                bookRequest.getRating()
        );
    }
}

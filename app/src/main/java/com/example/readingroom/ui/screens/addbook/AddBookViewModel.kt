package com.example.readingroom.ui.screens.addbook

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingroom.BuildConfig
import com.example.readingroom.data.BookRepository
import com.example.readingroom.data.BookRepositoryImpl
import com.example.readingroom.data.api.GoogleBooksApi
import com.example.readingroom.data.api.toBookModel
import com.example.readingroom.model.Book
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// Модели для событий (оставляем как есть)
data class BookActionResult(val success: Boolean, val bookTitle: String, val error: String? = null)
data class WishlistActionResult(val success: Boolean, val bookTitle: String, val error: String? = null)

@OptIn(FlowPreview::class)
@HiltViewModel
class AddBookViewModel @Inject constructor(
    private val googleBooksApi: GoogleBooksApi,
    private val bookRepository: BookRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Book>>(emptyList())
    val searchResults: StateFlow<List<Book>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _userAddedBooks = MutableStateFlow<List<Book>>(emptyList())
    val userAddedBooks: StateFlow<List<Book>> = _userAddedBooks.asStateFlow()

    private val _addBookResultEvent = Channel<BookActionResult>(Channel.BUFFERED)
    val addBookResultEvent = _addBookResultEvent.receiveAsFlow()

    private val _wishlistEvent = Channel<WishlistActionResult>(Channel.BUFFERED)
    val wishlistEvent = _wishlistEvent.receiveAsFlow()

    private val currentUserId: String? = auth.currentUser?.uid

    init {
        loadUserBooks()

        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .filter { it.isNotBlank() }
                .distinctUntilChanged()
                .flatMapLatest { query -> performSearch(query) }
                .catch { e -> 
                     Log.e("AddBookVM", "Error in search flow", e)
                     _error.value = "Ошибка поиска: ${e.message}"
                     _isLoading.value = false
                }
                .collect { results -> 
                    Log.d("AddBookVM", "Search results collected: ${results.size}")
                    _searchResults.value = results
                    _isLoading.value = false
                }
        }
    }

    private fun loadUserBooks() {
        if (currentUserId == null) return
        viewModelScope.launch {
            bookRepository.getAllBooks(currentUserId)
                .catch { e -> Log.e("AddBookVM", "Error loading user books", e) }
                .collect { books -> _userAddedBooks.value = books }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
    
    private fun performSearch(query: String): Flow<List<Book>> {
        Log.d("AddBookVM", "Performing search for: $query")
        _isLoading.value = true
        _error.value = null 

        val googleSearchFlow = flow { 
                emit(googleBooksApi.searchBooks(query, BuildConfig.GOOGLE_BOOKS_API_KEY)) 
            }
            .map { response -> 
                response.items?.mapNotNull { it.toBookModel() } ?: emptyList() 
            }
            .catch {
                Log.e("AddBookVM", "Google Books API error", it)
                _error.value = "Ошибка поиска в Google Books"
                emit(emptyList())
            }
        
        val manualSearchFlow = flowOf(emptyList<Book>())

        return combine(googleSearchFlow, manualSearchFlow) { googleResults, manualResults ->
            Log.d("AddBookVM", "Combining results - Google: ${googleResults.size}, Manual: ${manualResults.size}")
            val combinedList = mutableListOf<Book>()
            val googleIds = googleResults.mapNotNull { it.googleBooksId }.toSet()

            combinedList.addAll(googleResults)

            Log.d("AddBookVM", "Combined list size: ${combinedList.size}")
            combinedList.distinctBy { it.googleBooksId ?: it.id }
        }
    }

    fun addBookToLibrary(book: Book) {
        if (currentUserId == null) {
            viewModelScope.launch { _addBookResultEvent.send(BookActionResult(false, book.title, "Пользователь не авторизован")) }
            return
        }
        viewModelScope.launch {
            try {
                val bookToAdd = book.copy(userId = currentUserId, isInWishlist = false)
                bookRepository.addBook(bookToAdd)
                _addBookResultEvent.send(BookActionResult(true, book.title))
                _userAddedBooks.update { it + bookToAdd }
            } catch (e: BookRepositoryImpl.BookAlreadyExistsException) {
                 Log.w("AddBookVM", "Book already exists: ${e.message}")
                 _addBookResultEvent.send(BookActionResult(false, book.title, e.message))
             } catch (e: Exception) {
                Log.e("AddBookVM", "Error adding book to library", e)
                _addBookResultEvent.send(BookActionResult(false, book.title, "Ошибка добавления: ${e.message}"))
            }
        }
    }

    fun addToWishlist(book: Book) {
        if (currentUserId == null) {
             viewModelScope.launch { _wishlistEvent.send(WishlistActionResult(false, book.title, "Пользователь не авторизован")) }
            return
        }
        viewModelScope.launch {
             try {
                 val bookIdToSearch = book.googleBooksId ?: book.id
                 val existingBook = bookRepository.getBookById(userId = currentUserId, bookId = bookIdToSearch).firstOrNull()
                 
                 if (existingBook != null) {
                     if (!existingBook.isInWishlist) {
                          Log.d("AddBookVM", "Book ${existingBook.id} found in library, updating wishlist flag.")
                          bookRepository.updateBook(existingBook.copy(isInWishlist = true)) 
                          _wishlistEvent.send(WishlistActionResult(true, existingBook.title))
                          _userAddedBooks.update { list -> list.map { if(it.id == existingBook.id) existingBook.copy(isInWishlist = true) else it } }
                     } else { 
                          Log.d("AddBookVM", "Book ${existingBook.id} already in wishlist.")
                          _wishlistEvent.send(WishlistActionResult(false, existingBook.title, "Книга уже в списке желаний"))
                     }
                 } else {
                      Log.d("AddBookVM", "Book ${book.id} not found in library, adding to wishlist.")
                      val bookToAdd = Book(
                          id = book.id,
                          title = book.title,
                          author = book.author,
                          coverUrl = book.coverUrl,
                          description = book.description,
                          genre = book.genre,
                          pageCount = book.pageCount,
                          status = "wishlist",
                          rating = 0f,
                          isFavorite = false,
                          isRead = false,
                          readDate = null,
                          userId = currentUserId,
                          googleBooksId = book.googleBooksId,
                          isInWishlist = true,
                          titleLowercase = book.title.lowercase(),
                          authorLowercase = book.author.lowercase(),
                          addedDate = null
                      )
                      bookRepository.addBook(bookToAdd)
                      _wishlistEvent.send(WishlistActionResult(true, bookToAdd.title))
                      _userAddedBooks.update { it + bookToAdd }
                 }
             } catch (e: BookRepositoryImpl.BookAlreadyExistsException) {
                  Log.w("AddBookVM", "Book already exists (wishlist): ${e.message}")
                  _wishlistEvent.send(WishlistActionResult(false, book.title, "Книга уже добавлена"))
             } catch (e: Exception) {
                 Log.e("AddBookVM", "Error adding book to wishlist", e)
                 _wishlistEvent.send(WishlistActionResult(false, book.title, "Ошибка добавления в список желаний: ${e.message}"))
             }
        }
    }
} 
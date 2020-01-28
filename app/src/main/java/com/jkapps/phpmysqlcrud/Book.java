package com.jkapps.phpmysqlcrud;

class Book {
    private int id;
    private String title, author;
    private int rating;
    private String genre;

    public Book(int id, String title, String author, int rating, String genre) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.rating = rating;
        this.genre = genre;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getRating() {
        return rating;
    }

    public String getGenre() {
        return genre;
    }
}

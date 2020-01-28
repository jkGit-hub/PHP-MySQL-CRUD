package com.jkapps.phpmysqlcrud;

public class Api {
    private static final String ROOT_URL = "https://2mfreedom.com/test/BookApi/v1/Api.php?apicall=";

    public static final String URL_CREATE_BOOK = ROOT_URL + "createbook";
    public static final String URL_READ_BOOKS = ROOT_URL + "getbooks";
    public static final String URL_UPDATE_BOOK = ROOT_URL + "updatebook";
    public static final String URL_DELETE_BOOK = ROOT_URL + "deletebook&id=";
}
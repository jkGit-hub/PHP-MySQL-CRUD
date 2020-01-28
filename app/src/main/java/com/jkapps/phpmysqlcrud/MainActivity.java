package com.jkapps.phpmysqlcrud;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {

    private static final int CODE_GET_REQUEST = 1024;
    private static final int CODE_POST_REQUEST = 1025;

    EditText et_bookID, et_title, et_author;
    RatingBar ratingBar;
    TextView tv_ratingBarValue;
    Spinner spinnerGenre;
    ProgressBar progressBar;
    Button btnAddUpdate;
    ListView lv_books;

    List<Book> bookList;

    boolean isUpdating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_bookID = findViewById(R.id.et_bookID);
        et_title = findViewById(R.id.et_title);
        et_author = findViewById(R.id.et_author);
        ratingBar = findViewById(R.id.ratingBar);
        tv_ratingBarValue = findViewById(R.id.tv_ratingBarValue);
        spinnerGenre = findViewById(R.id.spinnerGenre);
        btnAddUpdate = findViewById(R.id.btnAddUpdate);
        progressBar = findViewById(R.id.progressBar);
        lv_books = findViewById(R.id.lv_books);

        bookList = new ArrayList<>();

        btnAddUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isUpdating) {
                    updateBook();
                } else {
                    createBook();
                }
            }
        });
        readBooks();
    }

    private void createBook() {
        String title = et_title.getText().toString().trim();
        String author = et_author.getText().toString().trim();
        int rating = (int) ratingBar.getRating();
        String genre = spinnerGenre.getSelectedItem().toString();

        if (TextUtils.isEmpty(title)) {
            et_title.setError("Please enter a book title.");
            et_title.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(author)) {
            et_author.setError("Please enter the author.");
            et_author.requestFocus();
            return;
        }

        HashMap<String, String> params = new HashMap<>();
        params.put("title", title);
        params.put("author", author);
        params.put("rating", String.valueOf(rating));
        params.put("genre", genre);

        PerformNetworkRequest request = new PerformNetworkRequest(Api.URL_CREATE_BOOK, params, CODE_POST_REQUEST);
        request.execute();

        clearInput();
    }

    private void clearInput() {
        et_title.setText("");
        et_author.setText("");
        ratingBar.setRating(0);
        spinnerGenre.setSelection(0);
    }

    private void readBooks() {
        PerformNetworkRequest request = new PerformNetworkRequest(Api.URL_READ_BOOKS, null, CODE_GET_REQUEST);
        request.execute();
    }

    private void updateBook() {
        String id = et_bookID.getText().toString();
        String title = et_title.getText().toString().trim();
        String author = et_author.getText().toString().trim();
        int rating = (int) ratingBar.getRating();
        String genre = spinnerGenre.getSelectedItem().toString();

        if (TextUtils.isEmpty(title)) {
            et_title.setError("Please enter a book title.");
            et_title.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(author)) {
            et_author.setError("Please enter the author.");
            et_author.requestFocus();
            return;
        }

        HashMap<String, String> params = new HashMap<>();
        params.put("id", id);
        params.put("title", title);
        params.put("author", author);
        params.put("rating", String.valueOf(rating));
        params.put("genre", genre);

        PerformNetworkRequest request = new PerformNetworkRequest(Api.URL_UPDATE_BOOK, params, CODE_POST_REQUEST);
        request.execute();

        btnAddUpdate.setText("Add");

        clearInput();

        isUpdating = false;
    }

    private void deleteBook(int id) {
        PerformNetworkRequest request = new PerformNetworkRequest(Api.URL_DELETE_BOOK + id, null, CODE_GET_REQUEST);
        request.execute();
    }

    private void refreshBookList(JSONArray books) throws JSONException {
        bookList.clear();

        for (int i = 0; i < books.length(); i++) {
            JSONObject obj = books.getJSONObject(i);

            bookList.add(new Book(
                    obj.getInt("id"),
                    obj.getString("title"),
                    obj.getString("author"),
                    obj.getInt("rating"),
                    obj.getString("genre")
            ));
        }

        BookAdapter adapter = new BookAdapter(bookList);
        lv_books.setAdapter(adapter);
    }

    private class PerformNetworkRequest extends AsyncTask<Void, Void, String> {
        String url;
        HashMap<String, String> params;
        int requestCode;

        PerformNetworkRequest(String url, HashMap<String, String> params, int requestCode) {
            this.url = url;
            this.params = params;
            this.requestCode = requestCode;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setVisibility(GONE);
            try {
                JSONObject object = new JSONObject(s);
                if (!object.getBoolean("error")) {
                    Toast.makeText(getApplicationContext(), object.getString("message"), Toast.LENGTH_SHORT).show();
                    refreshBookList(object.getJSONArray("books"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            RequestHandler requestHandler = new RequestHandler();

            if (requestCode == CODE_POST_REQUEST)
                return requestHandler.sendPostRequest(url, params);

            if (requestCode == CODE_GET_REQUEST)
                return requestHandler.sendGetRequest(url);

            return null;
        }
    }

    class BookAdapter extends ArrayAdapter<Book> {
        List<Book> bookList;

        public BookAdapter(List<Book> bookList) {
            super(MainActivity.this, R.layout.layout_book_list, bookList);
            this.bookList = bookList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View listViewItem = inflater.inflate(R.layout.layout_book_list, null, true);

            TextView tv_title = listViewItem.findViewById(R.id.tv_title);
            ImageButton ib_update = listViewItem.findViewById(R.id.ib_update);
            ImageButton ib_delete = listViewItem.findViewById(R.id.ib_delete);

            final Book book = bookList.get(position);

            tv_title.setText(book.getTitle());

            ib_update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isUpdating = true;
                    et_bookID.setText(String.valueOf(book.getId()));
                    et_title.setText(book.getTitle());
                    et_author.setText(book.getAuthor());
                    ratingBar.setRating(book.getRating());
                    tv_ratingBarValue.setText(ratingBar.getRating() + "/5.0");
                    spinnerGenre.setSelection(((ArrayAdapter<String>) spinnerGenre.getAdapter()).getPosition(book.getGenre()));
                    btnAddUpdate.setText("Update");
                }
            });

            ib_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                    builder.setTitle("Delete " + book.getTitle())
                            .setMessage("Are you sure you want to delete it?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteBook(book.getId());
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                }
            });

            return listViewItem;
        }
    }
}
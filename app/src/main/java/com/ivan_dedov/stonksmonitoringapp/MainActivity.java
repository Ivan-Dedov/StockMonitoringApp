package com.ivan_dedov.stonksmonitoringapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Describes the main activity - a list of stocks.
 */
public class MainActivity extends AppCompatActivity
                          implements StockViewAdapter.OnCardClickListener {
    /**
     * The index of the page currently being downloaded in the paginated NestedScrollView.
     */
    private int page = 0;
    /**
     * The number of stocks in one batch to be downloaded.
     */
    private final int batchSize = 10;

    /**
     * The list of all available stocks.
     */
    private ArrayList<Stock> stockList = new ArrayList<>();
    /**
     * The list of stock tickers in the S&P 500 list.
     */
    private final ArrayList<Stock> snp500Stocks = new ArrayList<>();
    /**
     * If the user is searching for stocks in the search bar, the found items go in this list.
     */
    private ArrayList<Stock> filteredList = new ArrayList<>();

    /**
     * The mode in which to show the stocks: true - show all, false - show favorites only.
     */
    private boolean showAllStocks = true;
    /**
     * Represents whether we can request data from the API. Upon receiving a 429 response
     * code, changes the value to false. After one minute passes, returns to true.
     */
    private boolean letDownload = true;
    /**
     * Detects whether the user has typed something in the search bar.
     *
     * true, if the user is searching for a company (i.e. has something inside the search text bar);
     * false, otherwise.
     */
    private boolean isFiltering = false;

    // Fields storing references to the corresponding objects in the activity.
    private RequestQueue requestQueue;
    private RecyclerView.Adapter stocksAdapter;
    private RecyclerView stocksRecyclerView;
    private NestedScrollView nestedScrollView;
    private ProgressBar progressBar;


    /**
     * Loads data and runs the application.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //clearData();      // UNCOMMENT THIS LINE IF YOU WISH TO CLEAR THE SAVED DATA!
        setContentView(R.layout.activity_main);

        // Setting references to objects in the activity.
        nestedScrollView = findViewById(R.id.nestedScrollView);
        progressBar = findViewById(R.id.stockLoadingBar);
        stocksRecyclerView = findViewById(R.id.stocksRecyclerView);
        stocksRecyclerView.setHasFixedSize(true);

        changeItemHighlight(findViewById(R.id.allStocksButton),
                            findViewById(R.id.favouriteStocksButton));

        // Adding a listener so that the user can search for items in the list.
        ((EditText)findViewById(R.id.searchTicker)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                isFiltering = !s.toString().equals("");
                filterStocks(s.toString());
            }
        });

        startApplication();
    }
    /**
     * Saves the stock data to the device when the application is stopped.
     */
    @Override
    public void onStop() {
        saveData();
        super.onStop();
    }

    /**
     * Starts the application.
     */
    private void startApplication() {
        if (connected()) {
            requestQueue = Volley.newRequestQueue(this);
            setStockButtonsClickable(false);
            new GetCompaniesAsyncTask().execute();
            displayStocks(stockList);
            setStockButtonsClickable(true);
            findViewById(R.id.stocksRecyclerView).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.noWifiIcon).setVisibility(View.VISIBLE);
            findViewById(R.id.noWifiMessage).setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }


    /**
     * Retrieves the data for the stocks in the corresponding page in the paginated ScrollView.
     *
     * @param page The zero-base index of the page.
     */
    private void getData(int page) {
        int itemCount = 0;
        for(int i = page * batchSize; i < (page + 1) * batchSize && i < snp500Stocks.size(); i++) {
                Stock stock = snp500Stocks.get(i);
                itemCount++;

                // Update prices and info for all stocks.
                new GetCompanyInfoAsyncTask(stock).execute();
                new GetStockInfoAsyncTask(stock).execute();

                stockList.add(stock);
        }
        stocksAdapter.notifyItemRangeInserted(stockList.size() - itemCount, itemCount);
    }

    /**
     * Saves the tickers of the stocks marked as favorites to the device.
     */
    private void saveData() {
        String jsonSaveFile = getString(R.string.data_save_file);
        Type stockListType = new TypeToken<ArrayList<String>>() {}.getType();

        ArrayList<String> favoriteStockTickers = new ArrayList<>();
        for(Stock stock : snp500Stocks) {
            if (stock.getIsFavorite()) {
                favoriteStockTickers.add(stock.getTicker());
            }
        }

        String jsonStocks = new Gson().toJson(favoriteStockTickers, stockListType);

        FileOutputStream fos = null;
        try {
            fos = openFileOutput(jsonSaveFile, MODE_PRIVATE);
            fos.write(jsonStocks.getBytes());
        } catch (IOException e) {
            // Can't do anything really, just hope for the best.
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    // Can't do anything really, just hope for the best.
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * Loads the saved data from the device - sets isFavorite value of the saved stocks to true.
     */
    private void loadData() {
        FileInputStream fis = null;
        String jsonSaveFile = getString(R.string.data_save_file);
        try {
            fis = openFileInput(jsonSaveFile);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
            }

            Type stockListType = new TypeToken<ArrayList<String>>() {}.getType();
            ArrayList<String> favoriteStocks = new Gson().fromJson(sb.toString(), stockListType);
            for(String ticker : favoriteStocks) {
                for(Stock stock : snp500Stocks) {
                    if (stock.getTicker().equals(ticker)) {
                        stock.setIsFavorite(true);
                        break;
                    }
                }
            }

        } catch (IOException e) {
            // Can't do anything really, just hope for the best.
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // Can't do anything really, just hope for the best.
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * Clears the data about stocks from the device.
     */
    private void clearData() {
        String jsonSaveFile = getString(R.string.data_save_file);
        Type stockListType = new TypeToken<ArrayList<Stock>>() {}.getType();
        String jsonStocks = new Gson().toJson(new ArrayList<Stock>(), stockListType);

        FileOutputStream fos = null;
        try {
            fos = openFileOutput(jsonSaveFile, MODE_PRIVATE);
            fos.write(jsonStocks.getBytes());
        } catch (IOException e) {
            // Can't do anything really, just hope for the best.
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    // Can't do anything really, just hope for the best.
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * Changes the value of the Clickable tag of the "Stocks" and "Favorites" buttons.
     *
     * @param clickable The value to set the Clickable tag to.
     */
    private void setStockButtonsClickable(boolean clickable) {
        TextView allStocksButton = findViewById(R.id.allStocksButton);
        TextView favoriteStocksButton = findViewById(R.id.favouriteStocksButton);
        allStocksButton.setClickable(clickable);
        favoriteStocksButton.setClickable(clickable);
    }
    /**
     * Handles clicking the "Stocks" button.
     */
    public void onStocksButtonClick(View v) {
        if (!showAllStocks) {
            setStockButtonsClickable(false);
            showAllStocks = true;
            changeItemHighlight(findViewById(R.id.allStocksButton),
                                findViewById(R.id.favouriteStocksButton));

            if (isFiltering) {
                filterStocks(((EditText)findViewById(R.id.searchTicker)).getText().toString());
                displayStocks(filteredList);
            } else {
                displayStocks(stockList);
            }

            setStockButtonsClickable(true);
        }
    }
    /**
     * Handles clicking the "Favorites" button.
     */
    public void onFavoriteButtonClick(View v) {
        if (showAllStocks) {
            setStockButtonsClickable(false);
            progressBar.setVisibility(View.INVISIBLE);
            showAllStocks = false;
            changeItemHighlight(findViewById(R.id.favouriteStocksButton),
                                findViewById(R.id.allStocksButton));

            if (isFiltering) {
                filterStocks(((EditText)findViewById(R.id.searchTicker)).getText().toString());
                displayStocks(selectFavoriteStocks(filteredList));
            } else {
                displayStocks(selectFavoriteStocks(snp500Stocks));
            }

            setStockButtonsClickable(true);
        }
    }


    /**
     * Retrieves SnP 500 company names and puts them into the ArrayList.
     */
    private class GetCompaniesAsyncTask extends AsyncTask<String, Void, Void> {
        /**
         * Asynchronously retrieves the tickers in the background.
         */
        @Override
        protected Void doInBackground(String... strings) {
            String url = getString(R.string.snp500Url);

            try {
                Document doc = Jsoup.connect(url).get();
                Elements sections = doc.select("tr").select("td").select("a");

                for(int i = 0; i < sections.size(); i++) {
                        String foundTicker = sections.get(i).text();

                        if (isAllUppercase(foundTicker)
                                && !stockAlreadyPresent(snp500Stocks, foundTicker)) {
                            snp500Stocks.add(new Stock(foundTicker));
                        }
                }

                loadData();
                if (letDownload) {
                    getData(page);
                }

                // Responsible for loading more data once scrolled to the bottom.
                nestedScrollView.setOnScrollChangeListener(
                        new NestedScrollView.OnScrollChangeListener() {
                            @Override
                            public void onScrollChange(NestedScrollView v,
                                                       int scrollX, int scrollY,
                                                       int oldScrollX, int oldScrollY) {
                                if (showAllStocks && letDownload) {
                                    if (scrollY == v.getChildAt(0).getMeasuredHeight()
                                                   - v.getMeasuredHeight()) {
                                        page++;
                                        progressBar.setVisibility(View.VISIBLE);
                                        getData(page);
                                    }
                                }
                            }
                        });
            } catch (IOException e) {
                // If something went wrong when trying to get data, nothing we can do -
                // display what we have.
                e.printStackTrace();
            }

            return null;
        }

        /**
         * Determines whether a stock is already present in the ArrayList.
         *
         * @param stocks The ArrayList to check against.
         * @param ticker The ticker to check.
         *
         * @return true, if a stock with the given ticker is present in the collection;
         * false, otherwise.
         */
        private boolean stockAlreadyPresent(@NotNull ArrayList<Stock> stocks, String ticker) {
            for(Stock stock : stocks) {
                if (stock.getTicker().equals(ticker)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Determines whether a string contains only uppercase symbols.
         *
         * @param s The string to check.
         *
         * @return true, if the string contains only uppercase characters; false, otherwise.
         */
        private boolean isAllUppercase(@NotNull String s) {
            for(int i = 0; i < s.length(); i++) {
                if (Character.isLowerCase(s.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
    }
    /**
     * Retrieves the information about a company
     * (currency, logo, description, country and industry).
     */
    private class GetCompanyInfoAsyncTask extends AsyncTask<String, Void, Void> {
        /**
         * The stock to get information on.
         */
        private Stock stock;

        /**
         * Asynchronously retrieves the required data about the stock in the background.
         */
        @Override
        protected Void doInBackground(String... strings) {
            String url = getString(R.string.baseUrl) + "stock/profile2?symbol=" +
                    stock.getTicker() + getString(R.string.apiToken);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            stock.setCurrency(response.getString(
                                    getString(R.string.currency_field_name)
                            ));
                            stock.setLogo(response.getString(
                                    getString(R.string.company_logo_field_name)
                            ));
                            stock.setDescription(response.getString(
                                    getString(R.string.company_description_field_name)
                            ));
                            stock.setCountry(response.getString(
                                    getString(R.string.country_field_name)
                            ));
                            stock.setIndustry(response.getString(
                                    getString(R.string.industry_field_name)
                            ));
                            stock.setMarketCapitalization(response.getInt(
                                    getString(R.string.market_capitalization_field_name)
                            ));
                        } catch (JSONException e) {
                            // Something can't be set, leave it as is.
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        letDownload = false;

                        // Setting a new timer to retry the request after one minute passes.
                        Timer retryRequestTimer = new Timer();
                        retryRequestTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                letDownload = true;
                                new GetCompanyInfoAsyncTask(stock).execute();
                            }
                        }, 60 * 1000);
                    });
            requestQueue.add(request);
            return null;
        }

        /**
         * Creates a new instance of this class.
         *
         * @param stock The stock to get information on.
         */
        public GetCompanyInfoAsyncTask(@NotNull Stock stock) {
            this.stock = stock;
        }
    }
    /**
     * Retrieves the price information about a stock
     * (open, previous, high, low and closed).
     */
    private class GetStockInfoAsyncTask extends AsyncTask<String, Void, Void> {
        /**
         * The stock to get information on.
         */
        private Stock stock;

        /**
         * Asynchronously retrieves the required data about the stock in the background.
         */
        @Override
        protected Void doInBackground(String... strings) {
            String url = getString(R.string.baseUrl) + "quote?symbol=" +
                    stock.getTicker() + getString(R.string.apiToken);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            stock.setCurrentPrice(response.getDouble(
                                    getString(R.string.current_price_field_name)
                            ));
                            stock.setPreviousPrice(response.getDouble(
                                    getString(R.string.previous_price_field_name)
                            ));
                            stock.setOpenPrice(response.getDouble(
                                    getString(R.string.open_price_field_name)
                            ));
                            stock.setHighPrice(response.getDouble(
                                    getString(R.string.high_price_field_name)
                            ));
                            stock.setLowPrice(response.getDouble(
                                    getString(R.string.low_price_field_name)
                            ));
                        } catch (JSONException e) {
                            // Can't set something - just leave it as is.
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        letDownload = false;

                        // Setting a new timer to retry the request after one minute passes.
                        Timer retryRequestTimer = new Timer();
                        retryRequestTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                letDownload = true;
                                new GetStockInfoAsyncTask(stock).execute();
                            }
                        }, 60 * 1000);
                    });

            requestQueue.add(request);
            return null;
        }

        /**
         * Creates a new instance of this class.
         *
         * @param stock The stock to get information on.
         */
        public GetStockInfoAsyncTask(@NotNull Stock stock) {
            this.stock = stock;
        }
    }


    /**
     * Handles clicking the CardView item, which displays detailed information about
     * the stock.
     *
     * @param position The index of the stock inside the stockList.
     */
    @Override
    public void onNoteClick(int position) {
        Intent intent = new Intent(this, StockInfoActivity.class);
        if (isFiltering) {
            intent.putExtra(getString(R.string.stock_parcel_name),
                    filteredList.get(position));
        } else {
            if (showAllStocks) {
                intent.putExtra(getString(R.string.stock_parcel_name),
                        stockList.get(position));
            } else {
                intent.putExtra(getString(R.string.stock_parcel_name),
                        selectFavoriteStocks(snp500Stocks).get(position));
            }
        }
        startActivity(intent);
    }

    /**
     * Displays the stocks as items in the RecyclerView.
     *
     * @param stockList The ArrayList of stocks to be displayed.
     */
    private void displayStocks(@NotNull ArrayList<Stock> stockList) {
        LayoutManager stocksLayoutManager = new LinearLayoutManager(this);
        stocksAdapter = new StockViewAdapter(stockList, this);
        stocksRecyclerView.setLayoutManager(stocksLayoutManager);
        stocksRecyclerView.setAdapter(stocksAdapter);
    }

    /**
     * Determines whether the device has an Internet connection.
     *
     * @return true, if Internet connection is present; false, otherwise.
     */
    private boolean connected() {
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnected());
    }

    /**
     * Filters through the elements in the ArrayList of stocks.
     *
     * @param s The string to find within tickers of the ArrayList.
     */
    private void filterStocks(String s) {
        filteredList = new ArrayList<>();
        ArrayList<Stock> stockList;

        if (showAllStocks) {
            stockList = this.stockList;
        } else {
            stockList = selectFavoriteStocks(this.snp500Stocks);
        }

        for(Stock stock : stockList) {
            String filter = s.toUpperCase();
            String ticker = stock.getTicker().toUpperCase();
            String description = "";
            if (stock.getDescription() != null) {
                description = stock.getDescription().toUpperCase();
            }
            if (ticker.startsWith(filter) || description.contains(filter)) {
                            filteredList.add(stock);
            }
        }
        ((StockViewAdapter)stocksAdapter).changeItemsInList(filteredList);
    }

    /**
     * Selects only the stocks marked as "Favorite" in the ArrayList.
     *
     * @param stockList The ArrayList to select favorites from.
     *
     * @return The ArrayList of stocks where isFavorite field value is true.
     */
    private @NotNull ArrayList<Stock> selectFavoriteStocks(@NotNull ArrayList<Stock> stockList) {
        ArrayList<Stock> favoriteStocks = new ArrayList<>();
        for(Stock stock : stockList) {
            if (stock.getIsFavorite()) {
                favoriteStocks.add(stock);
            }
        }
        return favoriteStocks;
    }

    /**
     * Changes the highlighting of the items.
     *
     * @param selectedTextView The TextView to be highlighted.
     * @param deselectedTextView The TextViews to be grayed-out.
     */
    private void changeItemHighlight(@NotNull TextView selectedTextView,
                                     @NotNull TextView... deselectedTextView) {
        selectedTextView.setTextColor(Color.parseColor(getString(R.string.black)));
        selectedTextView.setTextSize(28);

        for(TextView textView : deselectedTextView) {
            textView.setTextColor(Color.parseColor(getString(R.string.dark_gray)));
            textView.setTextSize(18);
        }
    }
}
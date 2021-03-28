package com.ivan_dedov.stonksmonitoringapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Describes a RecycleView adapter for stocks.
 */
public class StockViewAdapter extends RecyclerView.Adapter<StockViewAdapter.StockViewHolder> {
    /**
     * The Listener to use when the Card is clicked.
     */
    private final OnCardClickListener onCardClickListener;
    /**
     * Contains the stocks to be displayed in the RecyclerView.
     */
    private static ArrayList<Stock> stockList;


    /**
     * The ViewHolder for the RecyclerView.
     */
    public static class StockViewHolder extends RecyclerView.ViewHolder
                                        implements View.OnClickListener {
        private final OnCardClickListener listener;

        private final ImageView companyLogo;
        private final TextView companyTicker;
        private final TextView companyDescription;
        private final TextView price;
        private final TextView percentageChange;
        private final ImageButton favoriteButton;

        /**
         * Creates a new instance of the ViewHolder.
         */
        public StockViewHolder(@NonNull View itemView, OnCardClickListener listener) {
            super(itemView);
            companyLogo = itemView.findViewById(R.id.companyLogo);
            companyTicker = itemView.findViewById(R.id.companyTicker);
            companyDescription = itemView.findViewById(R.id.companyDescription);
            price = itemView.findViewById(R.id.currentPrice);
            percentageChange = itemView.findViewById(R.id.percentageChange);
            favoriteButton = itemView.findViewById(R.id.addToFavoritesButton);
            this.listener = listener;

            itemView.setOnClickListener(this);
        }

        /**
         * Handles clicking on the CardView to show the detailed information about the stock.
         */
        @Override
        public void onClick(View v) {
            Stock stock = stockList.get(getAdapterPosition());
            if (stock.getTicker() != null &&
                stock.getCurrency() != null &&
                stock.getCountry() != null &&
                stock.getIndustry() != null) {
                    listener.onNoteClick(getAdapterPosition());
            }
        }
    }

    /**
     * The interface responsible for displaying more detailed information about the stock.
     */
    public interface OnCardClickListener {
        void onNoteClick(int position);
    }


    /**
     * Creates a new Adapter for the RecyclerView.
     *
     * @param stockList The ArrayList of stocks to use in the Adapter.
     * @param listener The OnCardClickListener to use in the Adapter.
     */
    public StockViewAdapter(@NotNull ArrayList<Stock> stockList,
                            OnCardClickListener listener) {
        StockViewAdapter.stockList = stockList;
        this.onCardClickListener = listener;
    }


    /**
     * Sets the required values upon creating a new ViewHolder.
     * @return The new ViewHolder.
     */
    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stocks_cardview, parent, false);
        StockViewHolder vh = new StockViewHolder(v, onCardClickListener);
        vh.setIsRecyclable(false);
        return vh;
    }
    /**
     * Sets the values of the items in the activity.
     * @param holder The StockViewHolder to use.
     * @param position The zero-based position of the stock in the stockList.
     */
    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        // Changing the color of the card depending on its parity.
        if (position % 2 != 0) {
            View someView = holder.companyTicker;
            View root = someView.getRootView();
            ((CardView)root).setCardBackgroundColor(Color.parseColor("#FFF0F4F7"));
        }

        Stock stock = stockList.get(position);

        displayCompanyLogo(holder, stock);
        displayBasicValues(holder, stock);
        displayPriceChangeValue(holder, stock);
        displayFavoriteButton(holder, stock);
    }


    /**
     * Displays the company logo in a square with rounded corners.
     *
     * @param holder The StockViewHolder to use.
     * @param stock The stock to get the logo from.
     */
    private void displayCompanyLogo(@NotNull StockViewHolder holder, @NotNull Stock stock) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new RoundedCorners(20));
        Glide.with(holder.companyLogo)
                .load(stock.getLogo())
                .apply(requestOptions)
                .into(holder.companyLogo);
    }
    /**
     * Displays the following text values: company ticker, description and price.
     *
     * @param holder The StockViewHolder to use.
     * @param stock The stock to get information from.
     */
    private void displayBasicValues(@NotNull StockViewHolder holder, @NotNull Stock stock) {
        // Set decimal format to group digits like so: 000 000 000.00.
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        DecimalFormat df = (DecimalFormat)nf;
        df.setGroupingSize(3);
        df.setMaximumFractionDigits(2);

        holder.companyTicker.setText(stock.getTicker());
        holder.companyDescription.setText(stock.getDescription());
        holder.price.setText(Stock.currencies.get(stock.getCurrency())
                .concat(df.format(stock.getCurrentPrice()).replace(",", " ")));
    }
    /**
     * Displays the price change and its absolute value.
     *
     * @param holder The StockViewHolder to use.
     * @param stock The stock to get information from.
     */
    private void displayPriceChangeValue(@NotNull StockViewHolder holder, @NotNull Stock stock) {
        // Set decimal format to group digits like so: 000 000 000.00.
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        DecimalFormat df = (DecimalFormat)nf;
        df.setGroupingSize(3);
        df.setMaximumFractionDigits(2);

        double pChange = stock.getPercentageChange();
        double aChange = stock.getAbsoluteChange();

        String pChangeSign = pChange < 0 ? "-" : pChange > 0 ? "+" : "";
        String aChangeSign = aChange < 0 ? "-" : aChange > 0 ? "+" : "";

        String percentage =
                aChangeSign + Stock.currencies.get(stock.getCurrency()) +
                        df.format(Math.abs(aChange)).replace(",", " ") +
                " (" + pChangeSign + df.format(Math.abs(pChange)).replace(",", " ") + "%)";

        holder.percentageChange.setText(percentage);
        holder.percentageChange.setTextColor(
                pChange < 0 ? Color.parseColor("#FFB22424") :
                pChange > 0 ? Color.parseColor("#FF24B25D") :
                              Color.parseColor("#FFBABABA"));
    }
    /**
     * Displays the "favorite" state of the stock and adds an OnClickListener to the button
     * in order to toggle the state of the isFavorite field.
     *
     * @param holder The StockViewHolder to use.
     * @param stock The stock to get information from.
     */
    private void displayFavoriteButton(@NotNull StockViewHolder holder, @NotNull Stock stock) {
        if (stock.getIsFavorite()) {
            holder.favoriteButton.setImageResource(R.drawable.ic_stock_favorite);
        } else {
            holder.favoriteButton.setImageResource(R.drawable.ic_stock_not_favorite);
        }

        holder.favoriteButton.setOnClickListener(
                v -> {
                    if (stock.getIsFavorite()) {
                        holder.favoriteButton.setImageResource(R.drawable.ic_stock_not_favorite);
                    } else {
                        holder.favoriteButton.setImageResource(R.drawable.ic_stock_favorite);
                    }
                    stock.setIsFavorite(!stock.getIsFavorite());
                }
        );
    }


    /**
     * Retrieves the number of items in the stocks ArrayList.
     *
     * @return The number of items in the stocks ArrayList.
     */
    @Override
    public int getItemCount() {
        return stockList.size();
    }

    /**
     * Changes the ArrayList to a filtered one, notifies the RecyclerView about the changes.
     *
     * @param filteredStockList The filtered list.
     */
    public void changeItemsInList(ArrayList<Stock> filteredStockList) {
        stockList = filteredStockList;
        notifyDataSetChanged();
    }
}
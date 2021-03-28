package com.ivan_dedov.stonksmonitoringapp;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Describe an activity containing the detailed information about a stock.
 */
public class StockInfoActivity extends AppCompatActivity {
    /**
     * The stock to display information on.
     */
    private Stock stock;


    /**
     * Creates a new instance of the activity.
     *
     * @param savedInstanceBundle The information about the stock to display.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.activity_stock_info);

        this.stock = getIntent().getParcelableExtra(getString(R.string.stock_parcel_name));
        updateViews();
    }


    /**
     * Updates the information in the current activity.
     */
    private void updateViews() {
        displayCompanyLogo();
        getFavoriteButtonState();
        getTextValues();
        getNumericValues();
    }

    /**
     * Displays the company logo in a square with rounded corners.
     */
    private void displayCompanyLogo() {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new RoundedCorners(30));
        Glide.with(findViewById(R.id.companyLogoImageView))
                .load(stock.getLogo())
                .apply(requestOptions)
                .into((ImageView)findViewById(R.id.companyLogoImageView));
    }

    /**
     * Updates the Favorite button state.
     */
    private void getFavoriteButtonState() {
        if (stock.getIsFavorite()) {
            ((ImageButton)findViewById(R.id.favoriteButton))
                    .setImageResource(R.drawable.ic_stock_favorite);
        } else {
            ((ImageButton) findViewById(R.id.favoriteButton))
                    .setImageResource(R.drawable.ic_stock_not_favorite);
        }
    }
    /**
     * Updates the TextViews for: ticker, description, industry, country and date.
     */
    private void getTextValues() {
        ((TextView)findViewById(R.id.tickerLabel)).setText(stock.getTicker());
        ((TextView)findViewById(R.id.descriptionLabel)).setText(stock.getDescription());

        ((TextView)findViewById(R.id.industryAndCountryLabel))
                .setText(stock.getIndustry()
                        .concat(getString(R.string.industry_and_country_separator) + " ")
                        .concat(stock.getCountry()));

        String timeStamp = new SimpleDateFormat(getString(R.string.date_template), Locale.US)
                                .format(Calendar.getInstance().getTime());
        ((TextView)findViewById(R.id.dateLabel)).setText(timeStamp);
    }
    /**
     * Updates the TextViews for: current, open, high and low prices and price change values.
     */
    private void getNumericValues() {
        // Set decimal format to group digits like so: 000 000 000.00.
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        DecimalFormat df = (DecimalFormat)nf;
        df.setGroupingSize(3);
        df.setMaximumFractionDigits(2);

        // Market capitalization.
        ((TextView)findViewById(R.id.marketCapitalizationValue))
                .setText(Stock.currencies.get(stock.getCurrency())
                        .concat(df.format(stock.getMarketCapitalization())
                                .replace(",", " "))
                        .concat(getString(R.string.millions_unit)));

        // Current price.
        ((TextView)findViewById(R.id.currentPriceValue))
                .setText(Stock.currencies.get(stock.getCurrency())
                                         .concat(df.format(stock.getCurrentPrice())
                                                 .replace(",", " ")));

        // Open price.
        ((TextView)findViewById(R.id.openPriceLabel))
                .setText(Stock.currencies.get(stock.getCurrency())
                                         .concat(df.format(stock.getOpenPrice())
                                                 .replace(",", " ")));

        // Low price.
        ((TextView)findViewById(R.id.lowPriceValue))
                .setText(Stock.currencies.get(stock.getCurrency())
                                         .concat(df.format(stock.getLowPrice())
                                                 .replace(",", " ")));

        // High price.
        ((TextView)findViewById(R.id.highPriceValue))
                .setText(Stock.currencies.get(stock.getCurrency())
                                         .concat(df.format(stock.getHighPrice())
                                                 .replace(",", " ")));

        // Price change.
        getPriceChangeValues();
    }
    /**
     * Updates the TextView for price change.
     */
    private void getPriceChangeValues() {
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
                aChangeSign + Stock.currencies.get(stock.getCurrency())
                            + df.format(Math.abs(aChange)).replace(",", " ") +
                " (" + pChangeSign + df.format(Math.abs(pChange)).replace(",", " ") + "%)";

        ((TextView)findViewById(R.id.priceChangeValue)).setText(percentage);
        ((TextView)findViewById(R.id.priceChangeValue)).setTextColor(
                        pChange < 0 ? getColor(R.color.red) :
                        pChange > 0 ? getColor(R.color.green) :
                                      getColor(R.color.dark_gray));
    }
}
package com.ivan_dedov.stonksmonitoringapp;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static java.lang.Math.round;

/**
 * Describes a stock on the market.
 */
public class Stock implements Parcelable {
    /**
     * Stores the names and symbols for currencies.
     */
    public static final HashMap<String, String> currencies = new HashMap<String, String>() {
        {
            put("USD", "$");
            put("GBP", "£");
            put("EUR", "€");
            put("RUB", "₽");
            put(null, "");
        }
    };

    @SerializedName("ticker")
    private String ticker;
    @SerializedName("isFavorite")
    private boolean isFavorite;

    @SerializedName("country")
    private String country;
    @SerializedName("currency")
    private String currency;
    @SerializedName("logo")
    private String logo;
    @SerializedName("description")
    private String description;

    @SerializedName("industry")
    private String industry;

    @SerializedName("currentPrice")
    private double currentPrice;
    @SerializedName("previousPrice")
    private double previousPrice;

    @SerializedName("lowPrice")
    private double lowPrice;
    @SerializedName("highPrice")
    private double highPrice;
    @SerializedName("openPrice")
    private double openPrice;

    @SerializedName("marketCapitalization")
    private int marketCapitalization;


    /**
     * Creates a new instance of a Stock.
     *
     * @param ticker The ticker of the stock.
     */
    public Stock(String ticker) {
        isFavorite = false;
        this.ticker = ticker;
    }


    public String getTicker() {
        return ticker;
    }

    public boolean getIsFavorite() {
        return isFavorite;
    }
    public void setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }

    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getLogo() {
        return logo;
    }
    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getIndustry() {
        return industry;
    }
    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public double getCurrentPrice() {
        // Rounding to 2 decimal places.
        return round(currentPrice * 100.0) / 100.0;
    }
    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getPreviousPrice() {
        // Rounding to 2 decimal places.
        return round(previousPrice * 100.0) / 100.0;
    }
    public void setPreviousPrice(double previousPrice) {
        this.previousPrice = previousPrice;
    }

    public double getLowPrice() {
        return round(lowPrice * 100.0) / 100.0;
    }
    public void setLowPrice(double lowPrice) {
        this.lowPrice = lowPrice;
    }

    public double getHighPrice() {
        return round(highPrice * 100.0) / 100.0;
    }
    public void setHighPrice(double highPrice) {
        this.highPrice = highPrice;
    }

    public double getOpenPrice(){
        return openPrice;
    }
    public void setOpenPrice(double openPrice) {
        this.openPrice = openPrice;
    }

    public int getMarketCapitalization() {
        return marketCapitalization;
    }
    public void setMarketCapitalization(int marketCapitalization) {
        this.marketCapitalization = marketCapitalization;
    }

    /**
     * Calculates the price change of the stock (in its currency).
     *
     * @return The change in price
     * (positive, if the price increased; negative, if the price dropped).
     */
    public double getAbsoluteChange() {
        return round((currentPrice - previousPrice) * 100.0) / 100.0;
    }
    /**
     * Calculates the price change of the stock (in %).
     *
     * @return The change in price in %
     * (positive, if the price increased; negative, if the price dropped).
     */
    public double getPercentageChange() {
        return round((currentPrice - previousPrice) / previousPrice * 10000.0) / 100.0;
    }


    /**
     * Creates a new Stock from a parcel.
     *
     * @param in The parcel to extract stock data from.
     */
    private Stock(@NotNull Parcel in){
        String[] data = new String[13];
        in.readStringArray(data);

        this.ticker = data[0];
        this.isFavorite = Boolean.parseBoolean(data[1]);

        this.country = data[2];
        this.currency = data[3];
        this.logo = data[4];
        this.description = data[5];

        this.industry = data[6];

        this.currentPrice = Double.parseDouble(data[7]);
        this.previousPrice = Double.parseDouble(data[8]);

        this.lowPrice = Double.parseDouble(data[9]);
        this.highPrice = Double.parseDouble(data[10]);
        this.openPrice = Double.parseDouble(data[11]);

        this.marketCapitalization = Integer.parseInt(data[12]);
    }

    @Override
    public int describeContents(){
        return 0;
    }

    /**
     * Writes the Stock to a Parcel.
     *
     * @param dest The Parcel to write into.
     */
    @Override
    public void writeToParcel(@NotNull Parcel dest, int flags) {
        dest.writeStringArray(new String[]
                {
                        this.ticker,
                        String.valueOf(this.isFavorite),
                        this.country,
                        this.currency,
                        this.logo,
                        this.description,
                        this.industry,
                        String.valueOf(this.currentPrice),
                        String.valueOf(this.previousPrice),
                        String.valueOf(this.lowPrice),
                        String.valueOf(this.highPrice),
                        String.valueOf(this.openPrice),
                        String.valueOf(this.marketCapitalization)
                });
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Stock createFromParcel(Parcel in) {
            return new Stock(in);
        }

        public Stock[] newArray(int size) {
            return new Stock[size];
        }
    };
}
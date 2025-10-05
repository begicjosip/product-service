package org.tech.product_service.external.hnb;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * DTO for HNB API rate.
 * @see <a href="https://api.hnb.hr/">HNB API documentation</a>
 * @author Josip Begic
 */
public class HnbRateDto {
  @JsonProperty("broj_tecajnice")
  private String noOfExchangeRate;
  @JsonProperty("datum_primjene")
  private String dateOfApplication;
  @JsonProperty("drzava")
  private String country;
  @JsonProperty("drzava_iso")
  private String countryIso;
  @JsonProperty("kupovni_tecaj")
  private String buyingRate;
  @JsonProperty("prodajni_tecaj")
  private String sellingRate;
  @JsonProperty("sifra_valute")
  private String currencyCode;
  @JsonProperty("srednji_tecaj")
  private String middleRate;
  @JsonProperty("valuta")
  private String currency;

  /**
   * Converts the middle exchange rate from a String to a BigDecimal.
   * <p>
   * HNB API returns the middle rate as a String with a comma as decimal separator
   * (e.g., "1,173400"). This method replaces the comma with a dot and parses it
   * into a BigDecimal suitable for calculations.
   *
   * @return the middle exchange rate as a {@link BigDecimal}
   */
  public BigDecimal getMiddleRateAsBigDecimal() {
    return new BigDecimal(middleRate.replace(",", "."));
  }

  /**
   * Converts the date of application from a String to a LocalDate.
   * <p>
   * HNB API returns dates as ISO-8601 formatted strings (yyyy-MM-dd).
   * This method parses the string into a {@link LocalDate} object.
   *
   * @return the date of application as a {@link LocalDate}
   */
  public LocalDate getDateOfApplicationAsLocalDate() {
    return LocalDate.parse(dateOfApplication);
  }

}
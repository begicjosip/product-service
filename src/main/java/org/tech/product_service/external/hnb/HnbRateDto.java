package org.tech.product_service.external.hnb;

import java.math.BigDecimal;

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

  public BigDecimal getMiddleRateAsBigDecimal() {
    return new BigDecimal(middleRate.replace(",", "."));
  }
}
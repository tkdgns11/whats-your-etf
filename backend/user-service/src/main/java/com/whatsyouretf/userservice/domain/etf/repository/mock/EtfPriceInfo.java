package com.whatsyouretf.userservice.domain.etf.repository.mock;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EtfPriceInfo (
    LocalDate date,
    BigDecimal nav,
    BigDecimal close,
    BigDecimal dailyReturn
){
}

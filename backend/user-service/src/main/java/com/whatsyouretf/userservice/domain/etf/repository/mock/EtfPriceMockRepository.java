package com.whatsyouretf.userservice.domain.etf.repository.mock;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.etf.entity.EtfPrice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EtfPriceMockRepository {
    private final ListPageImpl<EtfPrice> listPage;

    public Page<EtfPrice> findAll(String ticker, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        List<EtfPriceInfo> temp = new ArrayList<>();
        try {
            String filePath = "data/" + ticker + ".csv";
            InputStream is = new ClassPathResource(filePath).getInputStream();
            CSVReader reader = new CSVReader(new InputStreamReader(is));

            String[] line;
            boolean header = true;

            while ((line = reader.readNext()) != null) {

                if (header) {
                    header = false;
                    continue;
                }
                log.debug(Arrays.toString(line));
                EtfPriceInfo etfPriceInfo = new EtfPriceInfo(
                    LocalDate.parse(line[0]),
                    new BigDecimal(line[1]),
                    new BigDecimal(line[2]),
                    Long.valueOf(line[4]),
                    new BigDecimal(line[3])
                );
                log.info(etfPriceInfo.toString());
                temp.add(etfPriceInfo);

            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.ETF_NOT_FOUND);
        } catch (CsvValidationException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        List<EtfPrice> fileterdList = temp.stream()
            .filter(info -> {
                boolean afterStart = startDate == null || !info.date().isBefore(startDate);
                boolean beforeEnd = endDate == null || !info.date().isAfter(endDate);
                return afterStart && beforeEnd;
            })
            .map(info -> new EtfPrice(
                1L,
                Etf.of(1L),
                info.date(),
                info.close(),
                info.nav(),
                info.volume(),
                info.dailyReturn(),
                LocalDateTime.now()
            ))
            .sorted(Comparator.comparing(EtfPrice::getTradeDate))
            .toList();

        return listPage.toPage(fileterdList, pageable);
    }
}

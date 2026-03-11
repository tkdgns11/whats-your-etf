package com.whatsyouretf.userservice.domain.etf.repository.mock;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EtfPriceJsonLoader {
    public List<EtfPriceInfo> findAll(String ticker) {
        try {
            String filePath = "data/" + ticker + ".csv";
            InputStream is = new ClassPathResource(filePath).getInputStream();
            CSVReader reader = new CSVReader(new InputStreamReader(is));

            String[] line;
            boolean header = true;
            List<EtfPriceInfo> temp = new ArrayList<>();
            while ((line = reader.readNext()) != null) {

                if (header) {
                    header = false;
                    continue;
                }

                temp.add(new EtfPriceInfo(
                    LocalDate.parse(line[1]),
                    BigDecimal.valueOf(Double.parseDouble(line[2])),
                    BigDecimal.valueOf(Long.parseLong(line[3])),
                    BigDecimal.valueOf(Double.parseDouble(line[4]))
                ));
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.ETF_NOT_FOUND);
        } catch (CsvValidationException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return null;
    }
}

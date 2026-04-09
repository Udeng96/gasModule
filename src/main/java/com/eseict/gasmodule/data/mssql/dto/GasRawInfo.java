package com.eseict.gasmodule.data.mssql.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class GasRawInfo {

    private Integer idx;
    private LocalDateTime date;
    private String state;
    private String detector;

}

package com.eseict.gasmodule.data.mssql.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "AlarmDB", schema = "dbo")
public class TblEventGas {

    @Id
    @Column(name = "idx")
    private Integer idx;

    @Column(name = "detector")
    private String detector;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "class")
    private String gasClass;

    @Column(name = "roomnum")
    private String roomNum;

    @Column(name = "gasname")
    private String gasName;

    @Column(name = "state")
    private String state;

    @Column(name = "set1st")
    private float set1st;

    @Column(name = "set2nd")
    private float set2nd;

    @Column(name = "value")
    private float value;

    @Column(name = "engunits")
    private String engUnits;

}

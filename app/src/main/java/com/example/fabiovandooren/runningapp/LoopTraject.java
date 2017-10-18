package com.example.fabiovandooren.runningapp;

import java.sql.Date;

/**
 * Created by fabiovandooren on 18/10/17.
 */

public class LoopTraject {

    String loopTrajectId;
    Date loopTrajectDatum;
    Number loopTrajectKms;

    public LoopTraject(String loopTrajectId, String loopTrajectDatum, String loopTrajectKms){

    }

    public LoopTraject(String loopTrajectId, Date loopTrajectDatum, Number loopTrajectKms) {
        this.loopTrajectId = loopTrajectId;
        this.loopTrajectDatum = loopTrajectDatum;
        this.loopTrajectKms = loopTrajectKms;
    }

    public String getLoopTrajectId() {
        return loopTrajectId;
    }

    public Date getLoopTrajectDatum() {
        return loopTrajectDatum;
    }

    public Number getLoopTrajectKms() {
        return loopTrajectKms;
    }
}

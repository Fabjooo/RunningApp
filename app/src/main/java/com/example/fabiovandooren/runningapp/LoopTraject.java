package com.example.fabiovandooren.runningapp;

import java.sql.Date;

/**
 * Created by fabiovandooren on 18/10/17.
 */

public class LoopTraject {

    String loopTrajectId;
    String loopTrajectDatum;
    String loopTrajectKms;

    public LoopTraject() {

    }

    public LoopTraject(String loopTrajectId, String loopTrajectDatum, String loopTrajectKms) {
        this.loopTrajectId = loopTrajectId;
        this.loopTrajectDatum = loopTrajectDatum;
        this.loopTrajectKms = loopTrajectKms;
    }

    public String getLoopTrajectId() {
        return loopTrajectId;
    }

    public String getLoopTrajectDatum() {
        return loopTrajectDatum;
    }

    public String getLoopTrajectKms() {
        return loopTrajectKms;
    }
}

package com.illoy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class LineStats {
    private long longCount = 0;
    private BigDecimal longMin = null;
    private BigDecimal longMax = null;
    private BigDecimal longSum = BigDecimal.ZERO;

    private long doubleCount = 0;
    private BigDecimal doubleMin = null;
    private BigDecimal doubleMax = null;
    private BigDecimal doubleSum = BigDecimal.ZERO;

    private long stringCount = 0;
    private long stringLengthMin = Long.MAX_VALUE;
    private long stringLengthMax = Long.MIN_VALUE;

    public void printStatistics(boolean isFullStatisticsMode) {
        String longMin = this.longMin == null ? "not exist" : this.longMin.toString();
        String longMax = this.longMax == null ? "not exist" : this.longMax.toString();
        String longSum = longCount == 0 ? "not exist" : this.longSum.toString();
        String longAverage = longCount == 0 ? "not exist" : getLongAverage().setScale(6, RoundingMode.HALF_UP).toString();
        String doubleMin = this.doubleMin == null ? "not exist" : this.doubleMin.setScale(6, RoundingMode.HALF_UP).toString();
        String doubleMax = this.doubleMax == null ? "not exist" : this.doubleMax.setScale(6, RoundingMode.HALF_UP).toString();
        String doubleSum = doubleCount == 0 ? "not exist" : this.doubleSum.toString();
        String doubleAverage = doubleCount == 0 ? "not exist" : getDoubleAverage().setScale(6, RoundingMode.HALF_UP).toString();
        String stringLengthMin = this.stringLengthMin == Long.MAX_VALUE ? "not exist" : String.valueOf(this.stringLengthMin);
        String stringLengthMax = this.stringLengthMax == Long.MIN_VALUE ? "not exist" : String.valueOf(this.stringLengthMax);

        String output;
        if (isFullStatisticsMode) {
            output = String.format("""
                STATISTICS:
                
                Integers:
                
                count = %s
                min = %s
                max = %s
                sum = %s
                average = %s
                
                Floats:
                
                count = %s
                min = %s
                max = %s
                sum = %s
                average = %s
                
                Strings:
                
                count = %s
                minLength = %s
                maxLength = %s
                """, longCount, longMin, longMax, longSum, longAverage,
                    doubleCount, doubleMin, doubleMax, doubleSum, doubleAverage,
                    stringCount, stringLengthMin, stringLengthMax);
        }
        else {
            output = String.format("""
                STATISTICS:
                
                Integers:
                
                count = %s
                
                Floats:
                
                count = %s
                
                Strings:
                
                count = %s
                """, longCount, doubleCount, stringCount);
        }

        System.out.println(output);
    }

    public void addLong(long num){
        BigDecimal bd = BigDecimal.valueOf(num);
        longCount++;
        longSum = longSum.add(bd);
        longMin = (longMin == null) ? bd : longMin.min(bd);
        longMax = (longMax == null) ? bd : longMax.max(bd);
    }

    public void addDouble(double num){
        BigDecimal bd = BigDecimal.valueOf(num);
        doubleCount++;
        doubleSum = doubleSum.add(bd);
        doubleMin = (doubleMin == null) ? bd : doubleMin.min(bd);
        doubleMax = (doubleMax == null) ? bd : doubleMax.max(bd);
    }

    public void add(Number number){
        if (number instanceof Long){
            addLong(number.longValue());
        }
        else {
            addDouble(number.doubleValue());
        }
    }

    public void addString(String str){
        stringCount++;
        stringLengthMin = Math.min(stringLengthMin, str.length());
        stringLengthMax = Math.max(stringLengthMax, str.length());
    }

    public BigDecimal getLongAverage() {
        return (longCount > 0) ? longSum.divide(BigDecimal.valueOf(longCount), 6, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public BigDecimal getDoubleAverage() {
        return (doubleCount > 0) ? doubleSum.divide(BigDecimal.valueOf(doubleCount), 6, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public long getLongCount() {
        return longCount;
    }

    public double getDoubleCount() {
        return doubleCount;
    }

    public long getStringCount() {
        return stringCount;
    }

    public long getStringLengthMin() {
        return stringLengthMin;
    }

    public long getStringLengthMax() {
        return stringLengthMax;
    }

    public BigDecimal getLongMin() {
        return longMin;
    }

    public BigDecimal getLongMax() {
        return longMax;
    }

    public BigDecimal getDoubleMin() {
        return doubleMin;
    }

    public BigDecimal getDoubleMax() {
        return doubleMax;
    }
}

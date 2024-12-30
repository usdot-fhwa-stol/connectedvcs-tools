/*
 * Copyright (C) 2024 LEIDOS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package gov.usdot.cv.rgaencoder;

public class DDate {
    private int year;
    private int month;
    private int day;
    
    public DDate()
    {
        this.year = 0;
        this.month = 0; 
        this.day = 0;
    }
    
    public DDate(int year, int month, int day) {
        this.year = year;
        this.month = month; 
        this.day = day;
    }

    public int getYear() {
        return year;
    }
    
    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public void setYear(int year){
        this.year = year;
    }
    
    public void setMonth(int month){
        this.month = month;
    }

    public void setDay(int day){
        this.day = day;
    }

    @Override
    public String toString() {
        return "RGAData [year" + year + ", month=" + month + ", day="+ day + "]";
    }
}
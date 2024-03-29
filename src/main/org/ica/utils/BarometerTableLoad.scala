package org.ica.utils

import org.apache.spark.sql.types._
import org.apache.spark.sql._
import org.apache.spark._
import org.apache.spark.sql.types.{StructType, StructField, StringType}


/**
 * @author sabari
 * @written 23 Nov, 2019
 * @description
 * 		trait to preprocess the barometer and temperature data and loading to target table of the application.
 */

trait BarometerTableLoad extends WeatherConfig{
  

   /*
   * Function to download weather data for the given input path and align the template into common format 
   * eg: 2013 Dec 2018 barometer measurements on air pressure  
   * 
   */
  
  def dataIngestionBarometer (path : String, fileName : String, targetTable : String ) : Unit = {
    
     var filePath=path +"\\"+fileName
        var tempViewName = fileName.substring(0, fileName.lastIndexOf("."))
        
        val header =  tempViewName match {
          case x if x.contains("1756") && x.contains("1858") => 
                     "year,month,day,morning_swc_inc,morning_deg_c,noon_swc_inc,noon_deg_c,evening_swc_inc,evening_deg_c,morning_air_pressure,noon_air_pressure,evening_air_pressure,morning_0deg_C,noon_0deg_c,evening_0deg_c"
          case x if x.contains("1859") && x.contains("1861") => 
                     "year,month,day,morning_swc_inc,morning_deg_c,morning_0deg_c,noon_swc_inc,noon_deg_c,noon_0deg_c,evening_swc_inc,evening_deg_c,evening_0deg_C,morning_air_pressure,noon_air_pressure,evening_air_pressure"
          case x if x.contains("1862") && x.contains("1937") => 
                     "year,month,day,morning_air_pressure,noon_air_pressure,evening_air_pressure,morning_swc_inc,morning_deg_c,morning_0deg_c,noon_swc_inc,noon_deg_C,noon_0deg_c,evening_swc_inc,evening_deg_c,evening_0deg_c"
          case x if x.contains("1938") && x.contains("1960") => 
                     "year,month,day,morning_air_pressure,noon_air_pressure,evening_air_pressure,morning_swc_inc,morning_deg_c,morning_0deg_c,noon_swc_inc,noon_deg_C,noon_0deg_c,evening_swc_inc,evening_deg_c,evening_0deg_c"
          case x if x.contains("1961") && x.contains("2012") => 
                     "year,month,day,morning_air_pressure,noon_air_pressure,evening_air_pressure,morning_swc_inc,morning_deg_c,morning_0deg_c,noon_swc_inc,noon_deg_C,noon_0deg_c,evening_swc_inc,evening_deg_c,evening_0deg_c" 
          case x if x.contains("2013") && x.contains("2017") && x.contains("stockholm_")  => 
                     "year,month,day,morning_air_pressure,noon_air_pressure,evening_air_pressure,morning_swc_inc,morning_deg_c,morning_0deg_c,noon_swc_inc,noon_deg_C,noon_0deg_c,evening_swc_inc,evening_deg_c,evening_0deg_c"
          case x if x.contains("2013") && x.contains("2017") && x.contains("stockholmA_")  =>  
                    "year,month,day,morning_air_pressure,noon_air_pressure,evening_air_pressure,morning_swc_inc,morning_deg_c,morning_0deg_c,noon_swc_inc,noon_deg_C,noon_0deg_c,evening_swc_inc,evening_deg_c,evening_0deg_c"
          case _ => ""
        }
     
      val partitionKey =  tempViewName match {
          case x if x.contains("stockholmA_") => 
		         "-A"
          case _ => ""
       }        
     
       val fields = header.split(",").map(fieldName => StructField(fieldName, StringType, nullable=true))
       val schema = StructType(fields)
       val dataFrame =  sparkSession.read
       .option("header","false")
       .option("delimiter"," ")
       .schema(schema)
       .csv(filePath)             
       dataFrame.createOrReplaceTempView(tempViewName)
            
       println ( " Table Name : " + tempViewName + "  Count : "+ dataFrame.count())                
       val targetDF = sparkSession.sql(s""" select 
               concat(Year,'${partitionKey}') as Year,
               concat (Year,'-',month,'-', day) as date,		
               morning_air_pressure,		
               noon_air_pressure,		
               evening_air_pressure,		
               morning_swc_inc,		
               morning_deg_C,	
               morning_0deg_C,		
               noon_swc_inc,		
               noon_deg_C,		
               noon_0deg_C,		
               evening_swc_inc,		
               evening_deg_C,		
               evening_0deg_C  
           from ${tempViewName}
           """ .stripMargin)   
       targetDF.write.mode("append").partitionBy("Year").saveAsTable(targetTable)
       println ( " Table Name : " + targetTable + "  Count : "+ targetDF.count())
  }
  
  

  
}
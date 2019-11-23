package  org.ica.gruppen

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.SQLContext


object icaMain extends fileConfig with Context{
  
  
  def main (args: Array[String]) :Unit ={    
     
    try{      
    
    //Reading Temperature files from the source folder   
    var temperatureFileList=list_files(temperatureDir)
    println(temperatureFileList)
    
    //Drop the existing Temperature table if any
    drop_table_if_exists(targetTable_Temperature)
    
    //Loading the files to Temperature table
    for (temperature_src_file_name <-temperatureFileList) {
      temperature_loading (temperaturePath,temperature_src_file_name,targetTable_Temperature)      
    }   
    
    //Reading Barometer files from the source folder
    
    var barometerFileList=list_files(barometerDir)
    println(barometerFileList)
    
    //Drop the existing Barometer table if any
    drop_table_if_exists(targetTable_Barometer)
    
    //Loading the files to Barometer table
    for (bar_src_file_name <-barometerFileList) {
       barometer_loading (barometerPath,bar_src_file_name,targetTable_Barometer)
    }
    
    // Prints the table count
    print_table_count()
    
    // Close the Spark Session
    close_context()
     
    }
    catch{      
      case x:RuntimeException => {
        println("Run Time Exception : " + x)
      }
      case x:Exception => {
        println("Exception : " + x)
      }
    }
  }
}
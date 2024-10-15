package io.prophecy.pipelines.test_pipeline

import io.prophecy.libs._
import io.prophecy.pipelines.test_pipeline.config._
import io.prophecy.pipelines.test_pipeline.functions.UDFs._
import io.prophecy.pipelines.test_pipeline.functions.PipelineInitCode._
import io.prophecy.pipelines.test_pipeline.graph._
import org.apache.spark._
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.expressions._
import java.time._

object Main {

  def apply(context: Context): Unit = {
    val df_csv_dat_read = csv_dat_read(context)
    val df_limit        = limit(context, df_csv_dat_read)
    csv_dat_write(context, df_limit)
  }

  def main(args: Array[String]): Unit = {
    val config = ConfigurationFactoryImpl.getConfig(args)
    val spark: SparkSession = SparkSession
      .builder()
      .appName("test_pipeline")
      .config("spark.default.parallelism",             "4")
      .config("spark.sql.legacy.allowUntypedScalaUDF", "true")
      .enableHiveSupport()
      .getOrCreate()
    val context = Context(spark, config)
    spark.conf.set("prophecy.metadata.pipeline.uri", "pipelines/test_pipeline")
    spark.conf.set("spark.hadoop.fs.s3a.endpoint",   "s3.us-west-2.amazonaws.com")
    spark.conf
      .set("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
    registerUDFs(spark)
    MetricsCollector.instrument(spark, "pipelines/test_pipeline") {
      apply(context)
    }
  }

}

// Databricks notebook source
// MAGIC %md
// MAGIC # What's in this exercise
// MAGIC Basics of how to work with CosmosDB from Databricks <B>in batch</B>.<BR>
// MAGIC Section 07: Aggregation operations<BR>
// MAGIC 
// MAGIC **Reference:**<br> 
// MAGIC **TODO**

// COMMAND ----------

// MAGIC %md
// MAGIC ## 7.0. Aggregation operations

// COMMAND ----------

// MAGIC %md
// MAGIC ### 7.0.1. Count

// COMMAND ----------

// MAGIC %md
// MAGIC ##### 7.0.1a. RDD API

// COMMAND ----------

//Count on the spark side
sc.cassandraTable("books_ks", "books").count

// COMMAND ----------

//count on cassandra side - NOT IMPLEMENTED YET
sc.cassandraTable("books_ks", "books").cassandraCount

// COMMAND ----------

// MAGIC %md
// MAGIC ##### 7.0.1b. Dataframe API
// MAGIC *Roadmap item for dataframe (works for RDD)*<br>
// MAGIC While we are pending release of count support, the sample below shows how we can execute counts currently -<br>
// MAGIC - materializes the dataframe to memory and then does a count<BR>
// MAGIC   
// MAGIC 
// MAGIC **Options for storage level**<br>
// MAGIC https://spark.apache.org/docs/2.2.0/rdd-programming-guide.html#which-storage-level-to-choose<br>
// MAGIC (1) MEMORY_ONLY:	
// MAGIC Store RDD as deserialized Java objects in the JVM. If the RDD does not fit in memory, some partitions will not be cached and will be recomputed on the fly each time they're needed. This is the default level.<br>
// MAGIC (2) MEMORY_AND_DISK:	<br>
// MAGIC Store RDD as deserialized Java objects in the JVM. If the RDD does not fit in memory, store the partitions that don't fit on disk, and read them from there when they're needed.
// MAGIC (3) MEMORY_ONLY_SER: Java/Scala<br>
// MAGIC Store RDD as serialized Java objects (one byte array per partition). This is generally more space-efficient than deserialized objects, especially when using a fast serializer, but more CPU-intensive to read.<br>
// MAGIC (4) MEMORY_AND_DISK_SER:  Java/Scala<br>
// MAGIC Similar to MEMORY_ONLY_SER, but spill partitions that don't fit in memory to disk instead of recomputing them on the fly each time they're needed.<br>
// MAGIC (5) DISK_ONLY:	<br>
// MAGIC Store the RDD partitions only on disk.<br>
// MAGIC (6) MEMORY_ONLY_2, MEMORY_AND_DISK_2, etc.	<br>
// MAGIC Same as the levels above, but replicate each partition on two cluster nodes.<br>
// MAGIC (7) OFF_HEAP (experimental):<br>
// MAGIC Similar to MEMORY_ONLY_SER, but store the data in off-heap memory. This requires off-heap memory to be enabled.<br>

// COMMAND ----------

//Read from source
val readBooksDF = spark
  .read
  .cassandraFormat("books", "books_ks", "")
  .load()

//Explain plan
readBooksDF.explain

//Does not work
readBooksDF.count

// COMMAND ----------

//Workaround
import org.apache.spark.storage.StorageLevel

//Read from source
val readBooksDF = spark
  .read
  .cassandraFormat("books", "books_ks", "")
  .load()

//Explain plan
readBooksDF.explain

//Materialize the dataframe
readBooksDF.persist(StorageLevel.MEMORY_ONLY)

//Subsequent execution against this DF hits the cache defined while materializing
readBooksDF.count

// COMMAND ----------

// MAGIC %sql
// MAGIC --Quick look at data
// MAGIC select * from books_vw

// COMMAND ----------

// MAGIC %sql
// MAGIC select count(*) from books_vw where book_pub_year > 1900;

// COMMAND ----------

// MAGIC %sql
// MAGIC select count(book_id) from books_vw;

// COMMAND ----------

// MAGIC %sql 
// MAGIC select count(*) from books_vw where book_pub_year > 1900;

// COMMAND ----------

// MAGIC %sql
// MAGIC select book_author, count(*) as count from books_vw group by book_author;

// COMMAND ----------

// MAGIC %sql
// MAGIC select count(*) from books_vw;

// COMMAND ----------

// MAGIC %md
// MAGIC ### 7.0.2. Average

// COMMAND ----------

// MAGIC %md
// MAGIC ##### 7.0.2a. SQL

// COMMAND ----------

// MAGIC %sql
// MAGIC select avg(book_price) from books_vw;

// COMMAND ----------

// MAGIC %md
// MAGIC ##### 7.0.2b. RDD

// COMMAND ----------

//======================
//RDD
//======================
sc.cassandraTable("books_ks", "books").select("book_price").as((c: Float) => c).mean

// COMMAND ----------

// MAGIC %md
// MAGIC ##### 7.0.2c. Dataframe

// COMMAND ----------

//======================
//DATAFRAME
//======================
val readBooksDF = spark
  .read
  .cassandraFormat("books", "books_ks", "")
  .load()

//Explain plan
readBooksDF.explain

//Does not work
readBooksDF.agg(avg("book_price")).show

// COMMAND ----------

// MAGIC %md
// MAGIC ### 7.0.3. Min

// COMMAND ----------

// MAGIC %md
// MAGIC ##### 7.0.3.a. CQL

// COMMAND ----------

// MAGIC %sql
// MAGIC select min(book_price) from books_vw;

// COMMAND ----------

// MAGIC %md
// MAGIC ##### 7.0.3.b. RDD API

// COMMAND ----------

//======================
//RDD
//======================
sc.cassandraTable("books_ks", "books").select("book_price").as((c: Float) => c).min

// COMMAND ----------

// MAGIC %md
// MAGIC ##### 7.0.3.c. Dataframe API

// COMMAND ----------

//======================
//DATAFRAME
//======================
val readBooksDF = spark
  .read
  .cassandraFormat("books", "books_ks", "")
  .load()

//Explain plan
readBooksDF.explain

//Does not work
readBooksDF.agg(min("book_price")).show

// COMMAND ----------

// MAGIC %md
// MAGIC ### 7.0.4. Max

// COMMAND ----------

// MAGIC %md
// MAGIC ##### 7.0.4.a. SQL API

// COMMAND ----------

// MAGIC %sql
// MAGIC select max(book_price) from books_vw;

// COMMAND ----------

// MAGIC %md
// MAGIC ##### 7.0.4.b. RDD API

// COMMAND ----------

//======================
//RDD
//======================
sc.cassandraTable("books_ks", "books").select("book_price").as((c: Float) => c).max

// COMMAND ----------

// MAGIC %md
// MAGIC ##### 7.0.4.c. Dataframe API

// COMMAND ----------

//======================
//DATAFRAME
//======================
val readBooksDF = spark
  .read
  .cassandraFormat("books", "books_ks", "")
  .load()

//Explain plan
readBooksDF.explain

//Does not work
readBooksDF.agg(max("book_price")).show

// COMMAND ----------

// MAGIC %md
// MAGIC ### 7.0.5. Sum

// COMMAND ----------

// MAGIC %md
// MAGIC ##### 7.0.5.a. SQL

// COMMAND ----------

// MAGIC %sql
// MAGIC --select sum(book_price) from books_vw;
// MAGIC select * from books_vw;

// COMMAND ----------

// MAGIC %md
// MAGIC ##### 7.0.5.b. Dataframe API

// COMMAND ----------

//======================
//DATAFRAME
//======================
val readBooksDF = spark
  .read
  .cassandraFormat("books", "books_ks", "")
  .load()

//Explain plan
readBooksDF.explain

//Does not work
readBooksDF.agg(sum("book_price")).show

// COMMAND ----------

// MAGIC %md
// MAGIC ##### 7.0.5.b. RDD API

// COMMAND ----------

//======================
//RDD
//======================
sc.cassandraTable("books_ks", "books").select("book_price").as((c: Int) => c).sum

// COMMAND ----------

// MAGIC %md
// MAGIC ### 7.0.6. Top

// COMMAND ----------

// MAGIC %md
// MAGIC ##### 7.0.6.a. SQL

// COMMAND ----------

// MAGIC %sql
// MAGIC select book_name,book_price from books_vw order by book_price desc limit 3;

// COMMAND ----------

// MAGIC %md
// MAGIC ##### 7.0.6.a. RDD API

// COMMAND ----------

//======================
//RDD
//======================
val readCalcTopRDD = sc.cassandraTable("books_ks", "books").select("book_name","book_price").sortBy(_.getFloat(1), false)
readCalcTopRDD.zipWithIndex.filter(_._2 < 3).collect.foreach(println)//delivers the first top n items without collecting the rdd to the driver.

// COMMAND ----------

// MAGIC %md
// MAGIC ##### 7.0.6.c. Dataframe API

// COMMAND ----------

//======================
//DATAFRAME
//======================
import org.apache.spark.sql.functions._

val readBooksDF = spark.read
.format("org.apache.spark.sql.cassandra")
  .options(Map( "table" -> "books", "keyspace" -> "books_ks"))
  .load
  .select("book_name","book_price")
  .orderBy(desc("book_price"))
  .limit(3)

//Explain plan
readBooksDF.explain

//Does not work
readBooksDF.show
# Coursework for TIE-22306 Data-Intensive Programming
##by Daniel "3ICE" Berezvai, Arjun Venkat, and Juho Peltonen

###Task 1 & 2:
####What are the top-10 best selling products in terms of total sales? & What are the top-10 browsed products?
 * flume-config
 * run-flume.sh
 * ProductCount.java
   - Regex used: https://regex101.com/r/eDt96O/1
   - **TESTING**: Run/Debug configuration → Program arguments: `/home/hduser/Desktop/log-data/ out2`
   - **PRODUCTION**: Compile and then execute with `hadoop jar productcount.jar fi.tut.ProductCount log-data out2`
 * SampleOutput.txt
 * psql
 * To answer these questions:
   * start some essential daemons: `sh start-daemons.sh`
   * Copy log data to HDFS: `./run-flume.sh`
   * run hadoop job: TODO how do we do this so result is in HDFS
   * run sqoop job: `./sqoop.sh`
   * top 10 best selling products: `psql -f top-10-selling-products.sql`
   * top 10 browsed products: `psql -f top-10-browsed-products.sql`
   * stop daemons: `sh stop-daemons.sh`
 
###Task 3:
####What anomaly is there between these two?
The fifth most purchased product is not in the top 10 most browsed (Under Armour Girls' Toddler Spine Surge Runni) and the **second** most browsed item (adidas Kids' RG III Mid Football Cleat) is not purchased frequently enough to be in the top ten profitable items.

(Initial guess was: Order of magnitude more people are buying a product, than browsing it. How do they buy without browsing? This is not the answer, because the logs are from one day while the purchases are from months of data.)

![Q3](/Q3.png)

###Task 4:
####What are the most popular browsing hours?

hour | amount 

------+--------
   
   21 |  31307
   
   20 |  30225
   
   22 |  29288
   
   23 |  21371
   
   19 |  19995
   
   18 |  10303
   
   11 |   8589
   
   17 |   6771
   
   12 |   5563
   
   10 |   5008

##Guidelines
Since the managers of the company don’t use Hadoop but a RDBMS, all the data must be transferred to PostgreSQL. Therefore, the detailed tasks are:
* Transfer Apache logs (with Apache Flume) to the HDFS
* Compute the frequencies of viewing of different products using MapReduce (Question 2)
* Compute the viewing hour data with MapReduce (Q4)
* Transfer the results (with Apache Sqoop) to PostgreSQL
* Find answer to the questions in PostgreSQL using SQL (Q1-4)

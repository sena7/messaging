# Messaging application
Author : Sena Bak

## prerequisites
* Java 1.8 is required as minimum. Set the project jdk path to the directory you have in your local machine. 
* The only external libraries required other than jdk 1.8 is slf4j-api-1.7.25.jar and slf4j-simple-1.7.25.jar

## how to run it
* navigate to Main class and run the main method, then use the console to interact with it. 

## design 
* Single thread for each producer and consumer side is utilised to access the single queue instance. 
* The producer and consumer has data containers to simulate databases they would maintain separately. 
* The common package has product data container to simulate the single source of truth.
* The controllers and data containers were implemented with singleton to share single instances throughout the application.
* The message queue container simulates a message broker and single main queue and deadletter queue (to test dead letter, program menu option 7 need to be implemented).
* producer side data input is loosely validated to simulate the failure responses returned in string values. 
* consumer side message validation before persisting(adding to the consumer data container) has strict rules. 

## further enhancements to be made
* Implement main menu option 7, and test dead letter queue. 
* Add more documentations
* Use dependency management tools like Gradle 
* Extract hardcoded static configuration values for BigDecimal scale, thread sleep time, etc. 
* BigDecimal scale is not set except for printing the values in console or logging.  
It was for the maximum precision of calculation (especially for large volume of random BigDecimal values coming from SampleDataGenerator) 
and the convenience of comparing data between producer and consumer side after messages are consumed for this exercise. 
however, the scale should be set properly according to a specific business requirement.  
* Repeated validation rules and BigDecimal comparisons should be reorganised.
* Move some info level logging to debug level. 

## shortfalls
* TDD approach not applied.

## for testing with menu options.
1.  To test Sale messages and Adjustment Messages are consumed properly
* choose option <6 ADD RANDOM SALES> and input desired quantity.
* choose option <2 adjust value of all sales of a single product> and follow the steps. 
to see the products that have any sale, choose option <4 show all sales in producer database>
* choose 6 again to add excessive amount of sales to see the log outputs for every 10/50 messages.
* choose option <7> to compare the sale & adjustment data between producer and consumer. 
* repeat the steps above. 

 
 
 


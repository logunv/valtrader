rem @echo of
java -classpath .;jsql/mysql.jar equotes.GetQuote 01/01/2013 pos > quotes.txt
java -classpath .;jsql/mysql.jar jsql < quotes.sql

Rem java equotes.GetQuote 01/01/2013 nas100 sp500 djia etf-liq follow pos > quotes.txt
Rem rb_tmu -d stocks quotes.tmu system manager

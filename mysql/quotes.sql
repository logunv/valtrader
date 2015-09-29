use valtrader;
LOAD DATA LOCAL INFILE 'QUOTES.txt'
INTO TABLE QUOTES columns terminated by ',';

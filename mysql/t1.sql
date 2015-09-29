use valtrader;

Create table FQUOTES1 (
  SYMBOL CHAR(12)
  ,TRADE_DAY DATE
  ,OPEN_PRICE float
  ,HIGH float
  ,LOW float
  ,CLOSE_PRICE float
  ,VOL INTEGER
  ,OI INTEGER
  ,EXPR CHAR(6)
);

LOAD DATA LOCAL INFILE 'FQUOTES1.txt'
INTO TABLE FQUOTES1 columns terminated by ',';
;

select count(*) from fquotes1;

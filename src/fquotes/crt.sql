--drop table fquotes;
Create table fquotes(symbol char(12) not null, trade_day date not null,
     open_price float,
     high float,  low float, close_price float, vol int,
     oi int,
     expr char(6),
     primary key(symbol, trade_day)
     );

drop table fxquotes;
Create table fxquotes (symbol char(12) not null, trade_day date not null,
     open_price float,
     high float,  low float, close_price float,
     primary key(symbol, trade_day)
     );

# java GetQuote 01/01/2010 tm my-stocks etf > /tmp/quotes.txt
# tm --? Trending markets
java GetQuote 04/01/2010 ltq-mkts indexes tm-etf-lev leaders > /tmp/quotes.txt
rb_tmu -d stocks quotes.tmu system manager

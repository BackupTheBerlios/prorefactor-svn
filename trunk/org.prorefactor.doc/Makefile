
# /c/progfile/fop-0.20.5/fop.bat -xml manual.xml -xsl c:/usr/share/xsl/docbook/fo/docbook.xsl -pdf manual.pdf

html/index.html: manual.xml joanju.xsl Makefile
	rm -rf html
	xmlto -o html -x joanju.xsl html manual.xml
	cp -r images html
	cp -r screenshots html
	cp *.css html
	rm -rf html/images/.svn
	rm -rf html/images/callouts/.svn
	rm -rf html/screenshots/.svn
	cp html/toc.xml .
	cd html ; zip -r ../doc.zip *


# This wrote into the top directory. How did I work around that in Callgraph?
#
# html/index.html: manual.xml joanju.xsl Makefile
#	rm -rf html
#	mkdir html
#	cp -r images html
#	cp -r screenshots html
#	cp *.css html
#	rm -rfd html/images/.svn
#	rm -rfd html/images/callouts/.svn
#	rm -rfd html/screenshots/.svn
#	java -cp /progfile/xalan-j_2_7_0/xalan.jar org.apache.xalan.xslt.Process -IN manual.xml -XSL joanju.xsl
#	cd html ; zip -r ../doc.zip *


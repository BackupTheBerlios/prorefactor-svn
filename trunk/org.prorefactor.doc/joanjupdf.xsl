<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

<xsl:import href="c:/usr/share/xsl/docbook/fo/docbook.xsl"/>

<xsl:param name="fop.extensions" select="1"></xsl:param>
<xsl:param name="variablelist.max.termlength">16</xsl:param>
<xsl:param name="variablelist.as.blocks" select="1"></xsl:param>

<xsl:param name="admon.graphics" select="1"></xsl:param>

<xsl:param name="insert.xref.page.number">yes</xsl:param>

<xsl:param name="section.autolabel" select="1"></xsl:param>
<xsl:param name="section.label.includes.component.label" select="1"></xsl:param>
<xsl:param name="xref.with.number.and.title" select="1"></xsl:param>



<xsl:template match="guibutton">
  <xsl:call-template name="inline.boldseq"/>
</xsl:template>

<xsl:template match="guiicon">
  <xsl:call-template name="inline.boldseq"/>
</xsl:template>

<xsl:template match="guilabel">
  <xsl:call-template name="inline.boldseq"/>
</xsl:template>

<xsl:template match="guimenu">
  <xsl:call-template name="inline.boldseq"/>
</xsl:template>

<xsl:template match="guimenuitem">
  <xsl:call-template name="inline.boldseq"/>
</xsl:template>

<xsl:template match="guisubmenu">
  <xsl:call-template name="inline.boldseq"/>
</xsl:template>


</xsl:stylesheet>




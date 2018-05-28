<!--

    NAME     LoginAppearance.xsl
    VERSION  1.21.0
    DATE     2018-03-19

    Copyright 2012-2018

    This file is part of the Linked Data Theatre.

    The Linked Data Theatre is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    The Linked Data Theatre is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the Linked Data Theatre.  If not, see <http://www.gnu.org/licenses/>.

-->
<!--
    DESCRIPTION
	LoginAppearance, add-on of rdf2html.xsl
	
	The Login appearance is used whenever a user has to enter his/her credentials (as part of a form-based authentication).
	
-->
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:res="http://www.w3.org/2005/sparql-results#"
	xmlns:elmo="http://bp4mc2.org/elmo/def#"
	xmlns:html="http://www.w3.org/1999/xhtml/vocab#"
>

<xsl:output method="xml" indent="yes"/>

<xsl:template match="rdf:RDF" mode="LoginAppearance">
	<form class="form-signin" action="#" method="post">
		<h2 class="form-signin-heading">Sign in</h2>
		<label for="username" class="sr-only">Username</label>
		<input type="text" id="username" name="username" class="form-control" placeholder="Username" required="required" autofocus="autofocus"/>
		<label for="password" class="sr-only">Password</label>
		<input type="password" id="password" name="password" class="form-control" placeholder="Password" required="required"/>
		<input type="hidden" id="csrf" name="_csrf" value="{/results/context/csrf}"/>
		<button class="btn btn-lg btn-primary btn-block" type="submit">Sign in</button>
	</form>
</xsl:template>

</xsl:stylesheet>
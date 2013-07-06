<h1><img src='ext_point_obj.gif' />&nbsp;${caption}</h1>
<table>
<tr valign='top'><td><nobr><b>${captionId}:</b></nobr></td><td>${id}</td></tr>
<tr valign='top'><td><nobr><b>${captionDeclaringPlugin}:</b></nobr></td><td>${declaringPlugin}</td></tr>
<tr valign='top'><td><nobr><b>${captionDeclaringFragment}:</b></nobr></td><td>${declaringFragment}</td></tr>
<tr valign='top'><td><nobr><b>${captionMultiplicity}:</b></nobr></td><td>${multiplicity}</td></tr>
<tr valign='top'><td><nobr><b>${captionDocumentation}:</b></nobr></td><td>${documentation}</td></tr>
</table>
<p>

<h3><img src='prop_ps.gif' />&nbsp;${captionParameterDefinitions}:</h3>
<table  border='1'>
<tr>
	<th><nobr>${captionId}</nobr></th>
	<th><nobr>${captionMultiplicity}</nobr></th>
	<th><nobr>${captionDocumentation}</nobr></th>
</tr>
§{paramaterDefinitions}[[
<tr valign='top'>
	<td>${id}</td>
	<td>${multiplicity}</td>
	<td>${documentation}</td>
</tr>]]
</table>
<p>

<h3><img src='prop_ps.gif' />&nbsp;${captionInheritedParameterDefinitions}:</h3>
<table  border='1'>
<tr>
	<th>${captionId}</th>
	<th>${captionDeclaringPoint}</th>
	<th>${captionMultiplicity}</th>
	<th>${captionDocumentation}</th>
</tr>
§{inheritedParamaterDefinitions}[[
<tr valign='top'>
	<td>${id}</td>
	<td>${declaringPoint}</td>
	<td>${multiplicity}</td>
	<td>${documentation}</td>
</tr>]]
</table>
<p>

<h3><img src='extensions_obj.gif' />&nbsp;${captionConnectedExtensions}:</h3>
<table  border='1'>
<tr>
	<th>${captionId}</th>
	<th>${captionDeclaringPlugin}</th>
	<th>${captionDocumentation}</th>
</tr>
§{connectedExtensions}[[
<tr valign='top'>
	<td>${id}</td>
	<td>${declaringPlugin}</td>
	<td>${documentation}</td>
</tr>]]
</table>
<h1><img src='plugin_obj.gif' />&nbsp;${caption}</h1>
<table>
<tr valign='top'><td><nobr><b>${captionId}:</b></nobr></td><td>${id}</td></tr>
<tr valign='top'><td><nobr><b>${captionVersion}:</b></nobr></td><td>${version}</td></tr>
<tr valign='top'><td><nobr><b>${captionVendor}:</b></nobr></td><td>${vendor}</td></tr>
<tr valign='top'><td><nobr><b>${captionLocation}:</b></nobr></td><td>${location}</td></tr>
<tr valign='top'><td><nobr><b>${captionDocumentation}:</b></nobr></td><td>${documentation}</td></tr>
</table>
<p>

<h3><img src='javadoc.gif' />&nbsp;${captionReferences}:</h3>
<table border='1'>
<tr>
	<th width='100'><nobr>${captionIndex}</nobr></nobr></th>
	<th><nobr>${captionReference}</nobr></nobr></th>
</tr>
§{references}[[
<tr valign='top'>
	<td>${index}</td>
	<td>${reference}</td>
</tr>]]
</table>
<p>

<!-- attributes -->
<h3><img src='prop_ps.gif' />&nbsp;${captionAttributes}:</h3>
<table border='1'>
<tr>
	<th><nobr>${captionId}</nobr></th>
	<th><nobr>${captionValue}</nobr></th>
	<th><nobr>${captionDocumentation}</nobr></th>
</tr>
§{attributes}[[
<tr valign='top'>
	<td>${id}</td>
	<td>${value}</td>
	<td>${documentation}</td>
</tr>]]
</table>
<p>

<!-- prerequisites -->
<h3><img src='req_plugins_obj.gif' />&nbsp;${captionPrerequisites}:</h3>
<table  border='1'>
<tr>
	<th><nobr>${captionId}</nobr></th>
	<th><nobr>${captionPluginVersion}</nobr></th>
	<th><nobr>${captionMatchingRule}</nobr></th>
	<th><nobr>${captionOptional}</nobr></th>
	<th><nobr>${captionDocumentation}</nobr></th>
</tr>
§{prerequisites}[[
<tr valign='top'>
	<td>${id}</td>
	<td>${version}</td>
	<td>${matchingRule}</td>
	<td>${optional}</td>
	<td>${documentation}</td>
</tr>]]
</table>
<p>

<!-- fragments -->
<h3><img src='frgmts_obj.gif' />&nbsp;${captionFragments}:</h3>
<table  border='1'>
<tr>
	<th><nobr>${captionId}</nobr></th>
	<th><nobr>${captionVersion}</nobr></th>
	<th><nobr>${captionPluginVersion}</nobr></th>
	<th><nobr>${captionMatchingRule}</nobr></th>
	<th><nobr>${captionDocumentation}</nobr></th>
</tr>
§{fragments}[[
<tr valign='top'>
	<td>${id}</td>
	<td>${version}</td>
	<td>${pluginVersion}</td>
	<td>${matchingRule}</td>
	<td>${documentation}</td>
</tr>]]
</table>
<p>

<!-- extensions -->
<h3><img src='extensions_obj.gif' />&nbsp;${captionExtensions}:</h3>
<table  border='1'>
<tr>
	<th><nobr>${captionId}</nobr></th>
	<th><nobr>${captionTarget}</nobr></th>
	<th><nobr>${captionDocumentation}</nobr></th>
</tr>
§{extensions}[[
<tr valign='top'>
	<td>${id}</td>
	<td>${target}</td>
	<td>${documentation}</td>
</tr>]]
</table>
<p>

<!-- extension points -->
<h3><img src='ext_points_obj.gif' />&nbsp;${captionExtensionPoints}:</h3>
<table border='1'>
<tr>
	<th><nobr>${captionId}</nobr></th>
	<th><nobr>${captionAncestry}</nobr></th>
	<th><nobr>${captionMultiplicity}</nobr></th>
	<th><nobr>${captionConnectedExtensions}</nobr></th>
	<th><nobr>${captionDocumentation}</nobr></th>
</tr>
§{extensionPoints}[[
<tr valign='top'>
	<td>${id}</td>
	<td>${ancestry}</td>
	<td>${multiplicity}</td>
	<td>${connectedExtensions}</td>
	<td>${documentation}</td>
</tr>]]
</table>
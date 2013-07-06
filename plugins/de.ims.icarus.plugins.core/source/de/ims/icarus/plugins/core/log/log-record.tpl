<html>
<head>
	<base href='${base}'/>
</head>
<body>
	<h1><img src='${icon}' />&nbsp;${caption}</h1>
	<table>
		<tr><td><nobr>${captionLogger}:</nobr></td><td>${logger}</td></tr>
		<tr><td><nobr>${captionLevel}:</nobr></td><td>${level}</td></tr>
		<tr><td><nobr>${captionDate}:</nobr></td><td>${date}</td></tr>
		<tr><td><nobr>${captionThreadID}:</nobr></td><td>${threadID}</td></tr>
		<tr><td><nobr>${captionClassName}:</nobr></td><td>${className}</td></tr>
		<tr><td><nobr>${captionMethodName}:</nobr></td><td>${methodName}</td></tr>
	</table>
	<p>
	<h3><img src='xml_text_node.gif' />&nbsp;${captionMessage}:</h3>
	${message}
	<p>
	<h3><img src='stckframe_obj.gif' />&nbsp;${captionStackTrace}:</h3>
	<font face='Consolas'>${stackTrace}</font>	
</body>
</html>
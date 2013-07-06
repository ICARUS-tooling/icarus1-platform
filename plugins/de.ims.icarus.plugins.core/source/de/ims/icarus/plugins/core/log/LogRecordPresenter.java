/*
 * $Revision: 46 $
 * $Date: 2013-06-13 12:32:58 +0200 (Do, 13 Jun 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.core/source/net/ikarus_systems/icarus/plugins/core/log/LogRecordPresenter.java $
 *
 * $LastChangedDate: 2013-06-13 12:32:58 +0200 (Do, 13 Jun 2013) $ 
 * $LastChangedRevision: 46 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.plugins.core.log;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import de.ims.icarus.io.IOUtil;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.view.AbstractEditorPanePresenter;
import de.ims.icarus.ui.view.MalformedTemplateException;
import de.ims.icarus.ui.view.Template;
import de.ims.icarus.ui.view.UnsupportedPresentationDataException;
import de.ims.icarus.util.HtmlUtils;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.cache.WeakLRUCache;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;


/**
 * @author Markus Gärtner
 * @version $Id: LogRecordPresenter.java 46 2013-06-13 10:32:58Z mcgaerty $
 *
 */
public class LogRecordPresenter extends AbstractEditorPanePresenter<LogRecord> {
	
	private static URL baseURL;

	private static final String emptyContent = "<html>{1}</html>"; //$NON-NLS-1$
	
	// We use weak references to the LogRecord objects to
	// allow the 'sender' of them to decide about them being
	// garbage-collected or not
	private static WeakLRUCache<LogRecord, String> textCache;
	
	private static Template sharedTemplate;

	private final Date date = new Date();
	private final DateFormat format = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$

	
	private static Template getSharedTemplate() {
		if(sharedTemplate==null) {
			String name = "log-record.tpl"; //$NON-NLS-1$
			
			URL url = LogRecordPresenter.class.getResource(name);
			if(url==null) {
				LoggerFactory.log(LogRecordPresenter.class, Level.SEVERE, "Cannot find template file: "+name); //$NON-NLS-1$
				return null;
			}
			String templateData = null;
			
			try {
				templateData = IOUtil.readStream(url.openStream(), IOUtil.UTF8_ENCODING);
				sharedTemplate = Template.compile(templateData, null);
			} catch (IOException e) {
				LoggerFactory.log(LogRecordPresenter.class, Level.SEVERE, "Failed to read template data from resource: "+url, e); //$NON-NLS-1$
			} catch (MalformedTemplateException e) {
				LoggerFactory.log(LogRecordPresenter.class, Level.SEVERE, "Malformed template data in resource: "+url, e); //$NON-NLS-1$
			}
		}
		return sharedTemplate;
	}
	
	protected String getDefaultText() {
		return ResourceManager.format(emptyContent, 
				ResourceManager.getInstance().get("plugins.core.outputView.emptyContent")); //$NON-NLS-1$;
	}
	
	private String getUnsupportedText() {
		return ResourceManager.format(emptyContent, 
				ResourceManager.getInstance().get("plugins.core.outputView.unsupportedContent")); //$NON-NLS-1$;
	}
	
	private static URL getBaseURL() {
		if(baseURL==null) {
			URL jarLocation = LogRecordPresenter.class.getProtectionDomain()
					.getCodeSource().getLocation();
			
			try {
				baseURL = new URL(jarLocation, "net/ikarus_systems/icarus/plugins/core/icons/"); //$NON-NLS-1$
			} catch (MalformedURLException e) {
				LoggerFactory.log(LogRecordPresenter.class, Level.SEVERE, "Failed to create base URL for icons folder at jar: "+jarLocation, e); //$NON-NLS-1$
			}
			
			LoggerFactory.log(LogRecordPresenter.class, Level.FINE, "New base url for log-record presenter html templates:\n"+baseURL); //$NON-NLS-1$
		}
		return baseURL;
	}

	/**
	 * 
	 */
	public LogRecordPresenter() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(ContentType type) {
		return ContentTypeRegistry.isCompatible("LogRecordContentType", type); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#present(java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public void present(Object data, Options options)
			throws UnsupportedPresentationDataException {
		if(data==null)
			throw new IllegalArgumentException("Invalid data"); //$NON-NLS-1$
		
		if(data==presentedData) {
			return;
		}
		
		if(!(data instanceof LogRecord)) {
			throw new UnsupportedPresentationDataException("Cannot present data: "+data); //$NON-NLS-1$
		}
		
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		presentedData = (LogRecord) data;
		this.options = options;
		
		if(contentPane!=null) {
			refresh();
		}
	}
	
	private String getCachedText(LogRecord record) {
		if(textCache==null) {
			return null;
		}
		
		return textCache.get(record);
	}
	
	private void cacheText(LogRecord record, String text) {
		if(textCache==null) {
			// Make cache size affordable
			// TODO leave size at 10 or increase to 20 or 50?
			textCache = new WeakLRUCache<>(10);
		}
		
		textCache.put(record, text);
	}
	
	@Override
	protected void refresh() {
		if(contentPane==null) {
			return;
		}
		
		LogRecord record = presentedData;
		
		// Just show default message if nothing is there to display
		if(record==null) {
			contentPane.setText(getDefaultText());
			return;
		}
		
		// Look for cached text first
		String cachedText = getCachedText(record);
		if(cachedText!=null) {
			contentPane.setText(cachedText);
			return;
		}
		
		Template template = getSharedTemplate();
		if(template==null) {
			contentPane.setText(getUnsupportedText());
			return;
		}
		
		template.clear();
		
		Level level = record.getLevel();
		String icon = "debugtt_obj.gif"; //$NON-NLS-1$
		if(level.intValue()>=Level.SEVERE.intValue()) {
			icon = "error_tsk.gif"; //$NON-NLS-1$
		} else if(level.intValue()>=Level.WARNING.intValue()) {
			icon = "warning_obj.gif"; //$NON-NLS-1$
		} else if(level.intValue()>=Level.INFO.intValue()) {
			icon = "information.gif"; //$NON-NLS-1$
		}
		
		// Header stuff
		template.setValue("base", getBaseURL()); //$NON-NLS-1$
		template.setValue("icon", icon); //$NON-NLS-1$
		
		// Caption
		template.setValue("caption", ResourceManager.getInstance().get( //$NON-NLS-1$
				"plugins.core.logView.labels.caption")); //$NON-NLS-1$
		
		// General
		template.setValue("captionLogger", ResourceManager.getInstance().get( //$NON-NLS-1$
				"plugins.core.logView.labels.logger")); //$NON-NLS-1$
		template.setValue("logger", noneOrNonempty(record.getLoggerName())); //$NON-NLS-1$
		template.setValue("captionLevel", ResourceManager.getInstance().get( //$NON-NLS-1$
				"plugins.core.logView.labels.level")); //$NON-NLS-1$
		String levelString = ResourceManager.getInstance().get(
				"plugins.core.logView.levels."+level.getName())+" ("+level.intValue()+")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		template.setValue("level", levelString); //$NON-NLS-1$
		template.setValue("captionDate", ResourceManager.getInstance().get( //$NON-NLS-1$
				"plugins.core.logView.labels.date")); //$NON-NLS-1$
		date.setTime(record.getMillis());
		template.setValue("date", noneOrNonempty(format.format(date))); //$NON-NLS-1$
		template.setValue("captionThreadID", ResourceManager.getInstance().get( //$NON-NLS-1$
				"plugins.core.logView.labels.threadId")); //$NON-NLS-1$
		template.setValue("threadID", noneOrNonempty(record.getThreadID())); //$NON-NLS-1$
		template.setValue("captionClassName", ResourceManager.getInstance().get( //$NON-NLS-1$
				"plugins.core.logView.labels.className")); //$NON-NLS-1$
		template.setValue("className", noneOrNonempty(record.getSourceClassName())); //$NON-NLS-1$
		template.setValue("captionMethodName", ResourceManager.getInstance().get( //$NON-NLS-1$
				"plugins.core.logView.labels.methodName")); //$NON-NLS-1$
		template.setValue("methodName", noneOrNonempty(record.getSourceMethodName())); //$NON-NLS-1$
		
		// Message
		template.setValue("captionMessage", ResourceManager.getInstance().get( //$NON-NLS-1$
				"plugins.core.logView.labels.message")); //$NON-NLS-1$
		String message = HtmlUtils.escapeHTML(record.getMessage());
		message = message.replaceAll("[\n\r]+", BR); //$NON-NLS-1$
		template.setRawValue("message", noneOrNonempty(message)); //$NON-NLS-1$
		
		// Stack-trace
		template.setValue("captionStackTrace", ResourceManager.getInstance().get( //$NON-NLS-1$
				"plugins.core.logView.labels.stackTrace")); //$NON-NLS-1$
		Throwable t = record.getThrown();
		if(t!=null && !options.get("compact", false)) { //$NON-NLS-1$
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println(t.getMessage());
            pw.println();
            t.printStackTrace(pw);
            pw.close();
            
            String trace = HtmlUtils.escapeHTML(sw.toString());
            trace = trace.replaceAll("\r\n|\n|\r", BR); //$NON-NLS-1$
            trace = trace.replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;"); //$NON-NLS-1$ //$NON-NLS-2$
            trace = trace.replaceAll(" ", "&nbsp;"); //$NON-NLS-1$ //$NON-NLS-2$
            template.setRawValue("stackTrace", trace); //$NON-NLS-1$
		} else {
			template.setValue("stackTrace", NONE); //$NON-NLS-1$
		}
				
		String text = template.getText();
		contentPane.setText(text);
		contentPane.setCaretPosition(0);
		// FIXME sometimes scrollPane jumps to the bottom after setText() with new content
		
		// Do not cache every little stuff
		if(text.length()>1000) {
			cacheText(record, text);
		}
	}
	
	@Override
	public void close() {
		// no-op
	}
}

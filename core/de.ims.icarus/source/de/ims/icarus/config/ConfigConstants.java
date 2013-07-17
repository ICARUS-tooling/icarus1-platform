/**
 * 
 */
package de.ims.icarus.config;

import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JList;

import de.ims.icarus.config.ConfigRegistry.EntryType;
import de.ims.icarus.config.ConfigRegistry.ValueFilter;


/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ConfigConstants {

	// MODIFIERS
	
	/**
	 * Hint for config-guis that an entry or group should not be displayed
	 */
	public static final int ENTRY_HIDDEN = 2;
	
	/**
	 * Hint for config-guis that an entry or group should not be
	 * displayed as editable.
	 */
	public static final int ENTRY_MODIFIABLE = 4;
	
	/**
	 * Signals that the given entry or group should not be
	 * modified by general modification methods.
	 */
	public static final int ENTRY_LOCKED = 8; 
	
	/**
	 * Signals that the given group should not be treated
	 * as a regular group when it comes to laying out related
	 * gui components but as an item. For a default config-gui
	 * this means that this group should not be displayed as
	 * an entry in the tree outline.
	 */
	public static final int GROUP_VIRTUAL = 16; 
	
	// STORAGE SAVING STRATEGIES
	
	/**
	 * Saving is omitted until manually done via a call to
	 * {@code ConfigStorage#commit()}
	 */
	public static final int MANUAL_SAVING = 0;
	
	/**
	 * Every change propagated to this storage will trigger
	 * a complete save of the stored data.
	 */
	public static final int IMMEDIATE_SAVING = 2;
	
	/**
	 * Saving is delayed until at least a certain number of
	 * changes is propagated.
	 */
	public static final int BLOCKWISE_SAVING = 4;
	
	/**
	 * Saving is done on a regular basis with fixed intervals.
	 */
	public static final int PERIODIC_SAVING = 8;
	
	// PROPERTY KEYS
	
	/**
	 * Tells a config-gui to layout this item without
	 * any kind of separation towards its parent.<p>
	 * For a virtual group this would mean it should not be 
	 * surrounded by a border.
	 * 
	 * The type of this property's value is {@code Boolean}
	 */
	public static final String INLINE = "inline"; //$NON-NLS-1$
	
	/**
	 * Tells a config-gui to place some kind of separator between
	 * this item and the one added right before it.<p>
	 * This property can be ignored if separation would make no sense,
	 * e.g. if the item is the first inside a group or the element
	 * before it already features some kind of visual separator.
	 * 
	 * The type of this property's value is {@code Boolean}
	 */
	public static final String SEPARATED = "separated"; //$NON-NLS-1$
	
	/**
	 * Used for items whose value type is {@code String} to
	 * signal that a text area or some other kind of multiline
	 * input field should be used. 
	 * 
	 * The type of this property's value is {@code Boolean}
	 */
	public static final String MULTILINE = "multiline"; //$NON-NLS-1$
	
	/**
	 * Used for items whose value type is {@code String} to
	 * determine the maximum number of characters allowed.
	 * 
	 * The type of this property's value is {@code Integer}
	 */
	public static final String MAX_LENGTH = "maxLength"; //$NON-NLS-1$
	
	/**
	 * Handler object for this entry that is responsible for
	 * providing an UI object.
	 * 
	 * The type of this property's value is {@link EntryHandler} or
	 * {@link MapHandler}.
	 */
	public static final String HANDLER = "handler"; //$NON-NLS-1$
	
	/**
	 * Holds a renderer that should be used to render {@link JList} or 
	 * {@link JComboBox} entries.<p>
	 * Note: This practically overrides any attempts made in terms of
	 * optimization regarding the type of components used to display
	 * this entry. If the type of this config item is {@link EntryType.OPTIONS}
	 * it would normally be possible to use radio buttons or a {@code JComboBox}
	 * as components. With a non-null {@link #RENDERER} set there must be used
	 * a {@code JComboBox}! 
	 * 
	 * The type of this property's value is {@code ListCellRenderer}.
	 */
	public static final String RENDERER = "renderer"; //$NON-NLS-1$
	
	/**
	 * Layout information, legal values are the constants
	 * {@code SwingConstants#HORIZONTAL} and
	 * {@code SwingConstants#VERTICAL} (the default).
	 * Any config-gui should lay out the direct children of
	 * the corresponding item in this way!
	 */
	public static final String ORIENTATION = "orientation"; //$NON-NLS-1$
	
	/**
	 * Used by {@link ValueFilter} objects to
	 * restrict a lower numerical bound.
	 * 
	 * The type of this property's value is any subtype of {@code Number}
	 */
	public static final String MIN_VALUE = "minValue"; //$NON-NLS-1$
	
	/**
	 * Hint for config-guis what precision to be used
	 * for e.g. slider components in combination with this entry.
	 * 
	 * The type of this property's value is any subtype of {@code Number}
	 */
	public static final String PRECISION = "precision"; //$NON-NLS-1$
	
	/**
	 * Used by {@link ValueFilter} objects to
	 * restrict an upper numerical bound.
	 * 
	 * The type of this property's value is any subtype of<code>Number</code>
	 */
	public static final String MAX_VALUE = "maxValue"; //$NON-NLS-1$
	
	/**
	 * Should be set when using the {@link ConfigRegistry#exclusiveRangeFilter} defined
	 * in {@link ConfigRegistry}. Signals that the upper and lower bounds
	 * are to be excluded from the legal range of values for the given entry.
	 * 
	 * The type of this property's value is {@code Boolean}
	 */
	public static final String EXCLUSIVE = "exclusive"; //$NON-NLS-1$
	
	/**
	 * The entry should be displayed indented relative to its neighbors.
	 * 
	 * The type of this property's value is {@code Boolean}
	 */
	public static final String INDENT = "indent"; //$NON-NLS-1$
	
	/**
	 * Used by {@link ValueFilter} objects that restrict
	 * textual input.
	 * 
	 * The type of this property's value is {@code String} or {@code Pattern}
	 */
	public static final String PATTERN = "pattern"; //$NON-NLS-1$
	
	/**
	 * For list and or map config entries this defines the type of
	 * the elements (list) or values(map).
	 * 
	 * The type of this property's value is {@link EntryType}
	 */
	public static final String ITEM_TYPE = "itemType"; //$NON-NLS-1$
	
	/**
	 * An enumeration of legal values used for CHOICE entries.
	 * 
	 * The type of this property's value is {@link List}
	 */
	public static final String OPTIONS = "options"; //$NON-NLS-1$
	
	/**
	 * Contains the objects to be displayed in a config-gui
	 * instead of the values in {@code OPTIONS}. Typically
	 * this list holds the localization keys for lookup.
	 * 
	 * The type of this property's value is {@code List}
	 */
	public static final String OPTIONS_KEYS = "optionsKeys"; //$NON-NLS-1$
	
	/**
	 * Lower bound for the number of item in a modifiable
	 * entry of type {@code EntryType#LIST} or {@code EntryType#MAP}.
	 * A config-gui should disable components related to remove
	 * actions when the actual number of elements in the list
	 * reaches this value.
	 * 
	 * The type of this property's value is {@code Integer}
	 */
	public static final String MIN_ITEM_COUNT = "minItemCount"; //$NON-NLS-1$
	
	/**
	 * Upper bound for the number of item in a modifiable
	 * entry of type {@code EntryType#LIST} or {@code EntryType#MAP}.
	 * A config-gui should disable components related to add/new
	 * actions when the actual number of elements in the list
	 * reaches this value.
	 * 
	 * The type of this property's value is {@code Integer}
	 */
	public static final String MAX_ITEM_COUNT = "maxItemCount"; //$NON-NLS-1$
	
	/**
	 * Used for localization to retrieve the actual string
	 * to be displayed as name of this item. Usually the name
	 * of the item itself serves as a key for localization
	 * but sometimes one might want to display different name
	 * strings for items that share the same name key.
	 * 
	 * The type of this property's value is {@code String}
	 */
	public static final String NAME_KEY = "nameKey"; //$NON-NLS-1$
	
	/**
	 * Used for localization to retrieve a description text.
	 * By convention the description of a group should by displayed as 
	 * a label before any of its children is displayed whereas the description
	 * of an entry should be displayed by means of a tooltip.
	 * Entries with custom objects as value might decide to handle
	 * this property in other ways.
	 * 
	 * The type of this property's value is {@code String}
	 */
	public static final String DESCRIPTION_KEY = "descriptionKey"; //$NON-NLS-1$
	
	/**
	 * This is an optional addition to entries representing text with
	 * a pattern applied to them. The purpose of this property is to
	 * present the user some kind of description on how the pattern
	 * influences legal input (e.g. "Only uppercase letters followed by one or
	 * more digits").
	 * 
	 * The type of this property's value is {@code String}
	 */
	public static final String PATTERN_DESCRIPTION_KEY = "patternDescriptionKey"; //$NON-NLS-1$
	
	/**
	 * Used for localization to retrieve a note text that should be 
	 * displayed next to the given entry or group to signal the user
	 * important facts.
	 * 
	 * The type of this property's value is {@code String}
	 */
	public static final String NOTE_KEY = "noteKey"; //$NON-NLS-1$
	
	/**
	 * Hint for any config-gui on how to display this group in a
	 * config dialog. Legal values are all the constants in this file
	 * starting with {@code MODE_}.
	 * 
	 * The type of this property's value is {@code String}
	 */
	public static final String DISPLAY_MODE = "displayMode"; //$NON-NLS-1$

	/**
	 * Generate a tree view of this group and all its subgroups.
	 * By convention the current root group of a config dialog
	 * should always be displayed as a tree with all its non-virtual
	 * child-groups as tree-nodes and so on.
	 */
	public static final String MODE_TREE = "tree"; //$NON-NLS-1$

	/**
	 * Arrange children as tabs where every subgroup gets
	 * its own tab and entries of this group be layed out
	 * after (below) the tabs.
	 */
	public static final String MODE_TABBED = "tabbed"; //$NON-NLS-1$

	/**
	 * Children should be layed out as a simple list
	 * one after another following the orientation of this
	 * group if any is set (default should be vertical).
	 */
	public static final String MODE_LIST = "list"; //$NON-NLS-1$

	/**
	 * Children should be layed out in a grid oriented
	 * way, depending on the orientation of this group
	 * (default should be vertical).
	 */
	public static final String MODE_GRID = "grid"; //$NON-NLS-1$
}

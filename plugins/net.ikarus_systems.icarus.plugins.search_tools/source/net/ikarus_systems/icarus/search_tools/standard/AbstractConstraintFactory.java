/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.standard;

import net.ikarus_systems.icarus.language.LanguageUtils;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.search_tools.ConstraintFactory;
import net.ikarus_systems.icarus.search_tools.SearchOperator;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractConstraintFactory implements ConstraintFactory {

	private String nameKey, descriptionKey;
	
	private String token;
	
	private int type;
	
	protected static final Object[] DEFAULT_UNDEFINED_VALUESET = {
		LanguageUtils.DATA_UNDEFINED_LABEL
	};

	public AbstractConstraintFactory(String token, int type, String nameKey, String descriptionKey) {
		this.token = token;
		this.nameKey = nameKey;
		this.descriptionKey = descriptionKey;
		this.type = type;
	}
	
	/**
	 * 
	 * @see net.ikarus_systems.icarus.search_tools.ConstraintFactory#getConstraintType()
	 */
	@Override
	public int getConstraintType() {
		return type;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getToken() {
		return token;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getName()
	 */
	@Override
	public String getName() {
		return ResourceManager.getInstance().get(nameKey);
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		return ResourceManager.getInstance().get(descriptionKey);
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.ConstraintFactory#getSupportedOperators()
	 */
	@Override
	public SearchOperator[] getSupportedOperators() {
		return SearchOperator.values();
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.ConstraintFactory#getValueClass()
	 */
	@Override
	public Class<?> getValueClass() {
		return String.class;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.ConstraintFactory#getDefaultValue()
	 */
	@Override
	public Object getDefaultValue() {
		return LanguageUtils.DATA_UNDEFINED_LABEL;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.ConstraintFactory#getValueSet()
	 */
	@Override
	public Object[] getLabelSet() {
		return DEFAULT_UNDEFINED_VALUESET;
	}

	@Override
	public Object labelToValue(Object label) {
		return label;
	}

	@Override
	public Object valueToLabel(Object value) {
		return value;
	}
}

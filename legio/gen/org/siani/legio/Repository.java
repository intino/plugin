package org.siani.legio;

import org.siani.legio.*;

import java.util.*;

public class Repository extends tara.magritte.Layer implements tara.magritte.tags.Component, tara.magritte.tags.Terminal {
	protected java.lang.String url;

	public Repository(tara.magritte.Node node) {
		super(node);
	}

	public java.lang.String url() {
		return url;
	}

	public void url(java.lang.String value) {
		this.url = value;
	}

	@Override
	public java.util.Map<java.lang.String, java.util.List<?>> variables() {
		java.util.Map<String, java.util.List<?>> map = new java.util.LinkedHashMap<>();
		map.put("url", new java.util.ArrayList(java.util.Collections.singletonList(this.url)));
		return map;
	}

	public tara.magritte.Concept concept() {
		return this.graph().concept(org.siani.legio.Repository.class);
	}

	@Override
	protected void _load(java.lang.String name, java.util.List<?> values) {
		super._load(name, values);
		if (name.equalsIgnoreCase("url")) this.url = tara.magritte.loaders.StringLoader.load(values, this).get(0);
	}

	@Override
	protected void _set(java.lang.String name, java.util.List<?> values) {
		super._set(name, values);
		if (name.equalsIgnoreCase("url")) this.url = (java.lang.String) values.get(0);
	}

	public Create create() {
		return new Create(null);
	}

	public Create create(java.lang.String name) {
		return new Create(name);
	}

	public class Create {
		protected final java.lang.String name;

		public Create(java.lang.String name) {
			this.name = name;
		}
		
	}
	
	public org.siani.legio.LegioApplication application() {
		return ((org.siani.legio.LegioApplication) graph().application());
	}
}

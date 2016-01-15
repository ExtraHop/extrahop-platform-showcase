package com.extrahop.tools.domain;

/**
 * This class represents a JSON Trigger object. Properties are automatically mapped to JSON properties during serialization
 * and de-serialization.
 * @author jeffbfry
 *
 */
public class Trigger extends EHObject {
	
	protected boolean apply_all;
	protected String author = "";
	protected boolean debug;
	protected String description = "";
	protected boolean disabled;
	protected String event="";
	protected Hints hints = new Hints();
	protected int priority;
	protected String script;
	
	public Trigger() {
		super();
	}
	public boolean isApply_all() {
		return apply_all;
	}
	public void setApply_all(boolean apply_all) {
		this.apply_all = apply_all;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public boolean isDebug() {
		return debug;
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isDisabled() {
		return disabled;
	}
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	public Hints getHints() {
		return hints;
	}
	public void setHints(Hints hints) {
		this.hints = hints;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public String getScript() {
		return script;
	}
	public void setScript(String script) {
		this.script = script;
	}
	
}

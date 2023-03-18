package org.openmrs.module.kenyaemrml.domain;

import java.util.Map;

public class ModelInputFields {
	
	private static final long serialVersionUID = 9053732207572116071L;
	
	private Map<String, Object> fields;
	
	public Map<String, Object> getFields() {
		return fields;
	}
	
	public void setFields(Map<String, Object> fields) {
		this.fields = fields;
	}

	@Override
	public String toString() {
		String ret = "";
		StringBuilder mapAsString = new StringBuilder("{");
		for (String key : fields.keySet()) {
			mapAsString.append(key + "=" + fields.get(key) + ", ");
		}
		mapAsString.delete(mapAsString.length()-2, mapAsString.length()).append("}");
		ret = "ModelInputFields [fields=" + mapAsString.toString() + "]";
		return ret;
	}
}

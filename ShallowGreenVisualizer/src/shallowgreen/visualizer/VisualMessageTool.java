package shallowgreen.visualizer;

public abstract class VisualMessageTool {

	public static final StringBuilder removeMessage(String id) {
		StringBuilder sb=new StringBuilder(30);
		removeMessage(sb,id);
		return sb;
	}

	public static final void removeMessage(StringBuilder sb, String id) {
		sb.append("{\"type\":\"remove\",\"id\":\"");
		sb.append(id);
		sb.append("\"}");
	}

	public static final StringBuilder updateMessage(String tagName, String id, Object... attrNameValues) {
		StringBuilder sb=new StringBuilder(60);
		updateMessage(sb,tagName,id,attrNameValues);
		return sb;
	}

	public static final void updateMessage(StringBuilder sb, String tagName, String id, Object... attrNameValues) {
		if(attrNameValues.length%2!=0)
			throw new IllegalArgumentException("attrNameValues must have both name and value for each entry");
		sb.append("{\"type\":\"usvg\",\"tag\":\"");
		sb.append(tagName);
		sb.append("\",\"id\":\"");
		sb.append(id);
		sb.append("\",\"attrs\":{");
		for(int i=0; i<attrNameValues.length; i++) {
			if(i%2==0) {
				// name -> ,"name":
				if(i!=0) {
					// not first name -> ,"
					sb.append(",\"");
				} else {
					// first name -> "
					sb.append("\"");
				}
				sb.append(attrNameValues[i].toString());
				sb.append("\":\"");
			} else {
				// value -> value"
				sb.append(attrNameValues[i].toString());
				sb.append("\"");
			}
		}
		sb.append("}}");
	}

}
